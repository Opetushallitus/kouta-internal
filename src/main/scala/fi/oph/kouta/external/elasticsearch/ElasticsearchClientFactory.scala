package fi.oph.kouta.external.elasticsearch

import com.sksamuel.elastic4s.http.{ElasticClient, ElasticProperties}
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object ElasticsearchClientFactory extends Logging {

  val client = ElasticClient(ElasticProperties("http://localhost:9200"))

  def withClient[T](f: ElasticClient => Future[T]): Future[T] = {
    val c = client
    f(c).andThen { case _ => c.close() }
  }

}
