package fi.oph.kouta.external.servlet

import fi.oph.kouta.external.domain.oid.HakuOid
import fi.oph.kouta.external.security.Authenticated
import fi.oph.kouta.external.service.HakuService
import fi.oph.kouta.external.swagger.SwaggerPaths.registerPath
import org.scalatra.FutureSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class HakuServlet
  extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  registerPath("/haku/{oid}",
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
      |""".stripMargin)
  get("/:oid") {
    implicit val authenticated: Authenticated = authenticate

    HakuService.get(HakuOid(params("oid")))
  }

}
