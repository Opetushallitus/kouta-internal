package fi.oph.kouta.internal.integration

import fi.oph.kouta.internal.TempElasticClient
import fi.oph.kouta.internal.elasticsearch.ElasticsearchHealth
import org.scalatra.test.scalatest.ScalatraFlatSpec

class ElasticsearchBaseSpec extends ScalatraFlatSpec {
  "Tests" should "connect to elasticsearch" in {
    new ElasticsearchHealth(TempElasticClient.client).checkStatus().healthy should be(true)
  }
}
