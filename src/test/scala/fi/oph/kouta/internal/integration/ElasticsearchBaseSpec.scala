package fi.oph.kouta.internal.integration

import fi.oph.kouta.internal.TempElasticDockerClient
import fi.oph.kouta.internal.domain.enums.ElasticsearchHealthStatus
import fi.oph.kouta.internal.elasticsearch.ElasticsearchHealth
import org.scalatra.test.scalatest.ScalatraFlatSpec

class ElasticsearchBaseSpec extends ScalatraFlatSpec {
  "Tests" should "connect to elasticsearch" in {
    new ElasticsearchHealth(TempElasticDockerClient.client).checkStatus().healthy should be(true)
  }
}
