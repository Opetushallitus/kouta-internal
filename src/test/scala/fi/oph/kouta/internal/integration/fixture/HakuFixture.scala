package fi.oph.kouta.internal.integration.fixture

import java.util.UUID
import fi.oph.kouta.internal.domain.Haku
import fi.oph.kouta.internal.domain.oid.HakuOid
import fi.oph.kouta.internal.elasticsearch.HakuClient
import fi.oph.kouta.internal.service.HakuService
import fi.oph.kouta.internal.servlet.HakuServlet
import fi.oph.kouta.internal.TempElasticClient

trait HakuFixture extends KoutaIntegrationSpec {
  val HakuPath = "/haku"

  addServlet(
    new HakuServlet(new HakuService(new HakuClient("haku-kouta", TempElasticClient.client)), sessionDAO),
    HakuPath
  )

  def get(oid: HakuOid): Haku = get[Haku](HakuPath, oid)

  def get(oid: HakuOid, sessionId: UUID): Haku = get[Haku](HakuPath, oid, sessionId)

  def get(oid: HakuOid, errorStatus: Int): Unit = get(oid, defaultSessionId, errorStatus)

  def get(oid: HakuOid, sessionId: UUID, errorStatus: Int): Unit = get(s"$HakuPath/$oid", sessionId, errorStatus)
}
