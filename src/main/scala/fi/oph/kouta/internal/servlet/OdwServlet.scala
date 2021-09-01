package fi.oph.kouta.internal.servlet

import fi.oph.kouta.internal.database.SessionDAO
import fi.oph.kouta.internal.domain.oid.HakuOid
import fi.oph.kouta.internal.security.Authenticated
import fi.oph.kouta.internal.service.OdwService
import fi.oph.kouta.internal.swagger.SwaggerPaths.registerPath
import org.scalatra.{BadRequest, FutureSupport}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

class OdwServlet(odwService: OdwService, val sessionDAO: SessionDAO)
    extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

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

  registerPath(
    "/odw/listHakuOids",
    """    get:
      |        summary: Listaa julkaistujen ja arkistoitujen hakujen OIDit
      |        description: Listaa kaikkien julkaistujen ja/tai arkistoitujen hakujen OIDit
      |        operationId: Listaa hakujen OIDit
      |        tags:
      |          - Odw
      |        responses:
      |          '200':
      |            description: Ok
      |            content:
      |              application/json:
      |                schema:
      |                  type: array
      |                  items: string
      |                  example: ["1.2.246.562.29.00000000000000000071", "1.2.246.562.29.00000000000000001183"]
      |""".stripMargin
  )
  get("/listHakuOids") {
    implicit val authenticated: Authenticated = authenticate

    val offset = Try(params("offset").toInt).toOption
    val limit  = Try(params("limit").toInt).toOption

    odwService.listAllHakuOids(offset.getOrElse(0), limit)
  }

  registerPath(
    "/odw/findHautByOids",
    """    post:
      |      summary: Etsi hakuja oideilla
      |      operationId: Etsi hakuja oideilla
      |      description: Etsii hakuja annetuilla oideilla
      |      tags:
      |        - Odw
      |      requestBody:
      |          description: Palautettavien hakujen oidit JSON-arrayna
      |          example: ["1.2.246.562.29.00000000000000000071", "1.2.246.562.29.00000000000000001183"]
      |          content:
      |             application/json:
      |               schema:
      |                 type: array
      |                 items:
      |                   type: string
      |                   example: ["1.2.246.562.29.00000000000000000071", "1.2.246.562.29.00000000000000001183"]
      |      responses:
      |        '200':
      |          description: Ok
      |          content:
      |            application/json:
      |              schema:
      |                type: array
      |                items:
      |                  $ref: '#/components/schemas/Haku'
      |""".stripMargin
  )
  post("/findHautByOids") {
    implicit val authenticated: Authenticated = authenticate
    val hakuOids                              = parsedBody.extract[Set[HakuOid]]

    if (hakuOids.exists(!_.isValid())) {
      BadRequest(s"Invalid hakuOids ${hakuOids.find(!_.isValid()).get.toString}")
    } else {
      odwService.findByOids(hakuOids)
    }
  }
}

object OdwServlet extends OdwServlet(OdwService, SessionDAO)
