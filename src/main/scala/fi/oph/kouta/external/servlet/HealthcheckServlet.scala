package fi.oph.kouta.external.servlet

import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
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

}
