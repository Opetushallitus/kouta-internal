package fi.oph.kouta.internal.integration

import fi.oph.kouta.internal.TempElasticClient
import fi.oph.kouta.internal.domain.Haku
import fi.oph.kouta.internal.domain.oid.HakuOid
import fi.oph.kouta.internal.integration.fixture.{AccessControlSpec, HakuFixture}
import fi.oph.kouta.internal.security.Role

import java.util.UUID

class HakuSpec extends HakuFixture with AccessControlSpec {

  override val roleEntities  = Seq(Role.Haku)
  val nonExistingId: HakuOid = HakuOid("1.2.246.562.29.00000000000000000000")

  val hakuOid1 = HakuOid("1.2.246.562.29.00000000000000000001")
  val hakuOid2 = HakuOid("1.2.246.562.29.00000000000000000002")
  val hakuOid3 = HakuOid("1.2.246.562.29.00000000000000000003")
  val hakuOid4 = HakuOid("1.2.246.562.29.00000000000000000004")
  val hakuOid5 = HakuOid("1.2.246.562.29.00000000000000000005")
  val hakuOid6 = HakuOid("1.2.246.562.29.00000000000000000006")
  val hakuOid7 = HakuOid("1.2.246.562.29.00000000000000000007")

  val ataruId1: UUID = UUID.fromString("dcd38a87-912e-4e91-8840-99c7e242dd53")
  val ataruId2: UUID = UUID.fromString("dcd38a87-912e-4e91-8840-99c7e242dd54")
  val ataruId3: UUID = UUID.fromString("dcd38a87-912e-4e91-8840-99c7e242dd55")

  "GET /:id" should s"get haku from elastic search" in {
    get(hakuOid1, defaultSessionId)
    get(hakuOid7, defaultSessionId).maksullinenKkHaku shouldEqual true
  }

  it should s"return 404 if haku not found" in {
    get(s"$HakuPath/$nonExistingId", headers = Seq(defaultSessionHeader)) {
      status should equal(404)
      body should include(s"Didn't find id $nonExistingId")
    }
  }

  it should "return 401 without a valid session" in {
    get(s"$HakuPath/$nonExistingId") {
      status should equal(401)
      body should include("Unauthorized")
    }
  }

  it should "return status code 404 if entity cannot be parsed" in {
    get(hakuOid1, crudSessions(ChildOid))
    updateExistingHakuToCertainTila(hakuOid1.s, "outotila")
    get(hakuOid1, crudSessions(ChildOid), 404)
    updateExistingHakuToCertainTila(hakuOid1.s, "julkaistu")
  }

  "Search by Ataru ID" should "find haku based on Ataru ID" in {
    val haut = get[Seq[Haku]](s"$HakuPath/search?ataruId=$ataruId1", defaultSessionId)

    val ataruIds = haut.map(_.hakulomakeAtaruId)
    ataruIds.foreach(_ should not be empty)
    ataruIds.map(_.get).foreach(_ shouldEqual ataruId1)

    haut.map(_.oid) should contain theSameElementsAs Seq(
      hakuOid1,
      hakuOid2,
      hakuOid5,
      hakuOid6,
      hakuOid7
    )
  }

  it should "return 200 with an empty list if no haut are found" in {
    get[Seq[Haku]](s"$HakuPath/search?ataruId=$ataruId3", defaultSessionId) should be(empty)
  }

  it should "return 401 without a valid session" in {
    get(s"$HakuPath/search?ataruId=$ataruId3") {
      status should equal(401)
      body should include("Unauthorized")
    }
  }

  it should "skip entities that can't be deserialized" in {
    updateExistingHakuToCertainTila(hakuOid1.s, "outotila")

    val haut = get[Seq[Haku]](s"$HakuPath/search?ataruId=$ataruId1", defaultSessionId)

    haut.map(_.oid) should contain theSameElementsAs Seq(
      hakuOid2,
      hakuOid5,
      hakuOid6,
      hakuOid7
    )
    updateExistingHakuToCertainTila(hakuOid1.s, "julkaistu")
  }

