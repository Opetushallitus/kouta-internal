package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.domain.Hakukohde
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid}
import fi.oph.kouta.internal.elasticsearch.HakukohdeClient
import fi.oph.kouta.internal.security.{Authenticated, Role, RoleEntity}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HakukohdeService(hakukohdeClient: HakukohdeClient, hakuService: HakuService)
    extends RoleEntityAuthorizationService {

  override val roleEntity: RoleEntity = Role.Hakukohde

  def get(oid: HakukohdeOid)(implicit authenticated: Authenticated): Future[Hakukohde] =
    authorizeGet(hakukohdeClient.getHakukohde(oid))

  def searchByHakuAndTarjoaja(hakuOid: Option[HakuOid], tarjoajaOid: Option[OrganisaatioOid])(implicit
      authenticated: Authenticated
  ): Future[Seq[Hakukohde]] = {
    val checkHakuExists = hakuOid.fold(Future.successful(()))(hakuService.get(_).map(_ => ()))
    checkHakuExists.flatMap(_ => hakukohdeClient.searchByHakuAndTarjoaja(hakuOid, tarjoajaOid))
  }
}

object HakukohdeService extends HakukohdeService(HakukohdeClient, HakuService)
