package fi.oph.kouta.external.integration

import fi.oph.kouta.external.KoutaBackendSwagger
import fi.oph.kouta.external.servlet.HealthcheckServlet
import org.scalatra.test.scalatest.ScalatraFlatSpec

class HealthcheckServletSpec extends ScalatraFlatSpec {
  implicit val swagger: KoutaBackendSwagger = new KoutaBackendSwagger

  addServlet(new HealthcheckServlet(), "/healthcheck")

  "Healthcheck" should "return 200" in {
    get("/healthcheck") {
      status should equal(200)
      body should equal("{\"message\":\"ok\"}")
    }
  }
}