  "Search by hakuvuosi" should "find haut based on hakuvuosi" in {
    val haut = get[Seq[Haku]](s"$HakuPath/search?hakuvuosi=2026", defaultSessionId)

    haut.map(_.oid) should contain theSameElementsAs Seq(
      hakuOid1,
      hakuOid2,
      hakuOid3,
      hakuOid4,
      hakuOid5,
      hakuOid6,
      hakuOid7
    )
  }

  it should "return 200 with an empty list if no haut match the hakuvuosi" in {
    get[Seq[Haku]](s"$HakuPath/search?hakuvuosi=2025", defaultSessionId) should be(empty)
  }

  "Search by koulutuksenAlkamisvuosi" should "find haut based on koulutuksen alkamisvuosi" in {
    val haut = get[Seq[Haku]](s"$HakuPath/search?koulutuksenAlkamisvuosi=2026", defaultSessionId)

    haut.map(_.oid) should contain theSameElementsAs Seq(
      hakuOid1,
      hakuOid2,
      hakuOid3,
      hakuOid4,
      hakuOid5,
      hakuOid6,
      hakuOid7
    )
  }

  it should "return 200 with an empty list if no haut match the koulutuksen alkamisvuosi" in {
    get[Seq[Haku]](s"$HakuPath/search?koulutuksenAlkamisvuosi=2025", defaultSessionId) should be(empty)
  }

  // Testit asettavat hakuOid3:n hakuvuodeksi 2025 (alkamisvuosi pysyy 2026), jotta hakuehdot voidaan erottaa toisistaan.
  "hakuvuosi and koulutuksenAlkamisvuosi" should "filter independently of each other" in {
    updateExistingHakuHakuvuosi(hakuOid3.s, 2025)
    try {
      get[Seq[Haku]](s"$HakuPath/search?hakuvuosi=2025", defaultSessionId)
        .map(_.oid) should contain theSameElementsAs Seq(hakuOid3)

      // Haku löytyy alkamisvuodella vaikka sen hakuvuosi on eri. Vanha yhdistetty vuosi-parametri pudotti tämän.
      get[Seq[Haku]](s"$HakuPath/search?koulutuksenAlkamisvuosi=2026", defaultSessionId)
        .map(_.oid) should contain(hakuOid3)
    } finally {
      updateExistingHakuHakuvuosi(hakuOid3.s, 2026)
    }
  }

  it should "combine the two parameters with AND" in {
    updateExistingHakuHakuvuosi(hakuOid3.s, 2025)
    try {
      get[Seq[Haku]](s"$HakuPath/search?hakuvuosi=2025&koulutuksenAlkamisvuosi=2026", defaultSessionId)
        .map(_.oid) should contain theSameElementsAs Seq(hakuOid3)

      get[Seq[Haku]](s"$HakuPath/search?hakuvuosi=2026&koulutuksenAlkamisvuosi=2026", defaultSessionId)
        .map(_.oid) should contain theSameElementsAs Seq(
        hakuOid1,
        hakuOid2,
        hakuOid4,
        hakuOid5,
        hakuOid6,
        hakuOid7
      )

      get[Seq[Haku]](
        s"$HakuPath/search?hakuvuosi=2025&koulutuksenAlkamisvuosi=2025",
        defaultSessionId
      ) should be(empty)
    } finally {
      updateExistingHakuHakuvuosi(hakuOid3.s, 2026)
    }
  }

  it should "combine with ataruId" in {
    val haut = get[Seq[Haku]](s"$HakuPath/search?ataruId=$ataruId1&hakuvuosi=2026", defaultSessionId)

    haut.map(_.oid) should contain theSameElementsAs Seq(
      hakuOid1,
      hakuOid2,
      hakuOid5,
      hakuOid6,
      hakuOid7
    )

    get[Seq[Haku]](s"$HakuPath/search?ataruId=$ataruId1&hakuvuosi=2025", defaultSessionId) should be(empty)
  }

