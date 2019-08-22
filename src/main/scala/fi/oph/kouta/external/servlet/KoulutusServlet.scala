package fi.oph.kouta.external.servlet

import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.domain.oid.KoulutusOid
import fi.oph.kouta.external.security.Authenticated
import fi.oph.kouta.external.service.KoulutusService
import org.scalatra.FutureSupport
import org.scalatra.swagger.Swagger

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class KoulutusServlet(implicit val swagger: Swagger)
    extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  override val applicationDescription = "Koulutus API"
  override val modelName              = "Koulutus"

  get(
    "/:oid",
    operation(
      apiOperation[Koulutus]("Hae koulutus")
        tags modelName
        summary "Hae koulutus"
        parameter pathParam[String]("oid").description("Koulutuksen oid")
    )
  ) {
    implicit val authenticated: Authenticated = authenticate

    KoulutusService.get(KoulutusOid(params("oid")))
  }

  prettifySwaggerModels()

}
