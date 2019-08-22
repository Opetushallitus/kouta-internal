package fi.oph.kouta.external.elasticsearch

import java.util.NoSuchElementException

import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.get.GetResponse
import com.sksamuel.elastic4s.http.{ElasticClient, ElasticProperties, RequestFailure, RequestSuccess}
import fi.oph.kouta.external.KoutaConfigurationFactory
import org.json4s.Serialization

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ElasticsearchClientHolder {
  val elasticUrl: String = KoutaConfigurationFactory.configuration.elasticSearchConfiguration.elasticUrl
  val client: ElasticClient = createClient

  def createClient = ElasticClient(ElasticProperties(elasticUrl))
}

abstract class ElasticsearchClient(val index: String, val entityName: String) {
  lazy val elasticClient: ElasticClient = ElasticsearchClientHolder.client

  implicit val json4s: Serialization = org.json4s.jackson.Serialization

  protected def withTemporaryElasticClient[T](f: ElasticClient => Future[T]): Future[T] = {
    val client = ElasticsearchClientHolder.createClient
    f(client).andThen { case _ => client.close() }
  }

  protected def getItem(id: String): Future[GetResponse] =
    elasticClient.execute {
      get(id).from(index)
    }.flatMap {
      case failure: RequestFailure =>
        Future.failed(ElasticSearchException(failure.error))

      case response: RequestSuccess[GetResponse] if !response.result.exists =>
        Future.failed(new NoSuchElementException(s"No such element for $entityName $id"))

      case response: RequestSuccess[GetResponse] =>
        Future.successful(response.result)
    }
}
