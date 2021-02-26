package fi.oph.kouta.internal.integration

import fi.oph.kouta.internal.domain.oid.{KoulutusOid, ToteutusOid}
import fi.oph.kouta.internal.integration.fixture.{AccessControlSpec, KoulutusFixture, ToteutusFixture}
import fi.oph.kouta.internal.security.Role

class ToteutusSpec extends ToteutusFixture with KoulutusFixture with AccessControlSpec {

  override val roleEntities      = Seq(Role.Toteutus)
  val existingId: ToteutusOid    = ToteutusOid("1.2.246.562.17.789")
  val nonExistingId: ToteutusOid = ToteutusOid("1.2.246.562.17.0")

  val koulutusOid: KoulutusOid = KoulutusOid("1.2.246.562.13.789")

  override def beforeAll(): Unit = {
    super.beforeAll()
    addMockKoulutus(koulutusOid, ChildOid)
    addMockToteutus(existingId, ChildOid, koulutusOid)
  }

  "GET /:id" should s"get toteutus from elastic search" in {
    get(existingId, defaultSessionId)
  }

  it should s"return 404 if toteutus not found" in {
    get(s"$ToteutusPath/$nonExistingId", headers = Seq(defaultSessionHeader)) {
      status should equal(404)
      body should include(s"Didn't find id $nonExistingId")
    }
  }

  it should "return 401 without a valid session" in {
    get(s"$ToteutusPath/$nonExistingId") {
      status should equal(401)
      body should include("Unauthorized")
    }
  }

  it should "get tutkinnon osa toteutus" in {
    val tutkinnonOsaOid = ToteutusOid("1.2.246.562.17.791")
    val koulutusOid = KoulutusOid("1.2.246.562.13.792")
    addMockTutkinnonOsaKoulutus(koulutusOid, ChildOid)
    addMockTutkinnonOsaToteutus(tutkinnonOsaOid, ChildOid, koulutusOid)
    get(tutkinnonOsaOid, defaultSessionId)
  }

  it should "get osaamisala toteutus" in {
    val tutkinnonOsaOid = ToteutusOid("1.2.246.562.17.793")
    val koulutusOid = KoulutusOid("1.2.246.562.13.794")
    addMockOsaamisalaKoulutus(koulutusOid, ChildOid)
    addMockOsaamisalaToteutus(tutkinnonOsaOid, ChildOid, koulutusOid)
    get(tutkinnonOsaOid, defaultSessionId)
  }
}
