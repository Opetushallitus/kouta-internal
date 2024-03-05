package fi.oph.kouta.internal.elasticsearch

import co.elastic.clients.elasticsearch
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.ElasticsearchTransport
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
//import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.{FieldSort, SortOptions, SortOrder, Time, query_dsl}
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders
import co.elastic.clients.elasticsearch.core.{OpenPointInTimeRequest, search}
import co.elastic.clients.elasticsearch.core.search.PointInTimeReference
import co.elastic.clients.elasticsearch.core.SearchRequest
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.http.{JavaClient, NoOpHttpClientConfigCallback}
import com.sksamuel.elastic4s.requests.get.GetResponse

import java.util
import scala.reflect.classTag
//import com.sksamuel.elastic4s.requests.searches.SearchRequest
import com.sksamuel.elastic4s.requests.searches.queries.Query
import com.sksamuel.elastic4s._
import fi.oph.kouta.internal.domain.WithTila
import fi.oph.kouta.internal.domain.enums.Julkaisutila.Tallennettu
import fi.oph.kouta.internal.util.KoutaJsonFormats
import fi.oph.kouta.internal.{ElasticSearchConfiguration, KoutaConfigurationFactory}
import fi.vm.sade.utils.Timer.timed
import fi.vm.sade.utils.slf4j.Logging
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

import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`

trait ElasticsearchClient { this: KoutaJsonFormats with Logging =>
  val index: String
  val client: ElasticClient

  val clientJava: elasticsearch.ElasticsearchClient
  val mapper: ObjectMapper = new ObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .registerModule(DefaultScalaModule)
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
        executeScrollQuery(ElasticApi.search(index).query(notTallennettu).keepAlive("1m").size(500))
      )(q => {
        val request = ElasticApi.search(index).bool(must(notTallennettu, q)).keepAlive("1m").size(500)
        executeScrollQuery(request)
      })
    }
  }
//
  protected def searchItemsNew[T: ClassTag](queryList: java.util.List[query_dsl.Query]): IndexedSeq[T] = { // List[T] = {
    val searchSize = 500
    try {
      val openPitRequest = new OpenPointInTimeRequest.Builder()
        .index(index)
        .keepAlive(new Time.Builder().time("1m").build())
        .build()
      val pointInTimeReference: PointInTimeReference = new PointInTimeReference.Builder()
        .keepAlive(new Time.Builder().time("1m").build())
        .id(clientJava.openPointInTime(openPitRequest).id())
        .build()
      val sortOpt =
        new SortOptions.Builder().field(FieldSort.of(f => f.field("oid.keyword").order(SortOrder.Asc))).build()
      val query = QueryBuilders.bool.must(queryList).build._toQuery()
      var searchRequestBuilder =
        new SearchRequest.Builder().query(query).size(searchSize).sort(sortOpt).pit(pointInTimeReference)

      val searchRequest = searchRequestBuilder.build()
      logger.info("searchRequest.query().toString = " +searchRequest.query().toString)
      var response: co.elastic.clients.elasticsearch.core.SearchResponse[Map[String, Object]] =
        clientJava.search(searchRequest, classOf[Map[String, Object]])

      var hitList = new util.ArrayList[search.Hit[Map[String, Object]]]
      hitList.addAll(response.hits().hits())
      var hitCount = hitList.size()

      // Search rest of results (While hitCount equals searchSize there is more search results)
      while (hitCount == searchSize) {
        val lastHit = response.hits().hits().last
        val lastHitSort = lastHit.sort()

        searchRequestBuilder = new SearchRequest.Builder()
          .query(query)
          .sort(sortOpt)
          .size(searchSize)
          .pit(pointInTimeReference)
          .searchAfter(lastHitSort)
        response = clientJava.search(searchRequestBuilder.build(), classOf[Object])
        hitList.addAll(response.hits().hits())
        hitCount = response.hits().hits().size()
      }

      val listToReturn =
//        hitList.map(hit => mapper.convertValue(hit.source(), classTag[T].runtimeClass).asInstanceOf[T]).toList
        hitList.map(hit => mapper.convertValue(hit.source(), classTag[T].runtimeClass).asInstanceOf[T]).toIndexedSeq
      listToReturn
//      List.empty
    } catch {
      case e: Exception =>
        logger.error("Got error: " + e.printStackTrace())
        IndexedSeq.empty
    }
  }

  def searchItemBulks[T: HitReader: ClassTag](
      query: Option[Query],
      offset: Int,
      limit: Option[Int]
  ): Future[IndexedSeq[T]] = {
    timed(s"Search item bulks from ElasticSearch (Query: ${query}, offset: ${offset}, limit: ${limit})", 100) {
      val request = ElasticApi.search(index).query(query.get).keepAlive("1m").size(500)
      executeScrollQuery[T](request)
    }
  }

  private def executeScrollQuery[T: HitReader: ClassTag](req: com.sksamuel.elastic4s.requests.searches.SearchRequest): Future[IndexedSeq[T]] = {
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

  val mapper: ObjectMapper = new ObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .registerModule(DefaultScalaModule)

  lazy val providerJavaClient = {
    val provider = new BasicCredentialsProvider
    val credentials = new UsernamePasswordCredentials(config.username, config.password)
    provider.setCredentials(AuthScope.ANY, credentials)
    provider
  }
  val restClient = RestClient
    .builder(HttpHost.create(config.elasticUrl))
    .setHttpClientConfigCallback(new HttpClientConfigCallback() {
      def customizeHttpClient(httpClientBuilder: HttpAsyncClientBuilder): HttpAsyncClientBuilder = {
        httpClientBuilder.disableAuthCaching
        httpClientBuilder.setDefaultCredentialsProvider(providerJavaClient)
      }
    })
    .build()
  val transport: ElasticsearchTransport =
    new RestClientTransport(restClient, new JacksonJsonpMapper(ElasticsearchClient.mapper))
  val clientJava: co.elastic.clients.elasticsearch.ElasticsearchClient =
    new elasticsearch.ElasticsearchClient(transport)
}
