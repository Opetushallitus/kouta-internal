package fi.oph.kouta.internal.integration

import fi.oph.kouta.internal.TempElasticDockerClient
import fi.oph.kouta.internal.domain.Haku
import fi.oph.kouta.internal.domain.oid.HakuOid
import fi.oph.kouta.internal.integration.fixture.{AccessControlSpec, HakuFixture}
import fi.oph.kouta.internal.security.Role

import java.util.UUID

class HakuSpec extends HakuFixture with AccessControlSpec with GenericGetTests[Haku, HakuOid] {

  override val roleEntities = Seq(Role.Haku)
  override val getPath      = HakuPath
  override val entityName   = "haku"
  val existingId            = HakuOid("1.2.246.562.29.00000000000000000009")
  val nonExistingId         = HakuOid("1.2.246.562.29.0")

  val ataruId1 = UUID.randomUUID()
  val ataruId2 = UUID.randomUUID()
  val ataruId3 = UUID.randomUUID()

  override def beforeAll(): Unit = {
    super.beforeAll()
    addMockHaku(existingId, ChildOid)

    addMockHaku(HakuOid("1.2.246.562.29.301"), ChildOid, Some(ataruId1))
    addMockHaku(HakuOid("1.2.246.562.29.302"), ChildOid, Some(ataruId1))
    addMockHaku(HakuOid("1.2.246.562.29.303"), ChildOid, Some(ataruId2))
    addMockHaku(HakuOid("1.2.246.562.29.304"), ChildOid, Some(ataruId2))
    addMockHaku(HakuOid("1.2.246.562.29.305"), ParentOid, Some(ataruId1))
    addMockHaku(HakuOid("1.2.246.562.29.306"), EvilChildOid, Some(ataruId1))
  }

  getTests()

  "Search by Ataru ID" should "find haku based on Ataru ID" in {
    val haut = get[Seq[Haku]](s"$HakuPath/search?ataruId=$ataruId1", defaultSessionId)

    val ataruIds = haut.map(_.hakulomakeAtaruId)
    ataruIds.foreach(_ should not be empty)
    ataruIds.map(_.get).foreach(_ shouldEqual ataruId1)

    haut.map(_.oid) should contain theSameElementsAs Seq(
      HakuOid("1.2.246.562.29.301"),
      HakuOid("1.2.246.562.29.302"),
      HakuOid("1.2.246.562.29.305"),
      HakuOid("1.2.246.562.29.306")
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

  private def updateExistingEntityWithUnknownTila(): Unit = {
    import com.sksamuel.elastic4s.http.ElasticDsl._

    import scala.concurrent.ExecutionContext.Implicits.global
    TempElasticDockerClient.client.execute {
      update(existingId.s).in("haku-kouta/haku-kouta").doc(Map("tila" -> "outotila"))
    }
    //TempElasticClient.client.execute(com.sksamuel.elastic4s.http.ElasticDsl.get(existingId.s).from("haku-kouta")).map(println(_))
  }

  it should "return status code 418 if entity cannot be parsed" in {
    get(existingId, crudSessions(ChildOid))
    updateExistingEntityWithUnknownTila()
    get(existingId, crudSessions(ChildOid), 418)
  }
}
