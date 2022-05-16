package fi.oph.kouta.internal.integration.fixture

import java.util.UUID
import fi.oph.kouta.internal.TempElasticClient
import fi.oph.kouta.internal.domain.Hakukohde
import fi.oph.kouta.internal.domain.oid.HakukohdeOid
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
        new HakuService(new HakuClient("haku-kouta", TempElasticClient.client)),
        new HakukohderyhmaClientMock
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
}
