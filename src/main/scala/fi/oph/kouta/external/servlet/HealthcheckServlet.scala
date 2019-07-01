package fi.oph.kouta.external.servlet

import org.scalatra._
import org.scalatra.swagger.{Swagger, _}

class HealthcheckServlet(implicit val swagger: Swagger) extends KoutaServlet with SwaggerSupport {
  
  override val applicationDescription = "Healthcheck API"

  get("/", operation(apiOperation[String]("Healthcheck") summary "Healthcheck" tags "Admin")) {
    Ok("message" -> "ok")
  }

}


