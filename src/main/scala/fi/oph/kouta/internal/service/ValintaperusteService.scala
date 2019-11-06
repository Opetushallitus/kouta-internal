package fi.oph.kouta.internal.service

import java.util.UUID

import fi.oph.kouta.internal.domain.Valintaperuste
import fi.oph.kouta.internal.elasticsearch.ValintaperusteClient
import fi.oph.kouta.internal.security.{Authenticated, Role, RoleEntity}
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.Future

object ValintaperusteService extends RoleEntityAuthorizationService with Logging {

  override val roleEntity: RoleEntity = Role.Valintaperuste

  def get(id: UUID)(implicit authenticated: Authenticated): Future[Valintaperuste] =
    authorizeGet(ValintaperusteClient.getValintaperuste(id))

}
