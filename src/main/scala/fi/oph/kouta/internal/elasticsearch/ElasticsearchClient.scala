package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.get.GetResponse
import com.sksamuel.elastic4s.requests.searches.SearchIterator
import com.sksamuel.elastic4s.requests.searches.queries.Query
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties, HitReader, RequestFailure, RequestSuccess}
import fi.oph.kouta.internal.KoutaConfigurationFactory
import fi.oph.kouta.internal.domain.WithTila
import fi.oph.kouta.internal.domain.enums.Julkaisutila.Tallennettu
import fi.oph.kouta.internal.util.KoutaJsonFormats
import fi.vm.sade.utils.Timer.timed
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

  def getItem[T <: WithTila: HitReader](id: String): Future[T] = timed(s"GetItem from ElasticSearch (Id: ${id}", 100) {
    val request = get(index, id)
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
    timed(s"SearchItems from ElasticSearch (Query: ${query}", 100) {
      val notTallennettu                    = not(termsQuery("tila.keyword", "tallennettu"))
      implicit val duration: FiniteDuration = Duration(1, TimeUnit.MINUTES)

      query.fold[Future[IndexedSeq[T]]]({
        Future(
          SearchIterator.iterate[T](client, search(index).query(notTallennettu).keepAlive("1m").size(500)).toIndexedSeq
        )
      })(q => {
        val request = search(index).bool(must(notTallennettu, q)).keepAlive("1m").size(500)
        logger.info(s"Elasticsearch request: ${request.show}")
        Future {
          SearchIterator
            .hits(client, request)
            .toIndexedSeq
            .map(hit => hit.safeTo[T])
            .flatMap(entity =>
              entity match {
                case Success(value) => Some(value)
                case Failure(exception) =>
                  logger.error(
                    s"Unable to deserialize json response to entity: ",
                    exception
                  )
                  None
              }
            )
        }
      })
    }
  }

  def searchItemBulks[T: HitReader: ClassTag](
      query: Option[Query],
      offset: Int,
      limit: Option[Int]
  ): Future[IndexedSeq[T]] = {
    timed(s"Search item bulks from ElasticSearch (Query: ${query}, offset: ${offset}, limit: ${limit})", 100) {
      implicit val duration: FiniteDuration = Duration(1, TimeUnit.MINUTES)
      val request                           = search(index).query(query.get).keepAlive("1m").size(500)
      logger.info(s"Elasticsearch request: ${request.show}")
      Future {
        SearchIterator
          .hits(client, request)
          .toIndexedSeq
          .map(hit => hit.safeTo[T])
          .flatMap(entity =>
            entity match {
              case Success(value) => Some(value)
              case Failure(exception) =>
                logger.error(
                  s"Unable to deserialize json response to entity: ",
                  exception
                )
                None
            }
          )
      }
    }
  }
}

object ElasticsearchClient {
  val client: ElasticClient = ElasticClient(
    JavaClient(ElasticProperties(KoutaConfigurationFactory.configuration.elasticSearchConfiguration.elasticUrl))
  )
}
