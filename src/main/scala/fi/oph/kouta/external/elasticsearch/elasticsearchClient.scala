package fi.oph.kouta.external.elasticsearch

import com.sksamuel.elastic4s.http.{ElasticClient, ElasticProperties}
import fi.oph.kouta.external.KoutaConfigurationFactory
import org.json4s.Serialization

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ElasticsearchClientHolder {
  val elasticUrl: String = KoutaConfigurationFactory.configuration.elasticSearchConfiguration.elasticUrl
  val client: ElasticClient = createClient

  def createClient = ElasticClient(ElasticProperties(elasticUrl))
}

trait ElasticsearchClient {
  lazy val elasticClient: ElasticClient = ElasticsearchClientHolder.client

  implicit val json4s: Serialization = org.json4s.jackson.Serialization

  def withTemporaryElasticClient[T](f: ElasticClient => Future[T]): Future[T] = {
    val client = ElasticsearchClientHolder.createClient
    f(client).andThen { case _ => client.close() }
  }
}
