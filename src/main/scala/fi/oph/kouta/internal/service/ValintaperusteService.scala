package fi.oph.kouta.internal.service

import java.util.UUID

import fi.oph.kouta.internal.domain.Valintaperuste
import fi.oph.kouta.internal.elasticsearch.ValintaperusteClient
import fi.oph.kouta.internal.security.{Authenticated, Role, RoleEntity}

import scala.concurrent.Future

class ValintaperusteService(valintaperusteClient: ValintaperusteClient)
    extends RoleEntityAuthorizationService {

  override val roleEntity: RoleEntity = Role.Valintaperuste

  def get(id: UUID)(implicit authenticated: Authenticated): Future[Valintaperuste] =
    authorizeGet(valintaperusteClient.getValintaperuste(id))
}

object ValintaperusteService extends ValintaperusteService(ValintaperusteClient)