package fi.oph.kouta.internal.integration

import fi.oph.kouta.internal.domain.Haku
import fi.oph.kouta.internal.domain.oid.HakuOid
import fi.oph.kouta.internal.integration.fixture.{AccessControlSpec, HakuFixture}
import fi.oph.kouta.internal.security.Role

class HakuSpec extends HakuFixture with AccessControlSpec with GenericGetTests[Haku, HakuOid] {

  override val roleEntities = Seq(Role.Haku)
  override val getPath      = HakuPath
  override val entityName   = "haku"
  val existingId            = HakuOid("1.2.246.562.29.00000000000000000009")
  val nonExistingId         = HakuOid("1.2.246.562.29.0")

  override def beforeAll(): Unit = {
    super.beforeAll()
    addMockHaku(existingId, ChildOid)
  }

  getTests()
}
