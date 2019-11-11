package fi.oph.kouta.internal.integration

import clojure.java.api.Clojure
import fi.oph.kouta.internal.domain.Haku
import fi.oph.kouta.internal.domain.oid.HakuOid
import fi.oph.kouta.internal.integration.fixture.HakuFixture

class HakuSpec extends HakuFixture {

  def symbol(name: String) = Clojure.read(s"'$name")

  "GET /:oid" should "get haku from elastic search" in {
    val hakuOid = HakuOid("1.2.246.562.29.00000000000000000009")

    addHaku(hakuOid)
    indexAll.invoke()

    val haku: Haku = get(hakuOid)

    haku.oid should not be empty
    haku.oid.get should equal(hakuOid)
  }
}
