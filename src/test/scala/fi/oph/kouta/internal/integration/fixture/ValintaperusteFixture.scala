package fi.oph.kouta.internal.integration.fixture

import java.util.UUID

import fi.oph.kouta.external.KoutaFixtureTool
import fi.oph.kouta.internal.TempElasticClient
import fi.oph.kouta.internal.domain.Valintaperuste
import fi.oph.kouta.internal.domain.oid.OrganisaatioOid
import fi.oph.kouta.internal.elasticsearch.ValintaperusteClient
import fi.oph.kouta.internal.service.ValintaperusteService
import fi.oph.kouta.internal.servlet.ValintaperusteServlet

trait ValintaperusteFixture extends KoutaIntegrationSpec {
  val ValintaperustePath = "/valintaperuste"

  addServlet(
    new ValintaperusteServlet(
      new ValintaperusteService(new ValintaperusteClient("valintaperuste-kouta", TempElasticClient.client)),
      sessionDAO
    ),
    ValintaperustePath
  )

  def get(id: UUID): Valintaperuste = get[Valintaperuste](ValintaperustePath, id)

  def get(id: UUID, sessionId: UUID): Valintaperuste = get[Valintaperuste](ValintaperustePath, id, sessionId)

  def get(id: UUID, sessionId: UUID, errorStatus: Int): Unit = get(s"$ValintaperustePath/$id", sessionId, errorStatus)

  def addMockValintaperuste(
      id: UUID,
      organisaatioOid: OrganisaatioOid
  ): Unit = {
    val valintaperuste = KoutaFixtureTool.DefaultValintaperusteScala +
      (KoutaFixtureTool.OrganisaatioKey -> organisaatioOid.s)
    KoutaFixtureTool.addValintaperuste(id.toString, valintaperuste)
    indexValintaperuste(id)
  }
}
