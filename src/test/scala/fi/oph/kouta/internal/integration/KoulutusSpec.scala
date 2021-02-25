package fi.oph.kouta.internal.integration

import fi.oph.kouta.internal.domain.oid.KoulutusOid
import fi.oph.kouta.internal.integration.fixture.{AccessControlSpec, KoulutusFixture}
import fi.oph.kouta.internal.security.Role

class KoulutusSpec extends KoulutusFixture with AccessControlSpec {

  override val roleEntities               = Seq(Role.Koulutus)
  val existingId: KoulutusOid    = KoulutusOid("1.2.246.562.13.00000000000000000009")
  val nonExistingId: KoulutusOid = KoulutusOid("1.2.246.562.13.0")

  override def beforeAll(): Unit = {
    super.beforeAll()
    addMockKoulutus(existingId, ChildOid)
  }

  "GET /:id" should s"get koulutus from elastic search" in {
    get(existingId, defaultSessionId)
  }

  it should s"return 404 if koulutus not found" in {
    get(s"$KoulutusPath/$nonExistingId", headers = Seq(defaultSessionHeader)) {
      status should equal(404)
      body should include(s"Didn't find id $nonExistingId")
    }
  }

  it should "return 401 without a valid session" in {
    get(s"$KoulutusPath/$nonExistingId") {
      status should equal(401)
      body should include("Unauthorized")
    }
  }

  it should "get ammatillinen tutkinnon osa koulutus" in {
    val tutkinnonOsaOid = KoulutusOid("1.2.246.562.13.00000000000000000019")
    addMockTutkinnonOsaKoulutus(tutkinnonOsaOid)
    get(tutkinnonOsaOid, defaultSessionId)
  }
}
