package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.client.OrganisaatioClient
import fi.oph.kouta.internal.domain.Hakukohde
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid}
import fi.oph.kouta.internal.elasticsearch.HakukohdeClient
import fi.oph.kouta.internal.security.Authenticated

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HakukohdeService(hakukohdeClient: HakukohdeClient, hakuService: HakuService) {
  def get(oid: HakukohdeOid)(implicit authenticated: Authenticated): Future[Hakukohde] =
    hakukohdeClient.getHakukohde(oid)

  def search(hakuOid: Option[HakuOid], tarjoajaOids: Option[Set[OrganisaatioOid]], q: Option[String])(implicit
      authenticated: Authenticated
  ): Future[Seq[Hakukohde]] = {
    val checkHakuExists = hakuOid.fold(Future.successful(()))(hakuService.get(_).map(_ => ()))
    checkHakuExists.flatMap(_ =>
      hakukohdeClient.search(hakuOid, tarjoajaOids.flatMap(OrganisaatioClient.getAllChildOidsFlat), q)
    )
  }
}

object HakukohdeService extends HakukohdeService(HakukohdeClient, HakuService)
