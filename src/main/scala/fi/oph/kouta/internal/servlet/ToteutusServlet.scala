package fi.oph.kouta.internal.servlet

import fi.oph.kouta.internal.domain.oid.ToteutusOid
import fi.oph.kouta.internal.elasticsearch.ElasticsearchClientHolder
import fi.oph.kouta.internal.security.Authenticated
import fi.oph.kouta.internal.service.ToteutusService
import fi.oph.kouta.internal.swagger.SwaggerPaths.registerPath
import org.scalatra.FutureSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class ToteutusServlet(elasticsearchClientHolder: ElasticsearchClientHolder)
    extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  val toteutusService = new ToteutusService(elasticsearchClientHolder)

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
    implicit val authenticated: Authenticated = authenticate

    toteutusService.get(ToteutusOid(params("oid")))
  }

}
