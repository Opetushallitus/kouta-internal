package fi.oph.kouta.external.elasticsearch

import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.cluster.ClusterHealthResponse
import com.sksamuel.elastic4s.http.{RequestFailure, RequestSuccess}
import fi.oph.kouta.external.domain.enums.ElasticsearchHealthStatus

import scala.concurrent.ExecutionContext.Implicits.global

object ElasticsearchHealth extends ElasticsearchHealth(DefaultElasticsearchClientHolder)

abstract class ElasticsearchHealth(clientHolder: ElasticsearchClientHolder)
    extends ElasticsearchClient("health", "health", clientHolder) {
  def checkStatus(): ElasticsearchHealthStatus =
    elasticClient.execute {
      clusterHealth()
    }.map {
      case _: RequestFailure =>
        ElasticsearchHealthStatus.Unreachable
      case response: RequestSuccess[ClusterHealthResponse] =>
        ElasticsearchHealthStatus(response.result.status)
    }.await

}
