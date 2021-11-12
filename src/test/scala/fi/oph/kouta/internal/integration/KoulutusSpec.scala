package fi.oph.kouta.internal.integration

import fi.oph.kouta.internal.domain.oid.KoulutusOid
import fi.oph.kouta.internal.integration.fixture.{AccessControlSpec, KoulutusFixture}
import fi.oph.kouta.internal.security.Role

import java.util.UUID

class KoulutusSpec extends KoulutusFixture with AccessControlSpec {

  override val roleEntities      = Seq(Role.Koulutus)
  val existingId: KoulutusOid    = KoulutusOid("1.2.246.562.13.00000000000000000009")
  val nonExistingId: KoulutusOid = KoulutusOid("1.2.246.562.13.00000000000000000000")
  val sorakuvausId: UUID         = UUID.fromString("9267884f-fba1-4b85-8bb3-3eb77440c197")

  override def beforeAll(): Unit = {
    super.beforeAll()
    addMockSorakuvaus(sorakuvausId, ChildOid)
    addMockKoulutus(existingId, ChildOid, sorakuvausId)
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
    addMockTutkinnonOsaKoulutus(tutkinnonOsaOid, ChildOid, sorakuvausId)
    get(tutkinnonOsaOid, defaultSessionId)
  }

  it should "get ammatillinen osaamisala koulutus" in {
    val osaamisalaOid = KoulutusOid("1.2.246.562.13.00000000000000000020")
    addMockOsaamisalaKoulutus(osaamisalaOid, ChildOid, sorakuvausId)
    get(osaamisalaOid, defaultSessionId)
  }
}
