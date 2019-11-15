package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.domain.Toteutus
import fi.oph.kouta.internal.domain.oid.ToteutusOid
import fi.oph.kouta.internal.elasticsearch.{ElasticsearchClientHolder, ToteutusClient}
import fi.oph.kouta.internal.security.{Authenticated, Role, RoleEntity}
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.Future

class ToteutusService(elasticsearchClientHolder: ElasticsearchClientHolder)
    extends RoleEntityAuthorizationService
    with Logging {

  override val roleEntity: RoleEntity = Role.Toteutus

  val toteutusClient = new ToteutusClient("toteutus-kouta", elasticsearchClientHolder)

  def get(oid: ToteutusOid)(implicit authenticated: Authenticated): Future[Toteutus] =
    authorizeGet(toteutusClient.getToteutus(oid))

}
