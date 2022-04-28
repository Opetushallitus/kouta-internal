package fi.oph.kouta.internal.integration

import fi.oph.kouta.internal.domain.oid.{KoulutusOid, ToteutusOid}
import fi.oph.kouta.internal.integration.fixture.{AccessControlSpec, KoulutusFixture, ToteutusFixture}
import fi.oph.kouta.internal.security.Role

import java.util.UUID

class ToteutusSpec extends ToteutusFixture with AccessControlSpec {

  override val roleEntities = Seq(Role.Toteutus)
  val ammToteutusOid        = ToteutusOid("1.2.246.562.17.00000000000000000001")
  val ammTutkinnonosaOid    = ToteutusOid("1.2.246.562.17.00000000000000000002")
  val ammOsaamisalaOid      = ToteutusOid("1.2.246.562.17.00000000000000000003")
  val nonExistingOid        = ToteutusOid("1.2.246.562.17.00000000000000000000")
  val tooShortOid           = ToteutusOid("1.2.246.562.17.1234567")

  "GET /:id" should s"get toteutus from elastic search" in {
    get(ammToteutusOid, defaultSessionId)
  }

  it should s"return 404 if toteutus not found" in {
    get(s"$ToteutusPath/$nonExistingOid", headers = Seq(defaultSessionHeader)) {
      status should equal(404)
      body should include(s"Didn't find id $nonExistingOid")
    }
  }

  it should s"return 404 if toteutus oid is too short" in {
    get(s"$ToteutusPath/$tooShortOid", headers = Seq(defaultSessionHeader)) {
      status should equal(404)
      body should include(s"Oid $tooShortOid is too short")
    }
  }

  it should "return 401 without a valid session" in {
    get(s"$ToteutusPath/$nonExistingOid") {
      status should equal(401)
      body should include("Unauthorized")
    }
  }

  it should "get tutkinnon osa toteutus" in {
    get(ammTutkinnonosaOid, defaultSessionId)
  }
  it should "get osaamisala toteutus" in {
    get(ammTutkinnonosaOid, defaultSessionId)
  }
}
