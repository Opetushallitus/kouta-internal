package fi.oph.kouta.internal.integration.fixture

import fi.oph.kouta.internal.CommandLine.runBlocking
import fi.oph.kouta.internal.TempElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import scala.concurrent.ExecutionContext.Implicits.global
import com.sksamuel.elastic4s.{RequestFailure, RequestSuccess}
import com.sksamuel.elastic4s.requests.count.{CountRequest, CountResponse}

trait ElasticDumpFixture {
  private def elasticEmpty(): Boolean = {
    TempElasticClient.client.execute(count("_all")).await match {
      case result: RequestSuccess[CountResponse] =>
        result.result.count == 0
      case failure: RequestFailure =>
        print(s"Count request failed: ${failure.toString}")
        true
    }
  }

  def initIndices(): Unit = {
    val elasticHost = TempElasticClient.url.replace("localhost", "host.docker.internal")

    println("Importing elasticsearch data...")
    if (elasticEmpty()) {
      runBlocking(s"src/test/resources/load_elastic_dump.sh $elasticHost data,mapping,analyzer,alias,settings,template")
    } else {
      println("Elasticsearch not empty. Data already imported. Doing nothing.")
    }
  }
}
