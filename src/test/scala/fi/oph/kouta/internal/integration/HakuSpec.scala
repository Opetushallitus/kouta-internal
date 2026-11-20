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

  "Search by hakukohderyhmat" should "find only haut containing group hakukohteet" in {
    val haut = get[Seq[Haku]](s"$HakuPath/search?hakukohderyhmat=1.2.246.562.28.00000000001", defaultSessionId)
    haut.map(_.oid) should contain theSameElementsAs Seq(hakuOid1)
  }

  it should "support repeated and comma-separated groups" in {
    Seq(
      "hakukohderyhmat=1.2.246.562.28.00000000001&hakukohderyhmat=1.2.246.562.28.00000000002",
      "hakukohderyhmat=1.2.246.562.28.00000000001,1.2.246.562.28.00000000002"
    ).foreach { query =>
      get[Seq[Haku]](s"$HakuPath/search?$query", defaultSessionId).map(_.oid) should
        contain theSameElementsAs Seq(hakuOid1, hakuOid7)
    }
  }

  it should "return no haut for an empty group" in {
    get[Seq[Haku]](s"$HakuPath/search?hakukohderyhmat=1.2.246.562.28.00000000003", defaultSessionId) should be(empty)
  }

  it should "combine provider and group matches with OR" in {
    val providerQuery = "tarjoaja=1.2.246.562.10.00000000001"
    val providerHaut  = get[Seq[Haku]](s"$HakuPath/search?$providerQuery", defaultSessionId)
    val haut = get[Seq[Haku]](
      s"$HakuPath/search?$providerQuery&hakukohderyhmat=1.2.246.562.28.00000000002",
      defaultSessionId
    )
    haut.map(_.oid).toSet shouldEqual (providerHaut.map(_.oid).toSet + hakuOid7)
  }

  it should "apply Ataru restrictions also to group matches" in {
    get[Seq[Haku]](
      s"$HakuPath/search?ataruId=$ataruId3&hakukohderyhmat=1.2.246.562.28.00000000001",
      defaultSessionId
    ) should be(empty)
  }

  it should "reject invalid group OIDs" in {
    get(s"$HakuPath/search?hakukohderyhmat=invalid", headers = Seq(defaultSessionHeader)) {
      status should equal(400)
      body should include("Invalid hakukohderyhmat")
    }
  }

  private def updateExistingHakuToCertainTila(hakuOid: String, tila: String): Unit = {
    import com.sksamuel.elastic4s.ElasticDsl._
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.Await
    import scala.concurrent.duration.Duration

    val updateOperation = TempElasticClient.client.execute {
      updateById("haku-kouta-virkailija", hakuOid).doc("tila" -> tila)
    }

    Await.result(updateOperation, Duration.Inf)
    // Elasticsearch refreshaa oletuksena sekunnin välein. Odotetaan sekunti että muokattu haku on haettavissa.
    Thread.sleep(1000)
  }
}
