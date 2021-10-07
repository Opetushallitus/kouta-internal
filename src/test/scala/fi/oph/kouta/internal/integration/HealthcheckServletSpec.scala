package fi.oph.kouta.internal.integration

import fi.oph.kouta.internal.TempElasticClient
import fi.oph.kouta.internal.elasticsearch.ElasticsearchHealth
import fi.oph.kouta.internal.servlet.HealthcheckServlet
import org.scalatra.test.scalatest.ScalatraFlatSpec

class HealthcheckServletSpec extends ScalatraFlatSpec {
  addServlet(new HealthcheckServlet(new ElasticsearchHealth(TempElasticClient.client)), "/healthcheck")

  "Healthcheck" should "return 200" in {
    get("/healthcheck") {
      status should equal(200)
      body should equal("{\"message\":\"ok\"}")
    }
  }
}
