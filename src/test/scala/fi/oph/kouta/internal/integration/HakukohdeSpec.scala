package fi.oph.kouta.internal.integration

import java.util.UUID

import fi.oph.kouta.internal.domain.Hakukohde
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid, KoulutusOid, ToteutusOid}
import fi.oph.kouta.internal.integration.fixture._
import fi.oph.kouta.internal.security.Role
import org.json4s.jackson.JsonMethods.parse

class HakukohdeSpec extends HakukohdeFixture with AccessControlSpec with GenericGetTests[Hakukohde, HakukohdeOid] {

  override val roleEntities                = Seq(Role.Hakukohde)
  override val getPath: String             = HakukohdePath
  override val entityName: String          = "hakukohde"
  override val existingId: HakukohdeOid    = HakukohdeOid("1.2.246.562.20.00000000000000000001")
  override val nonExistingId: HakukohdeOid = HakukohdeOid("1.2.246.562.20.00000000000000000000")

  val hakukohdeOid1      = HakukohdeOid("1.2.246.562.20.00000000000000000001")
  val hakukohdeOid2      = HakukohdeOid("1.2.246.562.20.00000000000000000002")
  val hakuOid            = HakuOid("1.2.246.562.29.00000000000000000001")
  val nonExistingOid     = HakukohdeOid("1.2.246.562.20.00000000000000000000")
  val nonExistingHakuOid = HakukohdeOid("1.2.246.562.29.00000000000000000000")
  val tooShortOid        = HakukohdeOid("1.2.246.562.20.333333")

  getTests()

  it should "get hakukohde by OID" in {
    val hakukohde = get[Hakukohde](s"$HakukohdePath/${hakukohdeOid1.toString}", defaultSessionId)
    hakukohde.oid should equal(hakukohdeOid1)
    hakukohde.opetuskieliKoodiUrit should contain theSameElementsAs Seq("oppilaitoksenopetuskieli_1#1")
  }

  it should "find hakukohde based on haku OID" in {
    val hakukohteet = get[Seq[Hakukohde]](s"$HakukohdePath/search?haku=${hakuOid.toString}", defaultSessionId)
    hakukohteet.map(_.oid) should contain theSameElementsAs Seq(hakukohdeOid1, hakukohdeOid2)
    hakukohteet.foreach(_.hakuOid should be(hakuOid))
  }

  it should "find hakukohde based on tarjoaja OID" in {
    val hakukohteet =
      get[Seq[Hakukohde]](s"$HakukohdePath/search?tarjoaja=${ParentOid.toString}", defaultSessionId)
    hakukohteet.map(_.oid) should contain theSameElementsAs Seq(hakukohdeOid1)
    hakukohteet.foreach(_.oikeusHakukohteeseen should be(Some(true)))
  }

  it should "find hakukohde based on haku and tarjoaja OID" in {
    val hakukohteet = get[Seq[Hakukohde]](
      s"$HakukohdePath/search?haku=${hakuOid.toString}&tarjoaja=${ParentOid.toString}",
      defaultSessionId
    )
    hakukohteet.map(_.oid) should contain theSameElementsAs Seq(hakukohdeOid1)
    hakukohteet.foreach(_.oikeusHakukohteeseen should be(Some(true)))
  }

  it should "find no hakukohde based on tarjoaja OID" in {
    val hakukohteet = get[Seq[Hakukohde]](
      s"$HakukohdePath/search?haku=${hakuOid.toString}&tarjoaja=${GrandChildOid.toString}",
      defaultSessionId
    )
    hakukohteet.isEmpty should be(true)
  }

  it should "find all hakukohde based on haku and give rights by tarjoaja OID" in {
    val hakukohteet = get[Seq[Hakukohde]](
      s"$HakukohdePath/search?all=true&haku=${hakuOid.s}&tarjoaja=${ParentOid.s}",
      defaultSessionId
    )
    hakukohteet.map(hk => (hk.oid, hk.oikeusHakukohteeseen)) should contain theSameElementsAs Seq(
      (hakukohdeOid1, Some(true)),
      (hakukohdeOid2, Some(false))
    )
  }

  it should "return 404 if haku does not exist" in {
    get(s"$HakukohdePath/search?haku=${nonExistingHakuOid.s}", headers = Seq(defaultSessionHeader)) {
      status should equal(404)
      body should include(s"Didn't find id 1.2.246.562.29.00000000000000000000")
    }
  }

  it should s"return 404 if oid is too short" in {
    get(s"$HakukohdePath/${tooShortOid.s}", headers = Seq(defaultSessionHeader)) {
      status should equal(404)
      body should include(s"Oid ${tooShortOid.s} is too short")
    }
  }

  it should "find hakukohteet based on array of OIDs" in {
    val oidSeq = Seq(hakukohdeOid1.toString, nonExistingOid.toString)
    post(s"$HakukohdePath/findbyoids", bytes(oidSeq), Seq(defaultSessionHeader)) {
      status should equal(200)

      val parsedResponse = parse(body).extract[Set[Hakukohde]]
      parsedResponse.map(_.oid) should contain theSameElementsAs Seq(hakukohdeOid1)
    }
  }

  it should "find hakukohteet based on array of OIDs and return rights corresponding to tarjoaja" in {
    val oidSeq = Seq(hakukohdeOid1.toString, hakukohdeOid2.toString)
    post(s"$HakukohdePath/findbyoids?tarjoaja=${ParentOid.toString}", bytes(oidSeq), Seq(defaultSessionHeader)) {
      status should equal(200)

      val parsedResponse = parse(body).extract[Set[Hakukohde]]
      parsedResponse.map(hk => (hk.oid, hk.oikeusHakukohteeseen)) should contain theSameElementsAs Seq(
        (hakukohdeOid1, Some(true)),
        (hakukohdeOid2, Some(false))
      )
    }
  }

  override def header = ???
}
