package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.HitReader
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.get.GetResponse
import com.sksamuel.elastic4s.http.search.SearchIterator
import com.sksamuel.elastic4s.http.{ElasticClient, ElasticProperties, RequestFailure, RequestSuccess}
import com.sksamuel.elastic4s.searches.queries.Query
import fi.oph.kouta.internal.KoutaConfigurationFactory
import fi.oph.kouta.internal.domain.WithTila
import fi.oph.kouta.internal.domain.enums.Julkaisutila.Tallennettu
import fi.oph.kouta.internal.util.KoutaJsonFormats
import fi.vm.sade.utils.slf4j.Logging

import java.util.NoSuchElementException
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.reflect.ClassTag
import scala.util.{Failure, Success}

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
          handleSuccesfulReponse(id, response)
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

  private def handleSuccesfulReponse[T <: WithTila: HitReader](id: String, response: RequestSuccess[GetResponse]) = {
    response.status match {
      case 404 => Future.successful(None)
      case _   => mapResultToEntity(id, response)
    }
  }

  private def mapResultToEntity[T <: WithTila: HitReader](id: String, response: RequestSuccess[GetResponse]) = {
    response.result.safeTo[T] match {
      case Success(x) =>
        Future.successful(Option(x))
      case Failure(exception) =>
        logger.error(
          s"Unable to read response entity with id $id from index $index. Not going to serve coffee from teapot!",
          exception
        )
        Future.failed(
          TeapotException(
            s"Unable to read response entity with id $id from index $index. Not going to serve coffee from teapot!",
            exception
          )
        )
    }
  }

  def searchItems[T: HitReader: ClassTag](query: Option[Query]): Future[IndexedSeq[T]] = {
    val notTallennettu = not(termsQuery("tila.keyword", "tallennettu"))
    implicit val duration: FiniteDuration = Duration(10, TimeUnit.SECONDS)

    query.fold[Future[IndexedSeq[T]]]({
      Future(
        SearchIterator.iterate[T](client, search(index).query(notTallennettu).keepAlive("1m").size(500)).toIndexedSeq
      )
    })(q => {
      val request           = search(index).bool(must(notTallennettu, q)).keepAlive("1m").size(500)
      logger.info(s"Elasticsearch request: ${request.show}")
      Future {
        SearchIterator.hits(client, request)
          .toIndexedSeq
          .map(hit => hit.safeTo[T])
          .flatMap(entity => entity match {
            case Success(value) => Some(value)
            case Failure(exception) =>
              logger.error(
                s"Unable to deserialize json response to entity: ",
                exception
              )
              None
          })
      }
    })
  }
}

object ElasticsearchClient {
  val client: ElasticClient = ElasticClient(
    ElasticProperties(KoutaConfigurationFactory.configuration.elasticSearchConfiguration.elasticUrl)
  )
}
