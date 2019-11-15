package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.domain.Hakukohde
import fi.oph.kouta.internal.domain.oid.HakukohdeOid
import fi.oph.kouta.internal.elasticsearch.{ElasticsearchClientHolder, HakukohdeClient}
import fi.oph.kouta.internal.security.{Authenticated, Role, RoleEntity}
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.Future

class HakukohdeService(elasticsearchClientHolder: ElasticsearchClientHolder)
    extends RoleEntityAuthorizationService
    with Logging {

  override val roleEntity: RoleEntity = Role.Hakukohde

  val hakukohdeClient = new HakukohdeClient("hakukohde-kouta", elasticsearchClientHolder)

  def get(oid: HakukohdeOid)(implicit authenticated: Authenticated): Future[Hakukohde] =
    authorizeGet(hakukohdeClient.getHakukohde(oid))

}
