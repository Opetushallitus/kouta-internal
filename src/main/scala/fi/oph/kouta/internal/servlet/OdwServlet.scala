package fi.oph.kouta.internal.servlet

import fi.oph.kouta.internal.database.SessionDAO
import fi.oph.kouta.internal.security.Authenticated
import fi.oph.kouta.internal.service.OdwService
import fi.oph.kouta.internal.swagger.SwaggerPaths.registerPath
import org.scalatra.FutureSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

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
    implicit val authenticated: Authenticated = authenticate

    val offset = Try(params("offset").toInt).toOption
    val limit  = Try(params("limit").toInt).toOption

    odwService.listAllHaut(offset.getOrElse(0), limit)
  }
}

object OdwServlet extends OdwServlet(OdwService, SessionDAO)

//parameters:
//  - in: query
//  name: offset
//  schema:
//  type: integer
//  required: false
//  description: Haluttu aloituskohta (indeksi) hakutuloksissa, esim. käytettäessä sivutusta.
//  example: 10
//  - in: query
//  name: limit
//  schema:
//  type: integer
//  required: false
//  description: Hakutulosten maksimimäärä, tarpeellinen esim. käytettäessä sivutusta
//  example: 100
