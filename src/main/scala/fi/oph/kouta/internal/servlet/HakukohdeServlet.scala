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
      |          required: false
      |          description: Haun-oid
      |          example: 1.2.246.562.29.00000000000000000009
      |        - in: query
      |          name: tarjoaja
      |          schema:
      |            type: array
      |            items:
      |              type: string
      |          required: false
      |          description: Organisaatio joka on hakukohteen tarjoaja
      |          example: 1.2.246.562.10.00000000001,1.2.246.562.10.00000000002
      |        - in: query
      |          name: q
      |          schema:
      |            type: string
      |          required: false
      |          description: Tekstihaku hakukohteen ja sen järjestyspaikan nimeen
      |          example: Autoalan perustutkinto
      |        - in: query
      |          name: all
      |          schema:
      |            type: boolean
      |          required: false
      |          description: Haetaanko myös muiden, kuin annettujen tarjoajien hakukohteet
      |          example: true
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
    val tarjoaja = params.get("tarjoaja").map(s => s.split(",").map(OrganisaatioOid).toSet)
    val q        = params.get("q")
    val all = params.get("all").exists {
      case "true"  => true
      case "false" => false
    }

    (hakuOid, tarjoaja) match {
      case (None, None)                     => BadRequest("Query parameter is required")
      case (Some(oid), _) if !oid.isValid() => BadRequest(s"Invalid haku ${oid.toString}")
      case (_, Some(oids)) if oids.exists(!_.isValid()) =>
        BadRequest(s"Invalid tarjoaja ${oids.find(!_.isValid()).get.toString}")
      case (hakuOid, tarjoajaOids) => hakukohdeService.search(hakuOid, tarjoajaOids, q, all)
    }
  }

  registerPath(
    "/hakukohde/findbyoids",
    """    post:
      |      summary: Etsi hakukohteita oideilla
      |      operationId: Etsi hakukohteita oideilla
      |      description: Etsii hakukohteista annetuilla oideilla
      |      tags:
      |        - Hakukohde
      |      parameters:
      |        - in: query
      |          name: tarjoaja
      |          schema:
      |            type: array
      |            items:
      |              type: string
      |          required: false
      |          description: Organisaatio joka on hakukohteen tarjoaja
      |          example: 1.2.246.562.10.00000000001,1.2.246.562.10.00000000002
      |      requestBody:
      |          description: Palautettavien hakukohteiden oidit JSON-arrayna
      |          example: ["1.2.246.562.10.00000000001","1.2.246.562.10.00000000002"]
      |          content:
      |             application/json:
      |               schema:
      |                 type: array
      |                 items:
      |                   type: string
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
  post("/findbyoids") {
    implicit val authenticated: Authenticated = authenticate

    val tarjoaja  = params.get("tarjoaja").map(s => s.split(",").map(OrganisaatioOid).toSet)
    val hakukohde = parsedBody.extract[Set[HakukohdeOid]]

    (tarjoaja, hakukohde) match {
      case (Some(oids), _) if oids.exists(!_.isValid()) =>
        BadRequest(s"Invalid tarjoaja ${oids.find(!_.isValid()).get.toString}")
      case (_, oids) if oids.exists(!_.isValid()) =>
        BadRequest(s"Invalid hakukohdeOids ${oids.find(!_.isValid()).get.toString}")
      case (tarjoajaOids, hakukohdeOids) => hakukohdeService.findByOids(tarjoajaOids, hakukohdeOids)
    }
  }
}

object HakukohdeServlet extends HakukohdeServlet(HakukohdeService, SessionDAO)
