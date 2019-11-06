package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.domain.Haku
import fi.oph.kouta.internal.domain.oid.HakuOid
import fi.oph.kouta.internal.elasticsearch.HakuClient
import fi.oph.kouta.internal.security.{Authenticated, Role, RoleEntity}
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.Future

object HakuService extends RoleEntityAuthorizationService with Logging {

  override val roleEntity: RoleEntity = Role.Haku

  def get(oid: HakuOid)(implicit authenticated: Authenticated): Future[Haku] =
    authorizeGet(HakuClient.getHaku(oid))

}
