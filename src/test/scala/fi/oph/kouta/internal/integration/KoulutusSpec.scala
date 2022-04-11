package fi.oph.kouta.internal.integration

import fi.oph.kouta.internal.domain.oid.KoulutusOid
import fi.oph.kouta.internal.integration.fixture.{AccessControlSpec, KoulutusFixture}
import fi.oph.kouta.internal.security.Role

import java.util.UUID

class KoulutusSpec extends KoulutusFixture with AccessControlSpec {

  override val roleEntities   = Seq(Role.Koulutus)
  val ammKoulutusOid          = KoulutusOid("1.2.246.562.13.00000000000000000001")
  val ammTukinnonosaOid       = KoulutusOid("1.2.246.562.13.00000000000000000002")
  val ammOsaamisalaOid        = KoulutusOid("1.2.246.562.13.00000000000000000003")
  val ammMuuOid               = KoulutusOid("1.2.246.562.13.00000000000000000003")
  val aikuistenPerusopetusOid = KoulutusOid("1.2.246.562.13.00000000000000000003")
  val nonExistingOid          = KoulutusOid("1.2.246.562.13.00000000000000000000")

  "GET /:id" should s"get koulutus from elastic search" in {
    get(ammKoulutusOid, defaultSessionId)
  }

  it should s"return 404 if koulutus not found" in {
    get(s"$KoulutusPath/$nonExistingOid", headers = Seq(defaultSessionHeader)) {
      status should equal(404)
      body should include(s"Didn't find id $nonExistingOid")
    }
  }

  it should "return 401 without a valid session" in {
    get(s"$KoulutusPath/$nonExistingOid") {
      status should equal(401)
      body should include("Unauthorized")
    }
  }

  it should "get ammatillinen tutkinnon osa koulutus" in {
    get(ammTukinnonosaOid, defaultSessionId)
  }

  it should "get ammatillinen osaamisala koulutus" in {
    get(ammOsaamisalaOid, defaultSessionId)
  }

  it should "get muu ammatillinen koulutus" in {
    get(ammMuuOid, defaultSessionId)
  }
  it should "get aikuisten perusopetus -koulutus" in {
    get(aikuistenPerusopetusOid, defaultSessionId)
  }
}
