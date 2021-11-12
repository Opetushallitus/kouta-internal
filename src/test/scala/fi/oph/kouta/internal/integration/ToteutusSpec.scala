package fi.oph.kouta.internal.integration

import fi.oph.kouta.internal.domain.oid.{KoulutusOid, ToteutusOid}
import fi.oph.kouta.internal.integration.fixture.{AccessControlSpec, KoulutusFixture, ToteutusFixture}
import fi.oph.kouta.internal.security.Role

import java.util.UUID

class ToteutusSpec extends ToteutusFixture with KoulutusFixture with AccessControlSpec {

  override val roleEntities      = Seq(Role.Toteutus)
  val existingId: ToteutusOid    = ToteutusOid("1.2.246.562.17.00000000000000000789")
  val nonExistingId: ToteutusOid = ToteutusOid("1.2.246.562.17.00000000000000000000")
  val tooShortOid: ToteutusOid = ToteutusOid("1.2.246.562.17.1234567")

  val koulutusOid: KoulutusOid = KoulutusOid("1.2.246.562.13.00000000000000000789")
  val sorakuvausId: UUID       = UUID.fromString("9267884f-fba1-4b85-8bb3-3eb77440c197")

  override def beforeAll(): Unit = {
    super.beforeAll()
    addMockSorakuvaus(sorakuvausId, ChildOid)
    addMockKoulutus(koulutusOid, ChildOid, sorakuvausId)
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

  it should s"return 404 if toteutus oid is too short" in {
    get(s"$ToteutusPath/$tooShortOid", headers = Seq(defaultSessionHeader)) {
      status should equal(404)
      body should include(s"Oid $tooShortOid is too short")
    }
  }

  it should "return 401 without a valid session" in {
    get(s"$ToteutusPath/$nonExistingId") {
      status should equal(401)
      body should include("Unauthorized")
    }
  }

  it should "get tutkinnon osa toteutus" in {
    val tutkinnonOsaOid = ToteutusOid("1.2.246.562.17.00000000000000000791")
    val koulutusOid     = KoulutusOid("1.2.246.562.13.00000000000000000792")
    addMockTutkinnonOsaKoulutus(koulutusOid, ChildOid, sorakuvausId)
    addMockTutkinnonOsaToteutus(tutkinnonOsaOid, ChildOid, koulutusOid)
    get(tutkinnonOsaOid, defaultSessionId)
  }
  it should "get osaamisala toteutus" in {
    val tutkinnonOsaOid = ToteutusOid("1.2.246.562.17.00000000000000000793")
    val koulutusOid     = KoulutusOid("1.2.246.562.13.00000000000000000793")
    addMockOsaamisalaKoulutus(koulutusOid, ChildOid, sorakuvausId)
    addMockOsaamisalaToteutus(tutkinnonOsaOid, ChildOid, koulutusOid)
    get(tutkinnonOsaOid, defaultSessionId)
  }
}
