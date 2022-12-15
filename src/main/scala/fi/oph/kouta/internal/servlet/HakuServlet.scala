package fi.oph.kouta.internal.servlet

import fi.oph.kouta.internal.database.SessionDAO
import fi.oph.kouta.internal.domain.oid.{HakuOid, OrganisaatioOid}
import fi.oph.kouta.internal.security.Authenticated
import fi.oph.kouta.internal.service.HakuService
import fi.oph.kouta.internal.swagger.SwaggerPaths.registerPath
import org.scalatra.{ActionResult, AsyncResult, BadRequest, FutureSupport, Ok}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, DurationInt}

class HakuServlet(hakuService: HakuService, val sessionDAO: SessionDAO)
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
    val hakuOid = HakuOid(params("oid"))
    hakuOid.validateLength()

    implicit val authenticated: Authenticated = authenticate

    hakuService.get(hakuOid)
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
      |          required: false
      |          description: Ataru-lomakkeen id
      |          example: 66b7b709-1ed0-49cc-bbef-e5b0420a81c9
      |        - in: query
      |          name: tarjoaja
      |          schema:
      |            type: array
      |            items:
      |              type: string
      |          required: false
      |          description: Organisaatio joka on haun hakukohteen tarjoaja
      |          example: 1.2.246.562.10.00000000001,1.2.246.562.10.00000000002
      |        - in: query
      |          name: includeHakukohdeOids
      |          schema:
      |            type: boolean
      |          required: false
      |          description: hakukohteen oidit paluuarvoon
      |          example: true
      |        - in: query
      |          name: vuosi
      |          schema:
      |            type: integer
      |          required: false
      |          description: viimeiseksi alkaneen hakuajan vuosi tai koulutuksen alkamisvuosi
      |          example: 2022
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

    val ataruId  = params.get("ataruId")
    val tarjoaja = params.get("tarjoaja").map(_.split(",").map(OrganisaatioOid).toSet)
    val vuosi    = params.getAs[Int]("vuosi")
    val includeHakukohdeOids = params.get("includeHakukohdeOids").exists {
      case "true"  => true
      case "false" => false
    }

    logger.debug(s"Request: /haku/search | ataruId: ${ataruId} | tarjoaja: ${tarjoaja}")

    new AsyncResult() {
      override implicit def timeout: Duration = 5.minutes

      override val is: Future[ActionResult] = tarjoaja match {
        case Some(oids) if oids.exists(!_.isValid) =>
          Future.successful(BadRequest(s"Invalid tarjoaja ${oids.find(!_.isValid()).get.toString}"))
        case tarjoaja => hakuService.search(ataruId, tarjoaja, vuosi, includeHakukohdeOids).map(Ok(_))
      }
    }

  }
}

object HakuServlet extends HakuServlet(HakuService, SessionDAO)
