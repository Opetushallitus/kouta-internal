package fi.oph.kouta.external.servlet

import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.domain.oid.KoulutusOid
import fi.oph.kouta.external.service.KoulutusService
import org.scalatra.FutureSupport
import org.scalatra.swagger._

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class KoulutusServlet(implicit val swagger: Swagger) extends KoutaServlet with SwaggerSupport with FutureSupport {

  override def executor: ExecutionContext = global

  override val applicationDescription = "Koulutus API"
  val modelName = "Koulutus"

  get("/:oid", operation(apiOperation[Koulutus]("Hae koulutus")
    tags modelName
    summary "Hae koulutus"
    parameter pathParam[String]("oid").description("Koulutuksen oid"))) {

    KoulutusService.getKoulutus(KoulutusOid(params("oid")))
  }
}
