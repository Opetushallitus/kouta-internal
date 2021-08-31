package fi.oph.kouta.internal.servlet

import fi.oph.kouta.internal.database.SessionDAO
import fi.oph.kouta.internal.service.OdwService
import fi.oph.kouta.internal.swagger.SwaggerPaths.registerPath
import org.scalatra.FutureSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class OdwServlet(odwService: OdwService, val sessionDAO: SessionDAO)
    extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  registerPath(
    "/odw/listHaut",
    """    get:
      |        summary: Listaa julkaistut ja arkistoidut haut
      |        description: Listaa kaikki julkaistut ja/tai arkistoidut haut
      |        operationId: Listaa haut
      |        tags:
      |          - Odw
      |        responses:
      |          '200':
      |            description: Ok
      |            content:
      |              application/json:
      |                schema:
      |                  type: array
      |                  items:
      |                    $ref: '#/components/schemas/Haku'
      |""".stripMargin
  )
  get("/listHaut") {
    odwService.listAllHaut
  }
}

object OdwServlet extends OdwServlet(OdwService, SessionDAO)
