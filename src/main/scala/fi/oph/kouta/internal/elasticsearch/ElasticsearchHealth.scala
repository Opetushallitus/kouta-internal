package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.cluster.ClusterHealthResponse
import com.sksamuel.elastic4s.http.{ElasticClient, RequestFailure, RequestSuccess}
import fi.oph.kouta.internal.domain.enums.ElasticsearchHealthStatus
import fi.oph.kouta.internal.util.KoutaJsonFormats
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.jackson.Serialization.write

import scala.concurrent.ExecutionContext.Implicits.global

class ElasticsearchHealth(client: ElasticClient) extends KoutaJsonFormats with Logging {
  def checkStatus(): ElasticsearchHealthStatus =
    client
      .execute(clusterHealth())
      .map {
        case e: RequestFailure =>
          logger.error(s"Elasticsearch error: ${write(e.error)}")
          ElasticsearchHealthStatus.Unreachable
        case response: RequestSuccess[ClusterHealthResponse] =>
          ElasticsearchHealthStatus(response.result.status)
      }
      .await
}

object ElasticsearchHealth extends ElasticsearchHealth(ElasticsearchClient.client)
