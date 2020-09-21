package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.domain.Toteutus
import fi.oph.kouta.internal.domain.oid.ToteutusOid
import fi.oph.kouta.internal.elasticsearch.ToteutusClient
import fi.oph.kouta.internal.security.{Authenticated, Role, RoleEntity}

import scala.concurrent.Future

class ToteutusService(toteutusClient: ToteutusClient) extends RoleEntityAuthorizationService {

  override val roleEntity: RoleEntity = Role.Toteutus

  def get(oid: ToteutusOid)(implicit authenticated: Authenticated): Future[Toteutus] =
    authorizeGet(toteutusClient.getToteutus(oid))
}

object ToteutusService extends ToteutusService(ToteutusClient)
