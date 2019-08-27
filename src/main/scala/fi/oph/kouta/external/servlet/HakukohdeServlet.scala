package fi.oph.kouta.external.servlet

import fi.oph.kouta.external.domain.Hakukohde
import fi.oph.kouta.external.domain.oid.HakukohdeOid
import fi.oph.kouta.external.security.Authenticated
import fi.oph.kouta.external.service.HakukohdeService
import org.scalatra.FutureSupport
import org.scalatra.swagger.Swagger

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class HakukohdeServlet(implicit val swagger: Swagger)
  extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  override val applicationDescription = "Hakukohde API"
  override val modelName              = "Hakukohde"

  get(
    "/:oid",
    operation(
      apiOperation[Hakukohde]("Hae Hakukohde")
        tags modelName
        summary "Hae Hakukohde"
        parameter pathParam[String]("oid").description("Hakukohteen oid")
    )
  ) {
    implicit val authenticated: Authenticated = authenticate

    HakukohdeService.get(HakukohdeOid(params("oid")))
  }

  prettifySwaggerModels()

}
