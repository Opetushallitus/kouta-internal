package fi.oph.kouta.internal.servlet

import fi.oph.kouta.internal.database.SessionDAO
import fi.oph.kouta.internal.domain.oid.{HakuOid, OrganisaatioOid}
import fi.oph.kouta.internal.security.Authenticated
import fi.oph.kouta.internal.service.HakuService
import fi.oph.kouta.internal.swagger.SwaggerPaths.registerPath
import org.scalatra.{ActionResult, AsyncResult, FutureSupport, Ok}

import java.util.UUID
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
      |          example: [1.2.246.562.10.00000000001,1.2.246.562.10.00000000002]
      |        - in: query
      |          name: includeHakukohdeOids
      |          schema:
      |            type: boolean
      |          required: false
      |          description: hakukohteen oidit paluuarvoon
      |          example: true
      |        - in: query
      |          name: hakuvuosi
      |          schema:
      |            type: integer
      |          required: false
      |          description: Haun hakuajan vuosi
      |          example: 2022
      |        - in: query
      |          name: koulutuksenAlkamisvuosi
      |          schema:
      |            type: integer
      |          required: false
      |          description: Koulutuksen alkamisvuosi
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
      |        '400':
      |          description: Virheellinen parametri tai poistettu vuosi-parametri (käytä hakuvuosi tai koulutuksenAlkamisvuosi)
      |""".stripMargin
  )
  get("/search") {
    implicit val authenticated: Authenticated = authenticate

    //Vuosi yhdisti ennen hakuvuoden ja koulutuksen alkamisvuoden. Nämä on nyt erotettu eri parametreiksi,
    //jotta kutsuja voi paremmin rajata haun palauttamaa sisältöä.
    if (params.contains("vuosi"))
      throw new IllegalArgumentException("vuosi is not supported, use hakuvuosi or koulutuksenAlkamisvuosi")

    val ataruId = parseParam("ataruId")(UUID.fromString(_).toString)
    val tarjoaja: Option[Set[OrganisaatioOid]] =
      multiParams.get("tarjoaja").map(_.filter(_.nonEmpty).flatMap(_.split(",")).map(OrganisaatioOid).toSet)
    tarjoaja.flatMap(_.find(!_.isValid())).foreach(oid => throw new IllegalArgumentException(s"Invalid tarjoaja $oid"))
    val hakuvuosi               = parseParam("hakuvuosi")(_.toInt)
    val koulutuksenAlkamisvuosi = parseParam("koulutuksenAlkamisvuosi")(_.toInt)
    val includeHakukohdeOids    = parseParam("includeHakukohdeOids")(_.toBoolean).getOrElse(false)

    logger.debug(
      s"Request: /haku/search | ataruId: ${ataruId} | tarjoaja: ${tarjoaja} | " +
        s"hakuvuosi: ${hakuvuosi} | koulutuksenAlkamisvuosi: ${koulutuksenAlkamisvuosi}"
    )

    new AsyncResult() {
      override implicit def timeout: Duration = 5.minutes

      override val is: Future[ActionResult] =
        hakuService
          .search(ataruId, tarjoaja, hakuvuosi, koulutuksenAlkamisvuosi, includeHakukohdeOids)
          .map(Ok(_))
    }

  }
}

object HakuServlet extends HakuServlet(HakuService, SessionDAO)
