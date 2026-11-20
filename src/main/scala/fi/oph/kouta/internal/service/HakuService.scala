package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.KoutaConfigurationFactory
import fi.oph.kouta.internal.client.{HakukohderyhmaClient, OrganisaatioClient}
import fi.oph.kouta.internal.domain.Haku
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid, HakukohderyhmaOid, OrganisaatioOid}
import fi.oph.kouta.internal.elasticsearch.HakuClient
import fi.oph.kouta.internal.security.Authenticated

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HakuService(hakuClient: HakuClient, hakukohderyhmaClient: HakukohderyhmaClient = new HakukohderyhmaClient) {
  def get(oid: HakuOid)(implicit authenticated: Authenticated): Future[Haku] =
    hakuClient.getHaku(oid)

  def search(
      ataruId: Option[String],
      tarjoajaOids: Option[Set[OrganisaatioOid]],
      vuosi: Option[Int] = None,
      includeHakukohdeOids: Boolean = false,
      hakukohderyhmaOids: Option[Set[HakukohderyhmaOid]] = None
  )(implicit
      authenticated: Authenticated
  ): Future[Seq[Haku]] = {
    val rootOrganisaatioOid = KoutaConfigurationFactory.configuration.securityConfiguration.rootOrganisaatio
    val withRootOikeus      = tarjoajaOids.exists(_.contains(rootOrganisaatioOid))
    // Juuriorganisaation tarjoajarajaus kattaa kaikki haut myös ryhmärajauksen kanssa.
    val ryhmaRajaus = if (withRootOikeus) None else hakukohderyhmaOids

    for {
      tarjoajat <- OrganisaatioClient.asyncGetAllChildOidsFlat(tarjoajaOids)
      hakukohteet <- ryhmaRajaus match {
        case None => Future.successful(Option.empty[Set[HakukohdeOid]])
        case Some(oids) =>
          Future
            .sequence(oids.toSeq.map(hakukohderyhmaClient.getHakukohteet))
            .map(results => Some(results.flatten.toSet))
      }
      haut <- hakuClient.search(ataruId, tarjoajat, vuosi, includeHakukohdeOids, hakukohteet)
    } yield haut
  }

}

object HakuService extends HakuService(HakuClient, new HakukohderyhmaClient)
