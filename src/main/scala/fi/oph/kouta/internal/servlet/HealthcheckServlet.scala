package fi.oph.kouta.internal.servlet

import fi.oph.kouta.internal.domain.enums.ElasticsearchHealthStatus._
import fi.oph.kouta.internal.elasticsearch.ElasticsearchHealth
import fi.oph.kouta.internal.swagger.SwaggerPaths.registerPath
import org.scalatra._

class HealthcheckServlet extends KoutaServlet {

  registerPath("/healthcheck/",
    s"""    get:
       |      summary: Healthcheck-rajapinta
       |      description: Healthcheck-rajapinta
       |      tags:
       |        - Admin
       |      responses:
       |        '200':
       |          description: Ok
       |""".stripMargin)
  get("/") {
    Ok("message" -> "ok")
  }

  registerPath("/healthcheck/elastic",
    s"""    get:
       |      summary: Tarkista yhteys Elasticsearchiin
       |      description: Tarkista yhteys Elasticsearchiin
       |      tags:
       |        - Admin
       |      responses:
       |        '200':
       |          description: Ok
       |""".stripMargin)
  get("/elastic") {
    ElasticsearchHealth.checkStatus() match {
      case s if s == Unreachable || s == Red =>
        InternalServerError("status" -> s.name)
      case s if s == Yellow || s == Green =>
        Ok("status" -> s.name)
    }
  }
}
