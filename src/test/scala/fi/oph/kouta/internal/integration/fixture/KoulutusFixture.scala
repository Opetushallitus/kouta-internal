package fi.oph.kouta.internal.integration.fixture

import java.util.UUID
import fi.oph.kouta.domain.{AmmOsaamisala, AmmTutkinnonOsa}
import fi.oph.kouta.external.KoutaFixtureTool
import fi.oph.kouta.internal.domain.Koulutus
import fi.oph.kouta.internal.domain.oid.{KoulutusOid, OrganisaatioOid}
import fi.oph.kouta.internal.elasticsearch.KoulutusClient
import fi.oph.kouta.internal.service.KoulutusService
import fi.oph.kouta.internal.servlet.KoulutusServlet
import fi.oph.kouta.internal.{OrganisaatioServiceMock, TempElasticClient}

trait KoulutusFixture extends KoutaIntegrationSpec {
  val KoulutusPath = "/koulutus"

  addServlet(
    new KoulutusServlet(
      new KoulutusService(new KoulutusClient("koulutus-kouta", TempElasticClient.client)),
      sessionDAO
    ),
    KoulutusPath
  )

  def get(oid: KoulutusOid): Koulutus = get[Koulutus](KoulutusPath, oid)

  def get(oid: KoulutusOid, sessionId: UUID): Koulutus = get[Koulutus](KoulutusPath, oid, sessionId)

  def get(oid: KoulutusOid, sessionId: UUID, errorStatus: Int): Unit =
    get(s"$KoulutusPath/$oid", sessionId, errorStatus)

  def addMockKoulutus(
      koulutusOid: KoulutusOid,
      organisaatioOid: OrganisaatioOid = OrganisaatioServiceMock.ChildOid,
      sorakuvausId: UUID
  ): Unit = {
    val koulutus = KoutaFixtureTool.DefaultKoulutusScala +
      (KoutaFixtureTool.OrganisaatioKey -> organisaatioOid.s) +
      (KoutaFixtureTool.SorakuvausIdKey -> sorakuvausId.toString)
    KoutaFixtureTool.addKoulutus(koulutusOid.s, koulutus)
    indexKoulutus(koulutusOid)
  }

  def addMockTutkinnonOsaKoulutus(
      koulutusOid: KoulutusOid,
      organisaatioOid: OrganisaatioOid = OrganisaatioServiceMock.ChildOid,
      sorakuvausId: UUID
  ): Unit = {
    val koulutus = KoutaFixtureTool.DefaultKoulutusScala +
      (KoutaFixtureTool.MetadataKey            -> KoutaFixtureTool.ammTutkinnonOsaKoulutusMetadata) +
      (KoutaFixtureTool.KoulutustyyppiKey      -> AmmTutkinnonOsa.name) +
      (KoutaFixtureTool.JohtaaTutkintoonKey    -> "false") +
      (KoutaFixtureTool.EPerusteIdKey          -> null) +
      (KoutaFixtureTool.KoulutuksetKoodiUriKey -> null) +
      (KoutaFixtureTool.OrganisaatioKey        -> organisaatioOid.s) +
      (KoutaFixtureTool.SorakuvausIdKey        -> sorakuvausId.toString)
    KoutaFixtureTool.addKoulutus(koulutusOid.s, koulutus)
    indexKoulutus(koulutusOid)
  }

  def addMockOsaamisalaKoulutus(
      koulutusOid: KoulutusOid,
      organisaatioOid: OrganisaatioOid = OrganisaatioServiceMock.ChildOid,
      sorakuvausId: UUID
  ): Unit = {
    val koulutus = KoutaFixtureTool.DefaultKoulutusScala +
      (KoutaFixtureTool.MetadataKey         -> KoutaFixtureTool.ammOsaamisalaKoulutusMetadata) +
      (KoutaFixtureTool.KoulutustyyppiKey   -> AmmOsaamisala.name) +
      (KoutaFixtureTool.JohtaaTutkintoonKey -> "false") +
      (KoutaFixtureTool.OrganisaatioKey     -> organisaatioOid.s) +
      (KoutaFixtureTool.SorakuvausIdKey     -> sorakuvausId.toString)
    KoutaFixtureTool.addKoulutus(koulutusOid.s, koulutus)
    indexKoulutus(koulutusOid)
  }

  def addMockSorakuvaus(id: UUID, organisaatioOid: OrganisaatioOid): Unit = {
    val sorakuvaus = KoutaFixtureTool.DefaultSorakuvausScala + (KoutaFixtureTool.OrganisaatioKey -> organisaatioOid.s)
    KoutaFixtureTool.addSorakuvaus(id.toString, sorakuvaus)
    indexSorakuvaus(id)
  }
}
