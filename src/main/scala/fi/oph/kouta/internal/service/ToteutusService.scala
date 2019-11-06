package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.domain.Toteutus
import fi.oph.kouta.internal.domain.oid.ToteutusOid
import fi.oph.kouta.internal.elasticsearch.ToteutusClient
import fi.oph.kouta.internal.security.{Authenticated, Role, RoleEntity}
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.Future

object ToteutusService extends RoleEntityAuthorizationService with Logging {

  override val roleEntity: RoleEntity = Role.Toteutus

  def get(oid: ToteutusOid)(implicit authenticated: Authenticated): Future[Toteutus] =
    authorizeGet(ToteutusClient.getToteutus(oid))

}
