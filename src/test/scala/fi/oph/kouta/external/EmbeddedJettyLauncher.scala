package fi.oph.kouta.external

import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.get.GetResponse
import com.sksamuel.elastic4s.http.{RequestFailure, RequestSuccess}
import fi.oph.kouta.external.elasticsearch.{ElasticSearchException, ElasticsearchClient}
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object EmbeddedJettyLauncher extends Logging with ElasticsearchClient {

  val DefaultPort = "8097"

  val TestDataGeneratorSessionId = "ea596a9c-5940-497e-b5b7-aded3a2352a7"

  def main(args: Array[String]) {

/*
    println("Creating a client")
    Future.sequence {
      1.to(9).map(Integer.toString).map { i =>
        withTemporaryElasticClient { client =>
          client.execute {
            get(s"1.2.246.562.13.0000000000000000000$i").from("koulutus-kouta")
          }.flatMap {
            case failure: RequestFailure =>
              Future.failed(ElasticSearchException(failure.error))
            case response: RequestSuccess[GetResponse] =>
              Future.successful(response.result.sourceAsString)
          }
        }
      }
    }.andThen {
      case Success(f) => f.foreach(println)
      case Failure(exception) => throw exception
    }
*/

    val port = System.getProperty("kouta-external.port", DefaultPort).toInt
    logger.info(s"Starting standalone Kouta-external Jetty on port $port...")
    logger.info(s"http://localhost:$port/kouta-external/swagger")
    new JettyLauncher(port).start.join()
  }
}
