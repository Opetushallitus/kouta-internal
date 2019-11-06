package fi.oph.kouta.internal.servlet

import java.util.UUID

import fi.oph.kouta.internal.security.Authenticated
import fi.oph.kouta.internal.service.ValintaperusteService
import fi.oph.kouta.internal.swagger.SwaggerPaths.registerPath
import org.scalatra.FutureSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class ValintaperusteServlet
    extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  registerPath("/valintaperuste/{id}",
    """    get:
      |      summary: Hae valintaperustekuvauksen tiedot
      |      operationId: Hae valintaperuste
      |      description: Hakee valintaperustekuvauksen kaikki tiedot
      |      tags:
      |        - Valintaperuste
      |      parameters:
      |        - in: path
      |          name: oid
      |          schema:
      |            type: string
      |          required: true
      |          description: Valintaperuste-id
      |          example: ea596a9c-5940-497e-b5b7-aded3a2352a7
      |      responses:
      |        '200':
      |          description: Ok
      |          content:
      |            application/json:
      |              schema:
      |                $ref: '#/components/schemas/Valintaperuste'
      |""".stripMargin)
  get("/:id") {
    implicit val authenticated: Authenticated = authenticate

    ValintaperusteService.get(UUID.fromString(params("id")))
  }
}
