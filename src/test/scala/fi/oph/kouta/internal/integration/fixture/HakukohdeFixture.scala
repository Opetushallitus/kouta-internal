package fi.oph.kouta.internal.integration.fixture

import java.util.UUID
import fi.oph.kouta.external.KoutaFixtureTool
import fi.oph.kouta.internal.TempElasticClient
import fi.oph.kouta.internal.domain.Hakukohde
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid, ToteutusOid}
import fi.oph.kouta.internal.elasticsearch.{HakuClient, HakukohdeClient}
import fi.oph.kouta.internal.service.{HakuService, HakukohdeService}
import fi.oph.kouta.internal.servlet.HakukohdeServlet
import org.json4s.jackson.Serialization.write

trait HakukohdeFixture extends KoutaIntegrationSpec {
  val HakukohdePath = "/hakukohde"

  addServlet(
    new HakukohdeServlet(
      new HakukohdeService(
        new HakukohdeClient("hakukohde-kouta", TempElasticClient.client),
        new HakuService(new HakuClient("haku-kouta", TempElasticClient.client))
      ),
      sessionDAO
    ),
    HakukohdePath
  )

  def get(oid: HakukohdeOid): Hakukohde = get[Hakukohde](HakukohdePath, oid)

  def get(oid: HakukohdeOid, sessionId: UUID): Hakukohde = get[Hakukohde](HakukohdePath, oid, sessionId)

  def get(oid: HakukohdeOid, sessionId: UUID, errorStatus: Int): Unit =
    get(s"$HakukohdePath/$oid", sessionId, errorStatus)

  def bytes(o: AnyRef): Array[Byte] = write(o).getBytes()

  def addMockHakukohde(
      hakukohdeOid: HakukohdeOid,
      organisaatioOid: OrganisaatioOid,
      hakuOid: HakuOid,
      toteutusOid: ToteutusOid,
      valintaperusteId: UUID,
      jarjestyspaikkaOid: OrganisaatioOid
  ): Unit = {
    val hakukohde = KoutaFixtureTool.DefaultHakukohdeScala +
      (KoutaFixtureTool.OrganisaatioKey       -> organisaatioOid.s) +
      (KoutaFixtureTool.HakuOidKey            -> hakuOid.s) +
      (KoutaFixtureTool.ToteutusOidKey        -> toteutusOid.s) +
      (KoutaFixtureTool.ValintaperusteIdKey   -> valintaperusteId.toString) +
      (KoutaFixtureTool.JarjestyspaikkaOidKey -> jarjestyspaikkaOid.s)
    KoutaFixtureTool.addHakukohde(hakukohdeOid.s, hakukohde)
    indexHakukohde(hakukohdeOid)
  }
}
