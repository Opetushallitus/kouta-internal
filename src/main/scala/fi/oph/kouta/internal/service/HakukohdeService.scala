package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.KoutaConfigurationFactory
import fi.oph.kouta.internal.client.OrganisaatioClient
import fi.oph.kouta.internal.domain.Hakukohde
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid}
import fi.oph.kouta.internal.elasticsearch.HakukohdeClient
import fi.oph.kouta.internal.security.Authenticated

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HakukohdeService(hakukohdeClient: HakukohdeClient, hakuService: HakuService) {
  private val rootOrganisaatioOid = KoutaConfigurationFactory.configuration.securityConfiguration.rootOrganisaatio

  private def createOikeusFn(
      withRootOikeus: Boolean,
      tarjoajaOids: Option[Set[OrganisaatioOid]]
  ): Set[OrganisaatioOid] => Option[Boolean] = { (tarjoajat: Set[OrganisaatioOid]) =>
    {
      if (withRootOikeus) {
        Some(true)
      } else {
        tarjoajaOids.map(oids => (oids & tarjoajat).nonEmpty)
      }
    }
  }

  def get(oid: HakukohdeOid)(implicit authenticated: Authenticated): Future[Hakukohde] =
    hakukohdeClient.getHakukohde(oid)

  def search(hakuOid: Option[HakuOid], tarjoajaOids: Option[Set[OrganisaatioOid]], q: Option[String], all: Boolean)(
      implicit authenticated: Authenticated
  ): Future[Seq[Hakukohde]] = {
    val checkHakuExists        = hakuOid.fold(Future.successful(()))(hakuService.get(_).map(_ => ()))
    val tarjoajaOidsWithChilds = tarjoajaOids.flatMap(OrganisaatioClient.getAllChildOidsFlat)
    val withRootOikeus         = tarjoajaOids.exists(_.contains(rootOrganisaatioOid))
    val oikeusHakukohteeseenFn = createOikeusFn(withRootOikeus, tarjoajaOidsWithChilds)
    checkHakuExists.flatMap(_ =>
      hakukohdeClient.search(hakuOid, if (all) None else tarjoajaOidsWithChilds, q, oikeusHakukohteeseenFn)
    )
  }

  def findByOids(tarjoajaOids: Option[Set[OrganisaatioOid]], hakukohdeOids: Set[HakukohdeOid])(implicit
      authenticated: Authenticated
  ): Future[Seq[Hakukohde]] = {
    val withRootOikeus = tarjoajaOids.exists(_.contains(rootOrganisaatioOid))
    val oikeusHakukohteeseenFn =
      createOikeusFn(withRootOikeus, tarjoajaOids.flatMap(OrganisaatioClient.getAllChildOidsFlat))
    hakukohdeClient.findByOids(hakukohdeOids, oikeusHakukohteeseenFn)
  }
}

object HakukohdeService extends HakukohdeService(HakukohdeClient, HakuService)
