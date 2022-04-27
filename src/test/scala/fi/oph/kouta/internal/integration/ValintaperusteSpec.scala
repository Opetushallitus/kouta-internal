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
  override val existingId: UUID    = UUID.fromString("fa7fcb96-3f80-4162-8d19-5b74731cf90c")
  override val nonExistingId: UUID = UUID.fromString("cc76da4a-d4cb-4ef2-a5d1-34b14c1a64bd")

  getTests()
}
