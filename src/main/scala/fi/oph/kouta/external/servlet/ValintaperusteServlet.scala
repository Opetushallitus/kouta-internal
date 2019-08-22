package fi.oph.kouta.external.servlet

import java.util.UUID

import fi.oph.kouta.external.domain.Valintaperuste
import fi.oph.kouta.external.security.Authenticated
import fi.oph.kouta.external.service.ValintaperusteService
import org.scalatra.FutureSupport
import org.scalatra.swagger.Swagger

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class ValintaperusteServlet(implicit val swagger: Swagger)
    extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  override val applicationDescription = "Valintaperuste API"
  override val modelName              = "Valintaperuste"

  get(
    "/:id",
    operation(
      apiOperation[Valintaperuste]("Hae valintaperuste")
        tags modelName
        summary "Hae valintaperuste"
        parameter pathParam[String]("id").description("Valintaperusteen UUID")
    )
  ) {
    implicit val authenticated: Authenticated = authenticate

    ValintaperusteService.get(UUID.fromString(params("id")))
  }

  prettifySwaggerModels()
}
