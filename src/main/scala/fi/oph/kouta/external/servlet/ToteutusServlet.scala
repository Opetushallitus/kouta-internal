package fi.oph.kouta.external.servlet

import fi.oph.kouta.external.domain.Toteutus
import fi.oph.kouta.external.domain.oid.ToteutusOid
import fi.oph.kouta.external.security.Authenticated
import fi.oph.kouta.external.service.ToteutusService
import org.scalatra.FutureSupport
import org.scalatra.swagger.Swagger

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class ToteutusServlet(implicit val swagger: Swagger)
  extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  override val applicationDescription = "Toteutus API"
  override val modelName              = "Toteutus"

  get(
    "/:oid",
    operation(
      apiOperation[Toteutus]("Hae toteutus")
        tags modelName
        summary "Hae toteutus"
        parameter pathParam[String]("oid").description("Toteutuksen oid")
    )
  ) {
    implicit val authenticated: Authenticated = authenticate

    ToteutusService.get(ToteutusOid(params("oid")))
  }

  prettifySwaggerModels()

}