  "Search parameter validation" should "return 400 for removed vuosi parameter" in {
    expectBadRequest("vuosi=2026", "vuosi is not supported")
  }

  it should "return 400 for non-numeric hakuvuosi" in {
    expectBadRequest("hakuvuosi=abc", "Invalid hakuvuosi: abc")
  }

  it should "return 400 for non-numeric koulutuksenAlkamisvuosi" in {
    expectBadRequest("koulutuksenAlkamisvuosi=abc", "Invalid koulutuksenAlkamisvuosi: abc")
  }

  it should "return 400 for invalid includeHakukohdeOids" in {
    expectBadRequest("includeHakukohdeOids=foo", "Invalid includeHakukohdeOids: foo")
  }

  it should "return 400 for ataruId that is not an UUID" in {
    expectBadRequest("ataruId=not-a-uuid", "Invalid ataruId: not-a-uuid")
  }

  it should "return 400 for invalid tarjoaja" in {
    expectBadRequest("tarjoaja=1.2.246.562.10.00000000001,foo", "Invalid tarjoaja foo")
  }

  it should "return 400 for invalid tarjoaja given as repeated parameter" in {
    expectBadRequest(s"tarjoaja=${ChildOid.s}&tarjoaja=foo", "Invalid tarjoaja foo")
  }

  it should "return 400 for empty values" in {
    expectBadRequest("hakuvuosi=", "Invalid hakuvuosi: ")
    expectBadRequest("koulutuksenAlkamisvuosi=", "Invalid koulutuksenAlkamisvuosi: ")
    expectBadRequest("ataruId=", "Invalid ataruId: ")
    expectBadRequest("includeHakukohdeOids=", "Invalid includeHakukohdeOids: ")
  }

  it should "include hakukohdeOids only when includeHakukohdeOids is true, case-insensitively" in {
    def hakukohdeOidsIncluded(value: String): Seq[Boolean] =
      get[Seq[Haku]](s"$HakuPath/search?ataruId=$ataruId1&includeHakukohdeOids=$value", defaultSessionId)
        .map(_.hakukohdeOids.isDefined)

    hakukohdeOidsIncluded("true") should (not be empty and contain only true)
    hakukohdeOidsIncluded("TRUE") should (not be empty and contain only true)
    hakukohdeOidsIncluded("false") should (not be empty and contain only false)
    get[Seq[Haku]](s"$HakuPath/search?ataruId=$ataruId1", defaultSessionId)
      .map(_.hakukohdeOids) should contain only None
  }

  it should "accept valid parameters together" in {
    get[Seq[Haku]](
      s"$HakuPath/search?ataruId=$ataruId1&tarjoaja=${ChildOid.s}&includeHakukohdeOids=true" +
        "&hakuvuosi=2026&koulutuksenAlkamisvuosi=2026",
      defaultSessionId
    )
  }

  private def expectBadRequest(query: String, expectedError: String): Unit =
    get(s"$HakuPath/search?$query", headers = Seq(defaultSessionHeader)) {
      withClue(body) {
        status should equal(400)
      }
      body should include(expectedError)
    }

  private def updateExistingHakuToCertainTila(hakuOid: String, tila: String): Unit =
    updateExistingHaku(hakuOid, "tila" -> tila)

  private def updateExistingHakuHakuvuosi(hakuOid: String, hakuvuosi: Int): Unit =
    updateExistingHaku(hakuOid, "hakuvuosi" -> hakuvuosi)

  private def updateExistingHaku(hakuOid: String, field: (String, Any)): Unit = {
    import com.sksamuel.elastic4s.ElasticDsl._
    import com.sksamuel.elastic4s.requests.common.RefreshPolicy
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.Await
    import scala.concurrent.duration.Duration

    // Refresh heti, jotta muokattu haku on haettavissa seuraavassa kyselyssä.
    val updateOperation = TempElasticClient.client.execute {
      updateById("haku-kouta-virkailija", hakuOid).doc(field).refresh(RefreshPolicy.Immediate)
    }

    Await.result(updateOperation, Duration.Inf)
  }
}
