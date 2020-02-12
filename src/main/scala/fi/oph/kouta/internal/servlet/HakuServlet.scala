package fi.oph.kouta.internal.servlet

import fi.oph.kouta.internal.domain.oid.HakuOid
import fi.oph.kouta.internal.security.Authenticated
import fi.oph.kouta.internal.service.HakuService
import fi.oph.kouta.internal.swagger.SwaggerPaths.registerPath
import org.scalatra.{BadRequest, FutureSupport}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class HakuServlet(hakuService: HakuService)
    extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  registerPath(
    "/haku/{oid}",
    """    get:
      |      summary: Hae haun tiedot
      |      operationId: Hae haku
      |      description: Hakee haun kaikki tiedot
      |      tags:
      |        - Haku
      |      parameters:
      |        - in: path
      |          name: oid
      |          schema:
      |            type: string
      |          required: true
      |          description: Haku-oid
      |          example: 1.2.246.562.29.00000000000000000009
      |      responses:
      |        '200':
      |          description: Ok
      |          content:
      |            application/json:
      |              schema:
      |                $ref: '#/components/schemas/Haku'
      |""".stripMargin
  )
  get("/:oid") {
    implicit val authenticated: Authenticated = authenticate

    hakuService.get(HakuOid(params("oid")))
  }

  registerPath(
    "/haku/search",
    """    get:
      |      summary: Etsi hakuja
      |      operationId: Etsi hakuja
      |      description: Etsii hauista annetuilla ehdoilla
      |      tags:
      |        - Haku
      |      parameters:
      |        - in: query
      |          name: ataruId
      |          schema:
      |            type: string
      |          required: true
      |          description: Ataru-lomakkeen id
      |          example: 66b7b709-1ed0-49cc-bbef-e5b0420a81c9
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
  get("/search") {
    implicit val authenticated: Authenticated = authenticate

    params.get("ataruId") match {
      case None => BadRequest("Query parameter ataruId is required")
      case Some(id) => hakuService.searchByAtaruId(id)
    }
  }
}
