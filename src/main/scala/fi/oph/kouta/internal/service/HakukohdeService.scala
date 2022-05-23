package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.KoutaConfigurationFactory
import fi.oph.kouta.internal.client.{HakukohderyhmaClient, OrganisaatioClient}
import fi.oph.kouta.internal.domain.{Hakukohde, Kielistetty}
import fi.oph.kouta.internal.domain.enums.Kieli.{En, Fi, Sv}
import fi.oph.kouta.internal.domain.indexed.KoodiUri
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid, HakukohderyhmaOid, OrganisaatioOid}
import fi.oph.kouta.internal.elasticsearch.HakukohdeClient
import fi.oph.kouta.internal.security.Authenticated
import fi.vm.sade.utils.slf4j.Logging

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class HakukohdeService(
    hakukohdeClient: HakukohdeClient,
    hakuService: HakuService,
    hakukohderyhmaClient: HakukohderyhmaClient
) extends Logging {
  private val rootOrganisaatioOid = KoutaConfigurationFactory.configuration.securityConfiguration.rootOrganisaatio

  private def createOikeusFn(
      withRootOikeus: Boolean,
      tarjoajaOids: Option[Set[OrganisaatioOid]]
  ): OrganisaatioOid => Option[Boolean] = { (tarjoaja: OrganisaatioOid) =>
    {
      if (withRootOikeus) {
        Some(true)
      } else {
        tarjoajaOids.map(oids => oids.contains(tarjoaja))
      }
    }
  }

  def get(oid: HakukohdeOid)(implicit authenticated: Authenticated): Future[Hakukohde] =
    hakukohdeClient.getHakukohde(oid)

  def search(
      hakuOid: Option[HakuOid],
      tarjoajaOids: Option[Set[OrganisaatioOid]],
      hakukohdeKoodiUri: Option[KoodiUri],
      hakukohderyhmaOids: Option[Set[HakukohderyhmaOid]],
      q: Option[String],
      all: Boolean
  )(implicit
      authenticated: Authenticated
  ): Future[Seq[Hakukohde]] = {
    val checkHakuExists = hakuOid.fold(Future.successful(()))(hakuService.get(_).map(_ => ()))
    val withRootOikeus  = tarjoajaOids.exists(_.contains(rootOrganisaatioOid))
    checkHakuExists
      .flatMap(_ => OrganisaatioClient.asyncGetAllChildOidsFlat(tarjoajaOids))
      .flatMap(oidsWithChilds => {

        val hakukohderyhmanHakukohdeOids: Option[Set[HakukohdeOid]] = hakukohderyhmaOids match {
          case None => None
          case Some(oids) =>
            Some(
              Await
                .result(
                  Future.sequence(
                    oids.map(oid =>
                      hakukohderyhmaClient
                        .getHakukohteet(oid)
                    )
                  ),
                  Duration(5, TimeUnit.SECONDS)
                )
                .flatMap(_.toSet)
            )
        }

        val hakukohteet = hakukohdeClient
          .search(
            hakuOid,
            if (all) None else oidsWithChilds,
            hakukohdeKoodiUri,
            q,
            createOikeusFn(withRootOikeus, oidsWithChilds),
            hakukohderyhmanHakukohdeOids
          )

        for {
          hk <- hakukohteet
        } yield {
          implicit val userOrdering: Ordering[Kielistetty] = Ordering.by(hk => (hk.get(Fi), hk.get(Sv), hk.get(En)))
          hk.sortBy(h => h.organisaatioNimi)
        }
      })
  }

  def findByOids(tarjoajaOids: Option[Set[OrganisaatioOid]], hakukohdeOids: Set[HakukohdeOid])(implicit
      authenticated: Authenticated
  ): Future[Seq[Hakukohde]] = {
    val withRootOikeus = tarjoajaOids.exists(_.contains(rootOrganisaatioOid))
    OrganisaatioClient
      .asyncGetAllChildOidsFlat(tarjoajaOids)
      .flatMap(oids => hakukohdeClient.findByOids(hakukohdeOids, createOikeusFn(withRootOikeus, oids)))
  }
}

object HakukohdeService extends HakukohdeService(HakukohdeClient, HakuService, new HakukohderyhmaClient)
