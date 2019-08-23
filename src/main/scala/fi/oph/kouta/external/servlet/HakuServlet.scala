package fi.oph.kouta.external.servlet

import fi.oph.kouta.external.domain.Haku
import fi.oph.kouta.external.domain.oid.HakuOid
import fi.oph.kouta.external.security.Authenticated
import fi.oph.kouta.external.service.HakuService
import org.scalatra.FutureSupport
import org.scalatra.swagger.Swagger

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class HakuServlet(implicit val swagger: Swagger)
  extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  override val applicationDescription = "Haku API"
  override val modelName              = "Haku"

  get(
    "/:oid",
    operation(
      apiOperation[Haku]("Hae haku")
        tags modelName
        summary "Hae haku"
        parameter pathParam[String]("oid").description("Haun oid")
    )
  ) {
    implicit val authenticated: Authenticated = authenticate

    HakuService.get(HakuOid(params("oid")))
  }

  prettifySwaggerModels()

}
