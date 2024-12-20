package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.http.{JavaClient, NoOpHttpClientConfigCallback}
import com.sksamuel.elastic4s.requests.get.GetResponse
import com.sksamuel.elastic4s.requests.searches.SearchRequest
import com.sksamuel.elastic4s.requests.searches.queries.Query
import com.sksamuel.elastic4s._
import fi.oph.kouta.internal.domain.WithTila
import fi.oph.kouta.internal.domain.enums.Julkaisutila.Tallennettu
import fi.oph.kouta.internal.util.KoutaJsonFormats
import fi.oph.kouta.internal.{ElasticSearchConfiguration, KoutaConfigurationFactory}
import fi.oph.kouta.util.Timer.timed;
import fi.oph.kouta.logging.Logging
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.config.RequestConfig.Builder
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback

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
  private val cachedClient          = client
  private val iterativeElasticFetch = new IterativeElasticFetch(client)

  def getItem[T <: WithTila: HitReader](id: String): Future[T] = timed(s"GetItem from ElasticSearch (Id: ${id}", 100) {
    val request = get(index, id)
    logger.debug(s"Elasticsearch query: ${request.show}")
    cachedClient
      .execute(request) recoverWith { case t: Throwable =>
      logger.warn(s"Elastic query for cachedClient failed: ${t.getMessage}. Will retry.")
      cachedClient.execute(request)
    } flatMap {
      case failure: RequestFailure =>
        logger.debug(s"Elasticsearch status: {}", failure.status)
        Future.failed(ElasticSearchException(failure.error))
      case response: RequestSuccess[GetResponse] =>
        if (response.status != 200) {
          logger.warn(
            s"Successful response had status other than 200: ${response.status}. Body: ${response.body}, result: ${response.result}"
          )
        }
        logger.debug(s"Elasticsearch status: {}", response.status)
        logger.debug(s"Elasticsearch response: {}", response.result.sourceAsString)
        handleSuccesfulReponse(id, response)
    } flatMap {
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
      val notTallennettu = not(termsQuery("tila.keyword", "tallennettu"))

      query.fold[Future[IndexedSeq[T]]](
        executeScrollQuery(search(index).query(notTallennettu).keepAlive("1m").size(500))
      )(q => {
        val request = search(index).bool(must(notTallennettu, q)).keepAlive("1m").size(500)
        executeScrollQuery(request)
      })
    }
  }

  def searchItemBulks[T: HitReader: ClassTag](
      query: Option[Query],
      offset: Int,
      limit: Option[Int]
  ): Future[IndexedSeq[T]] = {
    timed(s"Search item bulks from ElasticSearch (Query: ${query}, offset: ${offset}, limit: ${limit})", 100) {
      val request = search(index).query(query.get).keepAlive("1m").size(500)
      executeScrollQuery[T](request)
    }
  }

  private def executeScrollQuery[T: HitReader: ClassTag](req: SearchRequest): Future[IndexedSeq[T]] = {
    implicit val duration: FiniteDuration = Duration(1, TimeUnit.MINUTES)
    logger.info(s"Elasticsearch request: ${req.show}")
    iterativeElasticFetch
      .fetch(req)
      .map(hit => hit.flatMap(_.safeTo[T].toOption))
      .mapTo[IndexedSeq[T]]
  }
}

object ElasticsearchClient {
  val config: ElasticSearchConfiguration = KoutaConfigurationFactory.configuration.elasticSearchConfiguration;
  val httpClientConfigCallback: HttpClientConfigCallback = if (config.authEnabled) {
    lazy val provider = {
      val provider    = new BasicCredentialsProvider
      val credentials = new UsernamePasswordCredentials(config.username, config.password)
      provider.setCredentials(AuthScope.ANY, credentials)
      provider
    }
    (httpClientBuilder: HttpAsyncClientBuilder) => {
      httpClientBuilder.setDefaultCredentialsProvider(provider)
    }
  } else {
    NoOpHttpClientConfigCallback
  }
  val client: ElasticClient = ElasticClient(
    JavaClient(
      ElasticProperties(config.elasticUrl),
      (requestConfigBuilder: Builder) => {
        requestConfigBuilder
      },
      httpClientConfigCallback
    )
  )
}
