package fi.oph.kouta.internal.integration

import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.cluster.ClusterHealthResponse
import com.sksamuel.elastic4s.http.{RequestFailure, RequestSuccess}
import fi.oph.kouta.internal.elasticsearch.{DefaultElasticsearchClientHolder, ElasticsearchClient}
import org.scalatra.test.scalatest.ScalatraFlatSpec

import scala.concurrent.ExecutionContext.Implicits.global

class ElasticsearchBaseSpec
    extends ElasticsearchClient("test", "test", DefaultElasticsearchClientHolder)
      with ScalatraFlatSpec {

  "Tests" should "connect to elasticsearch" in {

    elasticClient.execute {
      clusterHealth()
    }.map { resp =>
      resp.isSuccess should be(true)
      resp match {
        case failure: RequestFailure => fail(s"Elastic health check failure: ${failure.error.reason}")
        case response: RequestSuccess[ClusterHealthResponse] =>
          println(s"response body ${response.body}")
      }
    }
  }
}
