package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.domain.Hakukohde
import fi.oph.kouta.internal.domain.oid.HakukohdeOid
import fi.oph.kouta.internal.elasticsearch.HakukohdeClient
import fi.oph.kouta.internal.security.{Authenticated, Role, RoleEntity}
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.Future

object HakukohdeService extends RoleEntityAuthorizationService with Logging {

  override val roleEntity: RoleEntity = Role.Hakukohde

  def get(oid: HakukohdeOid)(implicit authenticated: Authenticated): Future[Hakukohde] =
    authorizeGet(HakukohdeClient.getHakukohde(oid))

}
