package fi.oph.kouta.internal.integration.fixture

import java.util.UUID

import fi.oph.kouta.external.KoutaFixtureTool
import fi.oph.kouta.internal.domain.Haku
import fi.oph.kouta.internal.domain.oid.{HakuOid, OrganisaatioOid}
import fi.oph.kouta.internal.servlet.HakuServlet
import fi.oph.kouta.internal.{OrganisaatioServiceMock, TempElasticClientHolder}

trait HakuFixture extends KoutaIntegrationSpec {
  val HakuPath = "/haku"

  addServlet(new HakuServlet(TempElasticClientHolder), HakuPath)

  def get(oid: HakuOid): Haku = get[Haku](HakuPath, oid)

  def get(oid: HakuOid, sessionId: UUID): Haku = get[Haku](HakuPath, oid, sessionId)

  def get(oid: HakuOid, errorStatus: Int): Unit = get(oid, defaultSessionId, errorStatus)

  def get(oid: HakuOid, sessionId: UUID, errorStatus: Int): Unit = get(s"$HakuPath/$oid", sessionId, errorStatus)

  def addMockHaku(hakuOid: HakuOid, organisaatioOid: OrganisaatioOid = OrganisaatioServiceMock.ChildOid): Unit = {
    val haku = KoutaFixtureTool.DefaultHakuScala + (KoutaFixtureTool.OrganisaatioKey -> organisaatioOid.s)
    KoutaFixtureTool.addHaku(hakuOid.s, haku)
    indexHaku(hakuOid)
  }
}
