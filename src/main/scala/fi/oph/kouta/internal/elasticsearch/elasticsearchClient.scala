package fi.oph.kouta.internal.elasticsearch

import java.util.NoSuchElementException
import java.util.concurrent.TimeUnit

import com.sksamuel.elastic4s.HitReader
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.get.GetResponse
import com.sksamuel.elastic4s.http.search.{SearchIterator, SearchResponse}
import com.sksamuel.elastic4s.http.{ElasticClient, ElasticProperties, RequestFailure, RequestSuccess}
import com.sksamuel.elastic4s.searches.queries.Query
import fi.oph.kouta.internal.KoutaConfigurationFactory
import fi.oph.kouta.internal.domain.WithTila
import fi.oph.kouta.internal.domain.enums.Julkaisutila.Tallennettu
import fi.oph.kouta.internal.util.KoutaJsonFormats
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

trait ElasticsearchClient { this: KoutaJsonFormats with Logging =>
  val index: String
  val client: ElasticClient

  def getItem[T <: WithTila: HitReader](id: String): Future[T] = {
    val request = get(id).from(index)
    logger.debug(s"Elasticsearch query: ${request.show}")
    client
      .execute(request)
      .flatMap {
        case failure: RequestFailure =>
          logger.debug(s"Elasticsearch status: {}", failure.status)
          Future.failed(ElasticSearchException(failure.error))
        case response: RequestSuccess[GetResponse] =>
          logger.debug(s"Elasticsearch status: {}", response.status)
          logger.debug(s"Elasticsearch response: {}", response.result.sourceAsString)
          Future.successful(response.result.toOpt[T])
      }
      .flatMap {
        case None =>
          Future.failed(new NoSuchElementException(s"Didn't find id $id from index $index"))
        case Some(t) if t.tila == Tallennettu =>
          Future.failed(new NoSuchElementException(s"Entity with id $id from index $index was in tila luonnos"))
        case Some(t) =>
          Future.successful(t)
      }
  }

  def searchItems[T: HitReader: ClassTag](query: Option[Query]): Future[IndexedSeq[T]] = {
    val notTallennettu = not(termsQuery("tila.keyword", "tallennettu"))
    query.fold[Future[IndexedSeq[T]]]({
      implicit val duration = Duration(10, TimeUnit.SECONDS)
      Future(
        SearchIterator.iterate[T](client, search(index).query(notTallennettu).keepAlive("1m").size(500)).toIndexedSeq
      )
    })(q => {
      implicit val duration = Duration(10, TimeUnit.SECONDS)
      val request           = search(index).bool(must(notTallennettu, q)).keepAlive("1m").size(500)
      logger.info(s"Elasticsearch request: ${request.show}")
      Future(
        SearchIterator.iterate[T](client, request).toIndexedSeq
      )
    })
  }
}

object ElasticsearchClient {
  val client = ElasticClient(
    ElasticProperties(KoutaConfigurationFactory.configuration.elasticSearchConfiguration.elasticUrl)
  )
}
