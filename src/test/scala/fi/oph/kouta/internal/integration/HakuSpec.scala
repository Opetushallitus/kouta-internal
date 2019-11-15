package fi.oph.kouta.internal.integration

import fi.oph.kouta.internal.domain.Haku
import fi.oph.kouta.internal.domain.oid.HakuOid
import fi.oph.kouta.internal.integration.fixture.{AccessControlSpec, ElasticFixture, HakuFixture}
import fi.oph.kouta.internal.security.Role

class HakuSpec extends HakuFixture with AccessControlSpec with ElasticFixture {

  override val roleEntities = Seq(Role.Haku)

  val hakuOid = HakuOid("1.2.246.562.29.00000000000000000009")
  val notFoundHakuOid = HakuOid("1.2.246.562.29.0")

  override def beforeAll(): Unit = {
    super.beforeAll()
    addMockHaku(hakuOid, ChildOid)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    cleanElastic()
  }

  "GET /:oid" should "get haku from elastic search" in {
    val haku: Haku = get(hakuOid)

    haku.oid should not be empty
    haku.oid.get should equal(hakuOid)
  }

  it should "return 404 if haku not found" in {
    get(s"/haku/$notFoundHakuOid", headers = Seq(defaultSessionHeader)) {
      status should equal(404)
      body should include(s"Didn't find haku with id $notFoundHakuOid")
    }
  }

  it should "return 401 without a valid session" in {
    get("/haku/123") {
      status should equal(401)
      body should include("Unauthorized")
    }
  }

  it should "allow a user of the haku organization to read the haku" in {
    get(hakuOid, crudSessions(ChildOid))
  }

  it should "deny a user without access to the haku organization" in {
    get(hakuOid, crudSessions(LonelyOid), 403)
  }

  it should "allow a user of an ancestor organization to read the haku" in {
    get(hakuOid, crudSessions(ParentOid))
  }

  it should "deny a user with only access to a descendant organization" in {
    get(hakuOid, crudSessions(GrandChildOid), 403)
  }

  it should "deny a user with the wrong role" in {
    get(hakuOid, otherRoleSession, 403)
  }

  it should "allow indexer access" in {
    get(hakuOid, indexerSession)
  }


}
