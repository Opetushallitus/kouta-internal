package fi.oph.kouta.internal.integration.fixture

import fi.oph.kouta.domain.{AmmOsaamisala, AmmTutkinnonOsa}
import fi.oph.kouta.external.KoutaFixtureTool
import fi.oph.kouta.internal.TempElasticClient
import fi.oph.kouta.internal.domain.Toteutus
import fi.oph.kouta.internal.domain.oid.{KoulutusOid, OrganisaatioOid, ToteutusOid}
import fi.oph.kouta.internal.elasticsearch.ToteutusClient
import fi.oph.kouta.internal.service.ToteutusService
import fi.oph.kouta.internal.servlet.ToteutusServlet

import java.util.UUID

trait ToteutusFixture extends KoutaIntegrationSpec {
  val ToteutusPath = "/toteutus"

  addServlet(
    new ToteutusServlet(
      new ToteutusService(new ToteutusClient("toteutus-kouta", TempElasticClient.client)),
      sessionDAO
    ),
    ToteutusPath
  )

  def get(oid: ToteutusOid): Toteutus = get[Toteutus](ToteutusPath, oid)

  def get(oid: ToteutusOid, sessionId: UUID): Toteutus = get[Toteutus](ToteutusPath, oid, sessionId)

  def get(oid: ToteutusOid, sessionId: UUID, errorStatus: Int): Unit =
    get(s"$ToteutusPath/$oid", sessionId, errorStatus)

  def addMockToteutus(toteutusOid: ToteutusOid, organisaatioOid: OrganisaatioOid, koulutusOid: KoulutusOid): Unit = {
    val toteutus = KoutaFixtureTool.DefaultToteutusScala +
      (KoutaFixtureTool.OrganisaatioKey -> organisaatioOid.s) +
      (KoutaFixtureTool.KoulutusOidKey  -> koulutusOid.s) +
      (KoutaFixtureTool.TarjoajatKey    -> organisaatioOid.s)
    KoutaFixtureTool.addToteutus(toteutusOid.s, toteutus)
    indexToteutus(toteutusOid)
  }

  def addMockTutkinnonOsaToteutus(
      toteutusOid: ToteutusOid,
      organisaatioOid: OrganisaatioOid,
      koulutusOid: KoulutusOid
  ): Unit = {
    val toteutus = KoutaFixtureTool.DefaultToteutusScala +
      (KoutaFixtureTool.OrganisaatioKey   -> organisaatioOid.s) +
      (KoutaFixtureTool.KoulutusOidKey    -> koulutusOid.s) +
      (KoutaFixtureTool.TarjoajatKey      -> organisaatioOid.s) +
      (KoutaFixtureTool.MetadataKey       -> KoutaFixtureTool.ammTutkinnonOsaToteutusMetadata) +
      (KoutaFixtureTool.KoulutustyyppiKey -> AmmTutkinnonOsa.name)
    KoutaFixtureTool.addToteutus(toteutusOid.s, toteutus)
    indexToteutus(toteutusOid)
  }

  def addMockOsaamisalaToteutus(
      toteutusOid: ToteutusOid,
      organisaatioOid: OrganisaatioOid,
      koulutusOid: KoulutusOid
  ): Unit = {
    val toteutus = KoutaFixtureTool.DefaultToteutusScala +
      (KoutaFixtureTool.OrganisaatioKey   -> organisaatioOid.s) +
      (KoutaFixtureTool.KoulutusOidKey    -> koulutusOid.s) +
      (KoutaFixtureTool.TarjoajatKey      -> organisaatioOid.s) +
      (KoutaFixtureTool.MetadataKey       -> KoutaFixtureTool.ammOsaamisalaToteutusMetadata) +
      (KoutaFixtureTool.KoulutustyyppiKey -> AmmOsaamisala.name)
    KoutaFixtureTool.addToteutus(toteutusOid.s, toteutus)
    indexToteutus(toteutusOid)
  }
}
