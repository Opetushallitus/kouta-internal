package fi.oph.kouta.internal.servlet

import fi.oph.kouta.internal.database.SessionDAO
import fi.oph.kouta.internal.service.OdwService
import org.scalatra.FutureSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class OdwServlet(odwService: OdwService,  val sessionDAO: SessionDAO)
    extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  //registerPath("/odw/listHaut", "")
  get("/listHaut") {
    odwService.listAllHaut
  }
}

object OdwServlet extends OdwServlet(OdwService, SessionDAO)
