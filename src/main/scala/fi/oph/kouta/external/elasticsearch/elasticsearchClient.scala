package fi.oph.kouta.external.elasticsearch

import com.sksamuel.elastic4s.http.{ElasticClient, ElasticProperties}
import org.json4s.Serialization

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ElasticsearchClientHolder {
  def createClient = ElasticClient(ElasticProperties("http://localhost:9200"))

  val client: ElasticClient = createClient
}

trait ElasticsearchClient {
  val elasticClient: ElasticClient = ElasticsearchClientHolder.client

  implicit val json4s: Serialization = org.json4s.jackson.Serialization

  def withTemporaryElasticClient[T](f: ElasticClient => Future[T]): Future[T] = {
    val client = ElasticsearchClientHolder.createClient
    f(client).andThen { case _ => client.close() }
  }
}
