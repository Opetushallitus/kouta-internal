package fi.oph.kouta.internal.integration

import java.util.UUID

import fi.oph.kouta.internal.domain.Valintaperuste
import fi.oph.kouta.internal.integration.fixture.{AccessControlSpec, ValintaperusteFixture}
import fi.oph.kouta.internal.security.Role

class ValintaperusteSpec
    extends ValintaperusteFixture
    with AccessControlSpec
    with GenericGetTests[Valintaperuste, UUID] {

  override val roleEntities        = Seq(Role.Valintaperuste)
  override val getPath: String     = ValintaperustePath
  override val entityName: String  = "valintaperuste"
  override val existingId: UUID    = UUID.fromString("03715370-2c2e-40b1-adf9-4de9e4eb3c73")
  override val nonExistingId: UUID = UUID.fromString("cc76da4a-d4cb-4ef2-a5d1-34b14c1a64bd")

  override def beforeAll(): Unit = {
    super.beforeAll()
    addMockValintaperuste(existingId, ChildOid)
  }

  getTests()
}
