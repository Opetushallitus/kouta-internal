package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.cluster.ClusterHealthResponse
import com.sksamuel.elastic4s.http.{RequestFailure, RequestSuccess}
import fi.oph.kouta.internal.domain.enums.ElasticsearchHealthStatus
import fi.oph.kouta.internal.util.KoutaJsonFormats
import org.json4s.jackson.Serialization.write

import scala.concurrent.ExecutionContext.Implicits.global

object ElasticsearchHealth extends ElasticsearchHealth(DefaultElasticsearchClientHolder)

abstract class ElasticsearchHealth(clientHolder: ElasticsearchClientHolder)
    extends ElasticsearchClient("health", "health", clientHolder)
    with KoutaJsonFormats {
  def checkStatus(): ElasticsearchHealthStatus =
    elasticClient.execute {
      clusterHealth()
    }.map {
      case e: RequestFailure =>
        logger.error(s"Elasticsearch error: ${write(e.error)}")
        ElasticsearchHealthStatus.Unreachable
      case response: RequestSuccess[ClusterHealthResponse] =>
        ElasticsearchHealthStatus(response.result.status)
    }.await

}
