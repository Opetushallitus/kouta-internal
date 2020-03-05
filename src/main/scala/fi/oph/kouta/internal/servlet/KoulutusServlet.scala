package fi.oph.kouta.internal.servlet

import fi.oph.kouta.internal.domain.oid.KoulutusOid
import fi.oph.kouta.internal.elasticsearch.ElasticsearchClientHolder
import fi.oph.kouta.internal.security.Authenticated
import fi.oph.kouta.internal.service.KoulutusService
import fi.oph.kouta.internal.swagger.SwaggerPaths.registerPath
import org.scalatra.FutureSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class KoulutusServlet(elasticsearchClientHolder: ElasticsearchClientHolder)
    extends KoutaServlet
    with CasAuthenticatedServlet
    with FutureSupport {

  override def executor: ExecutionContext = global

  val koulutusService = new KoulutusService(elasticsearchClientHolder)

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
    implicit val authenticated: Authenticated = authenticate

    koulutusService.get(KoulutusOid(params("oid")))
  }

}
