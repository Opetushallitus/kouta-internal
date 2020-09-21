package fi.oph.kouta.internal.servlet

import fi.oph.kouta.internal.database.SessionDAO
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid}
import fi.oph.kouta.internal.security.Authenticated
import fi.oph.kouta.internal.service.HakukohdeService
import fi.oph.kouta.internal.swagger.SwaggerPaths.registerPath
import org.scalatra.{BadRequest, FutureSupport}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class HakukohdeServlet(hakukohdeService: HakukohdeService, val sessionDAO: SessionDAO)
    extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  registerPath(
    "/hakukohde/{oid}",
    """    get:
      |      summary: Hae hakukohteen tiedot
      |      operationId: Hae hakukohde
      |      description: Hakee hakukohteen kaikki tiedot
      |      tags:
      |        - Hakukohde
      |      parameters:
      |        - in: path
      |          name: oid
      |          schema:
      |            type: string
      |          required: true
      |          description: Hakukohde-oid
      |          example: 1.2.246.562.20.00000000000000000009
      |      responses:
      |        '200':
      |          description: Ok
      |          content:
      |            application/json:
      |              schema:
      |                $ref: '#/components/schemas/Hakukohde'
      |""".stripMargin
  )
  get("/:oid") {
    implicit val authenticated: Authenticated = authenticate

    hakukohdeService.get(HakukohdeOid(params("oid")))
  }

  registerPath(
    "/hakukohde/search",
    """    get:
      |      summary: Etsi hakukohteita
      |      operationId: Etsi hakukohteita
      |      description: Etsii hakukohteista annetuilla ehdoilla
      |      tags:
      |        - Hakukohde
      |      parameters:
      |        - in: query
      |          name: haku
      |          schema:
      |            type: string
      |          required: true
      |          description: Haun-oid
      |          example: 1.2.246.562.29.00000000000000000009
      |        - in: query
      |          name: tarjoaja
      |          schema:
      |            type: string
      |          required: false
      |          description: Organisaatio joka on hakukohteen tarjoaja
      |          example: 1.2.246.562.10.00000000001
      |      responses:
      |        '200':
      |          description: Ok
      |          content:
      |            application/json:
      |              schema:
      |                type: array
      |                items:
      |                  $ref: '#/components/schemas/Hakukohde'
      |""".stripMargin
  )
  get("/search") {
    implicit val authenticated: Authenticated = authenticate

    val hakuOid  = params.get("haku").map(HakuOid)
    val tarjoaja = params.get("tarjoaja").map(OrganisaatioOid)

    (hakuOid, tarjoaja) match {
      case (None, None)                     => BadRequest("Query parameter is required")
      case (Some(oid), _) if !oid.isValid() => BadRequest(s"Invalid haku ${oid.toString}")
      case (_, Some(oid)) if !oid.isValid() => BadRequest(s"Invalid tarjoaja ${oid.toString}")
      case (hakuOid, tarjoajaOid)           => hakukohdeService.searchByHakuAndTarjoaja(hakuOid, tarjoajaOid)
    }
  }
}

object HakukohdeServlet extends HakukohdeServlet(HakukohdeService, SessionDAO)
