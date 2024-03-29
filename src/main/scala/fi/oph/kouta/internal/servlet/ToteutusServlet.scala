package fi.oph.kouta.internal.servlet

import fi.oph.kouta.internal.database.SessionDAO
import fi.oph.kouta.internal.domain.oid.{HakuOid, ToteutusOid}
import fi.oph.kouta.internal.security.Authenticated
import fi.oph.kouta.internal.service.ToteutusService
import fi.oph.kouta.internal.swagger.SwaggerPaths.registerPath
import org.scalatra.FutureSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class ToteutusServlet(toteutusService: ToteutusService, val sessionDAO: SessionDAO)
    extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  registerPath(
    "/toteutus/{oid}",
    """    get:
      |      summary: Hae koulutuksen toteutus
      |      operationId: Hae toteutus
      |      description: Hakee koulutuksen toteutuksen tiedot
      |      tags:
      |        - Toteutus
      |      parameters:
      |        - in: path
      |          name: oid
      |          schema:
      |            type: string
      |          required: true
      |          description: toteutus-oid
      |          example: 1.2.246.562.17.00000000000000000009
      |      responses:
      |        '200':
      |          description: Ok
      |          content:
      |            application/json:
      |              schema:
      |                $ref: '#/components/schemas/Toteutus'
      |""".stripMargin
  )
  get("/:oid") {
    val toteutusOid = ToteutusOid(params("oid"))
    toteutusOid.validateLength()

    implicit val authenticated: Authenticated = authenticate

    toteutusService.get(toteutusOid)
  }

  registerPath(
    "/toteutus/search",
    """    get:
      |      summary: Etsi toteutuksia
      |      description: Etsi toteutuksia haku-oidilla
      |      operationId: Etsi toteutuksia
      |      tags:
      |        - Toteutus
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
      |                  $ref: '#/components/schemas/Toteutus'
      |""".stripMargin
  )
  get("/search") {
    implicit val authenticated: Authenticated = authenticate

    val hakuOid: HakuOid = params
      .get("hakuOid")
      .map(HakuOid)
      .getOrElse(throw new RuntimeException("HakuOid is mandatory parameter"))

    toteutusService.getByHakuOid(hakuOid)
  }
}

object ToteutusServlet extends ToteutusServlet(ToteutusService, SessionDAO)
