package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.domain.Hakukohde
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid}
import fi.oph.kouta.internal.elasticsearch.{ElasticsearchClientHolder, HakukohdeClient}
import fi.oph.kouta.internal.security.{Authenticated, Role, RoleEntity}
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HakukohdeService(elasticsearchClientHolder: ElasticsearchClientHolder, hakuService: HakuService)
    extends RoleEntityAuthorizationService
    with Logging {

  override val roleEntity: RoleEntity = Role.Hakukohde

  val hakukohdeClient = new HakukohdeClient("hakukohde-kouta", elasticsearchClientHolder)

  def get(oid: HakukohdeOid)(implicit authenticated: Authenticated): Future[Hakukohde] =
    authorizeGet(hakukohdeClient.getHakukohde(oid))

  def searchByHakuAndTarjoaja(hakuOid: Option[HakuOid], tarjoajaOid: Option[OrganisaatioOid])(implicit authenticated: Authenticated): Future[Seq[Hakukohde]] = {
    val checkHakuExists = hakuOid.fold(Future.successful(()))(hakuService.get(_).map(_ => ()))
    checkHakuExists.flatMap(_ => hakukohdeClient.searchByHakuAndTarjoaja(hakuOid, tarjoajaOid))
  }
}
