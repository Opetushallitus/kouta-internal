package fi.oph.kouta.internal.servlet

import fi.oph.kouta.internal.database.SessionDAO
import fi.oph.kouta.internal.domain.Haku
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid, KoulutusOid, ToteutusOid}
import fi.oph.kouta.internal.security.Authenticated
import fi.oph.kouta.internal.service.OdwService
import fi.oph.kouta.internal.swagger.SwaggerPaths.registerPath
import org.scalatra.{BadRequest, FutureSupport}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
      |        operationId: odwListHakuOids
      |        tags:
      |          - Odw
      |        parameters:
      |          - in: query
      |            name: fromDate
      |            schema:
      |              type: string
      |            required: false
      |            description: Listaa ainoastaan ko. päivämäärän jälkeen muutetut
      |            example: 2021-09-13
      |        responses:
      |          '200':
      |            description: Ok
      |            content:
      |              application/json:
      |                schema:
      |                  type: array
      |                  items:
      |                     type: string
      |                  example: ["1.2.246.562.29.00000000000000000071", "1.2.246.562.29.00000000000000001183"]
      |""".stripMargin
  )
  get("/listHakuOids") {
    implicit val authenticated: Authenticated = authenticate

    val modifiedDateStartFrom =
      Try(LocalDate.parse(params("fromDate"), DateTimeFormatter.ofPattern("yyyy-MM-dd"))).toOption
    val offset = Try(params("offset").toInt).toOption
    val limit  = Try(params("limit").toInt).toOption

    odwService.listAllHakuOids(modifiedDateStartFrom, offset.getOrElse(0), limit)
  }

  registerPath(
    "/odw/findHautByOids",
    """    post:
      |      summary: Etsi hakuja oideilla
      |      operationId: odwFindHautByOids
      |      description: Etsii hakuja annetuilla oideilla
      |      tags:
      |        - Odw
      |      requestBody:
      |          description: Palautettavien hakujen oidit JSON-arrayna
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
      |                  $ref: '#/components/schemas/OdwHaku'
      |""".stripMargin
  )
  post("/findHautByOids") {
    implicit val authenticated: Authenticated = authenticate
    val hakuOids                              = parsedBody.extract[Set[HakuOid]]

    hakuOids match {
      case (oids) if (oids.exists(!_.isValid())) =>
        BadRequest(s"Invalid hakuOids ${oids.find(!_.isValid()).get.toString}")
      case (oids) =>
        odwService
          .findOdwHautByOids(oids)
          .map(_.map(h => {
            h.copy(
              alkamiskausiKoodiUri = h.alkamiskausiKoodiUri.map(_ + "#1"),
              metadata = h.metadata.map(m =>
                m.copy(
                  koulutuksenAlkamiskausi = m.koulutuksenAlkamiskausi.map(k =>
                    k.copy(
                      koulutuksenAlkamiskausi =
                        k.koulutuksenAlkamiskausi.map(k2 => k2.copy(koodiUri = k2.koodiUri + "#1"))
                    )
                  )
                )
              )
            )
          }))
    }
  }

  registerPath(
    "/odw/listHakukohdeOids",
    """    get:
      |        summary: Listaa julkaistujen ja arkistoitujen hakukohteiden OIDit
      |        description: Listaa kaikkien julkaistujen ja/tai arkistoitujen hakukohteiden OIDit
      |        operationId: odwListHakukohdeOids
      |        tags:
      |          - Odw
      |        parameters:
      |          - in: query
      |            name: fromDate
      |            schema:
      |              type: string
      |            required: false
      |            description: Listaa ainoastaan ko. päivämäärän jälkeen muutetut
      |            example: 2021-09-13
      |        responses:
      |          '200':
      |            description: Ok
      |            content:
      |              application/json:
      |                schema:
      |                  type: array
      |                  items:
      |                    type: string
      |                  example: ["1.2.246.562.20.00000000000000002074", "1.2.246.562.20.00000000000000002142"]
      |""".stripMargin
  )
  get("/listHakukohdeOids") {
    implicit val authenticated: Authenticated = authenticate

    val modifiedDateStartFrom =
      Try(LocalDate.parse(params("fromDate"), DateTimeFormatter.ofPattern("yyyy-MM-dd"))).toOption
    val offset = Try(params("offset").toInt).toOption
    val limit  = Try(params("limit").toInt).toOption

    odwService.listAllHakukohdeOids(modifiedDateStartFrom, offset.getOrElse(0), limit)
  }

  registerPath(
    "/odw/findHakukohteetByOids",
    """    post:
      |      summary: Etsi hakukohteita oideilla
      |      operationId: odwFindHakukohteetByOids
      |      description: Etsii hakukohteita annetuilla oideilla
      |      tags:
      |        - Odw
      |      requestBody:
      |          description: Palautettavien hakukohteiden oidit JSON-arrayna
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
  post("/findHakukohteetByOids") {
    implicit val authenticated: Authenticated = authenticate
    val hakukohdeOids                         = parsedBody.extract[Set[HakukohdeOid]]

    hakukohdeOids match {
      case (oids) if (oids.exists(!_.isValid())) =>
        BadRequest(s"Invalid hakukohdeOids ${oids.find(!_.isValid()).get.toString}")
      case (oids) => odwService.findHakukohteetByOids(oids)
    }
  }

  registerPath(
    "/odw/listKoulutusOids",
    """    get:
      |        summary: Listaa julkaistujen ja arkistoitujen koulutusten OIDit
      |        description: Listaa kaikkien julkaistujen ja/tai arkistoitujen koulutusten OIDit
      |        operationId: odwListKoulutusOids
      |        tags:
      |          - Odw
      |        parameters:
      |          - in: query
      |            name: fromDate
      |            schema:
      |              type: string
      |            required: false
      |            description: Listaa ainoastaan ko. päivämäärän jälkeen muutetut
      |            example: 2021-09-13
      |        responses:
      |          '200':
      |            description: Ok
      |            content:
      |              application/json:
      |                schema:
      |                  type: array
      |                  items:
      |                    type: string
      |                  example: ["1.2.246.562.13.00000000000000000445", "1.2.246.562.13.00000000000000000505"]
      |""".stripMargin
  )
  get("/listKoulutusOids") {
    implicit val authenticated: Authenticated = authenticate

    val modifiedDateStartFrom =
      Try(LocalDate.parse(params("fromDate"), DateTimeFormatter.ofPattern("yyyy-MM-dd"))).toOption
    val offset = Try(params("offset").toInt).toOption
    val limit  = Try(params("limit").toInt).toOption

    odwService.listAllKoulutusOids(modifiedDateStartFrom, offset.getOrElse(0), limit)
  }

  registerPath(
    "/odw/findKoulutuksetByOids",
    """    post:
      |      summary: Etsi koulutuksia oideilla
      |      operationId: odwFindKoulutuksetByOids
      |      description: Etsii koulutuksia annetuilla oideilla
      |      tags:
      |        - Odw
      |      requestBody:
      |          description: Palautettavien koulutusten oidit JSON-arrayna
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
      |                  $ref: '#/components/schemas/OdwKoulutus'
      |""".stripMargin
  )
  post("/findKoulutuksetByOids") {
    implicit val authenticated: Authenticated = authenticate
    val koulutusOids                          = parsedBody.extract[Set[KoulutusOid]]
    koulutusOids match {
      case (oids) if (oids.exists(!_.isValid())) =>
        BadRequest(s"Invalid koulutusOids ${oids.find(!_.isValid()).get.toString}")
      case (oids) => odwService.findKoulutuksetByOids(oids)
    }
  }

  registerPath(
    "/odw/listToteutusOids",
    """    get:
      |        summary: Listaa julkaistujen ja arkistoitujen toteutusten OIDit
      |        description: Listaa kaikkien julkaistujen ja/tai arkistoitujen toteutusten OIDit
      |        operationId: odwListToteutusOids
      |        tags:
      |          - Odw
      |        parameters:
      |          - in: query
      |            name: fromDate
      |            schema:
      |              type: string
      |            required: false
      |            description: Listaa ainoastaan ko. päivämäärän jälkeen muutetut
      |            example: 2021-09-13
      |        responses:
      |          '200':
      |            description: Ok
      |            content:
      |              application/json:
      |                schema:
      |                  type: array
      |                  items:
      |                    type: string
      |                  example: ["1.2.246.562.17.00000000000000000267", "1.2.246.562.17.00000000000000000772"]
      |""".stripMargin
  )
  get("/listToteutusOids") {
    implicit val authenticated: Authenticated = authenticate

    val modifiedDateStartFrom =
      Try(LocalDate.parse(params("fromDate"), DateTimeFormatter.ofPattern("yyyy-MM-dd"))).toOption
    val offset = Try(params("offset").toInt).toOption
    val limit  = Try(params("limit").toInt).toOption

    odwService.listAllToteutusOids(modifiedDateStartFrom, offset.getOrElse(0), limit)
  }

  registerPath(
    "/odw/findToteutuksetByOids",
    """    post:
      |      summary: Etsi toteutuksia oideilla
      |      operationId: odwFindToteutuksetByOids
      |      description: Etsii toteutuksia annetuilla oideilla
      |      tags:
      |        - Odw
      |      requestBody:
      |          description: Palautettavien toteutusten oidit JSON-arrayna
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
      |                  $ref: '#/components/schemas/Toteutus'
      |""".stripMargin
  )
  post("/findToteutuksetByOids") {
    implicit val authenticated: Authenticated = authenticate
    val toteutusOids                          = parsedBody.extract[Set[ToteutusOid]]
    toteutusOids match {
      case (oids) if (oids.exists(!_.isValid())) =>
        BadRequest(s"Invalid toteutusOids ${oids.find(!_.isValid()).get.toString}")
      case (oids) => odwService.findToteutuksetByOids(oids)
    }
  }
}

object OdwServlet extends OdwServlet(OdwService, SessionDAO)
