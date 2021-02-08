package fi.oph.kouta.internal.integration

import java.util.UUID

import fi.oph.kouta.internal.domain.Hakukohde
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid, KoulutusOid, ToteutusOid}
import fi.oph.kouta.internal.integration.fixture._
import fi.oph.kouta.internal.security.Role

class HakukohdeSpec
    extends HakukohdeFixture
    with HakuFixture
    with KoulutusFixture
    with ToteutusFixture
    with ValintaperusteFixture
    with AccessControlSpec
    with GenericGetTests[Hakukohde, HakukohdeOid] {

  override val roleEntities                = Seq(Role.Hakukohde)
  override val getPath: String             = HakukohdePath
  override val entityName: String          = "hakukohde"
  override val existingId: HakukohdeOid    = HakukohdeOid("1.2.246.562.20.00000000000000000009")
  override val nonExistingId: HakukohdeOid = HakukohdeOid("1.2.246.562.20.0")

  val hakuOid: HakuOid = HakuOid("1.2.246.562.29.00000000000000000010")
  val toteutusId: ToteutusOid = ToteutusOid("1.2.246.562.17.00000000000000000010")
  val koulutusOid: KoulutusOid = KoulutusOid("1.2.246.562.13.00000000000000000010")
  val valintaperusteId: UUID = UUID.fromString("fa7fcb96-3f80-4162-8d19-5b74731cf90c")
  val sorakuvausId: UUID = UUID.fromString("e17773b2-f5a0-418d-a49f-34578c4b3625")

  override def beforeAll(): Unit = {
    super.beforeAll()

    addMockHaku(hakuOid, ParentOid)
    addMockKoulutus(koulutusOid, ParentOid)
    addMockToteutus(toteutusId, ParentOid, koulutusOid)

    addMockSorakuvaus(sorakuvausId, ChildOid)
    addMockValintaperuste(valintaperusteId, ChildOid, sorakuvausId)

    addMockHakukohde(existingId, ChildOid, hakuOid, toteutusId, valintaperusteId, jarjestyspaikkaOid = ChildOid)
  }

  getTests()

  it should "find hakukohde based on haku OID" in {
    val hakukohteet = get[Seq[Hakukohde]](s"$HakukohdePath/search?haku=${hakuOid.toString}", defaultSessionId)
    hakukohteet.map(_.oid) should contain theSameElementsAs Seq(existingId)
    hakukohteet.foreach(_.hakuOid should be(hakuOid))
  }

  it should "find hakukohde based on tarjoaja OID" in {
    val hakukohteet = get[Seq[Hakukohde]](s"$HakukohdePath/search?tarjoaja=${ParentOid.toString}", defaultSessionId)
    hakukohteet.map(_.oid) should contain theSameElementsAs Seq(existingId)
  }

  it should "find hakukohde based on haku and tarjoaja OID" in {
    val hakukohteet = get[Seq[Hakukohde]](
      s"$HakukohdePath/search?haku=${hakuOid.toString}&tarjoaja=${ParentOid.toString}",
      defaultSessionId
    )
    hakukohteet.map(_.oid) should contain theSameElementsAs Seq(existingId)
  }

  it should "return 404 if haku does not exist" in {
    get(s"$HakukohdePath/search?haku=1.2.246.562.29.00000000000000000001", headers = Seq(defaultSessionHeader)) {
      status should equal(404)
      body should include(s"Didn't find id 1.2.246.562.29.00000000000000000001")
    }
  }
}
