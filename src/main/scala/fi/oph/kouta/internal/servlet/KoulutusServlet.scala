package fi.oph.kouta.internal.servlet

import fi.oph.kouta.internal.database.SessionDAO
import fi.oph.kouta.internal.domain.oid.{HakuOid, KoulutusOid}
import fi.oph.kouta.internal.security.Authenticated
import fi.oph.kouta.internal.service.KoulutusService
import fi.oph.kouta.internal.swagger.SwaggerPaths.registerPath
import org.scalatra.FutureSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class KoulutusServlet(koulutusService: KoulutusService, val sessionDAO: SessionDAO)
    extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  registerPath(
    "/koulutus/{oid}",
    """    get:
      |      summary: Hae koulutus
      |      description: Hae koulutuksen tiedot annetulla koulutus-oidilla
      |      operationId: Hae koulutus
      |      tags:
      |        - Koulutus
      |      parameters:
      |        - in: path
      |          name: oid
      |          schema:
      |            type: string
      |          required: true
      |          description: Koulutus-oid
      |          example: 1.2.246.562.13.00000000000000000009
      |      responses:
      |        '200':
      |          description: Ok
      |          content:
      |            application/json:
      |              schema:
      |                $ref: '#/components/schemas/Koulutus'
      |""".stripMargin
  )
  get("/:oid") {
    val koulutusOid = KoulutusOid(params("oid"))
    koulutusOid.validateLength()

    implicit val authenticated: Authenticated = authenticate

    koulutusService.get(koulutusOid)
  }

  registerPath(
    "/koulutus/search",
    """    get:
      |      summary: Etsi koulutuksia
      |      description: Etsi koulutuksia haku-oidilla
      |      operationId: Etsi koulutuksia
      |      tags:
      |        - Koulutus
      |      parameters:
      |        - in: query
      |          name: hakuOid
      |          schema:
      |            type: string
      |          required: true
      |          description: Haku-oid
      |          example: 1.2.246.562.29.00000000000000002776
      |      responses:
      |        '200':
      |          description: Ok
      |          content:
      |            application/json:
      |              schema:
      |                type: array
      |                items:
      |                  $ref: '#/components/schemas/Koulutus'
      |""".stripMargin
  )
  get("/search") {
    implicit val authenticated: Authenticated = authenticate

    val hakuOid: HakuOid = params
      .get("hakuOid")
      .map(HakuOid)
      .getOrElse(throw new RuntimeException("HakuOid is mandatory parameter"))

    koulutusService.getByHakuOid(hakuOid)
  }
}

object KoulutusServlet extends KoulutusServlet(KoulutusService, SessionDAO)
