package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.domain.{Haku, Hakukohde, Koulutus, OdwHaku, Toteutus}
import fi.oph.kouta.internal.domain.enums.Julkaisutila
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid, KoulutusOid, ToteutusOid}
import fi.oph.kouta.internal.elasticsearch.{HakuClient, HakukohdeClient, KoulutusClient, ToteutusClient}
import fi.oph.kouta.internal.security.Authenticated

import java.time.LocalDate
import scala.concurrent.Future

class OdwService(
    hakuClient: HakuClient,
    hakukohdeClient: HakukohdeClient,
    koulutusClient: KoulutusClient,
    toteutusClient: ToteutusClient
) {
  def listAllHakuOids(modifiedDateStartFrom: Option[LocalDate], offset: Int, limit: Option[Int])(implicit
      authenticated: Authenticated
  ): Future[Seq[HakuOid]] =
    hakuClient.hakuOidsByJulkaisutila(
      Some(Seq(Julkaisutila.Julkaistu, Julkaisutila.Arkistoitu)),
      modifiedDateStartFrom,
      offset,
      limit
    )

  def listAllHakukohdeOids(modifiedDateStartFrom: Option[LocalDate], offset: Int, limit: Option[Int])(implicit
      authenticated: Authenticated
  ): Future[Seq[HakukohdeOid]] =
    hakukohdeClient.hakukohdeOidsByJulkaisutila(
      Some(Seq(Julkaisutila.Julkaistu, Julkaisutila.Arkistoitu)),
      modifiedDateStartFrom,
      offset,
      limit
    )

  def listAllKoulutusOids(modifiedDateStartFrom: Option[LocalDate], offset: Int, limit: Option[Int])(implicit
      authenticated: Authenticated
  ): Future[Seq[KoulutusOid]] =
    koulutusClient.koulutusOidsByJulkaisutila(
      Some(Seq(Julkaisutila.Julkaistu, Julkaisutila.Arkistoitu)),
      modifiedDateStartFrom,
      offset,
      limit
    )

  def listAllToteutusOids(modifiedDateStartFrom: Option[LocalDate], offset: Int, limit: Option[Int])(implicit
      authenticated: Authenticated
  ): Future[Seq[ToteutusOid]] =
    toteutusClient.toteutusOidsByJulkaisutila(
      Some(Seq(Julkaisutila.Julkaistu, Julkaisutila.Arkistoitu)),
      modifiedDateStartFrom,
      offset,
      limit
    )

  def findHautByOids(hakuOids: Set[HakuOid])(implicit authenticated: Authenticated): Future[Seq[Haku]] =
    hakuClient.findByOids(hakuOids)

  def findOdwHautByOids(hakuOids: Set[HakuOid])(implicit authenticated: Authenticated): Future[Seq[OdwHaku]] =
    hakuClient.findOdwHautByOids(hakuOids)

  def findHakukohteetByOids(hakukohteetOids: Set[HakukohdeOid])(implicit
      authenticated: Authenticated
  ): Future[Seq[Hakukohde]] =
    hakukohdeClient.findByOids(hakukohteetOids)

  def findKoulutuksetByOids(koulutusOids: Set[KoulutusOid])(implicit
      authenticated: Authenticated
  ): Future[Seq[Koulutus]] =
    koulutusClient.findByOids(koulutusOids)

  def findToteutuksetByOids(toteutusOids: Set[ToteutusOid])(implicit
      authenticated: Authenticated
  ): Future[Seq[Toteutus]] =
    toteutusClient.findByOids(toteutusOids)
}

object OdwService extends OdwService(HakuClient, HakukohdeClient, KoulutusClient, ToteutusClient)
