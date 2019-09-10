package fi.oph.kouta.external.elasticsearch

import java.util.NoSuchElementException

import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.get.GetResponse
import com.sksamuel.elastic4s.http.{ElasticClient, ElasticProperties, RequestFailure, RequestSuccess}
import fi.oph.kouta.external.KoutaConfigurationFactory
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.Serialization

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ElasticsearchClientHolder {
  def client: ElasticClient
}

object DefaultElasticsearchClientHolder extends ElasticsearchClientHolder {
  private lazy val elasticUrl: String = KoutaConfigurationFactory.configuration.elasticSearchConfiguration.elasticUrl

  private var clientHolder: Option[ElasticClient] = None

  def client: ElasticClient = clientHolder.getOrElse {
    clientHolder = Option(ElasticClient(ElasticProperties(elasticUrl)))
    clientHolder.orNull
  }
}

abstract class ElasticsearchClient(
    val index: String,
    val entityName: String,
    clientHolder: ElasticsearchClientHolder = DefaultElasticsearchClientHolder
) extends Logging {
  lazy val elasticClient: ElasticClient = clientHolder.client

  implicit val json4s: Serialization = org.json4s.jackson.Serialization

  protected def getItem(id: String): Future[GetResponse] =
    elasticClient.execute {
      get(id).from(index)
    }.flatMap {
      case failure: RequestFailure =>
        Future.failed(ElasticSearchException(failure.error))

      case response: RequestSuccess[GetResponse] if !response.result.exists =>
        Future.failed(new NoSuchElementException(s"No such element for $entityName $id"))

      case response: RequestSuccess[GetResponse] =>
        logger.debug(s"Elasticsearch status: ${response.status}")
        logger.debug(s"Elasticsearch response: ${response.result.sourceAsString}")
        Future.successful(response.result)
    }
}
