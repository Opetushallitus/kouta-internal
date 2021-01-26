package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.client.OrganisaatioClient
import fi.oph.kouta.internal.domain.Haku
import fi.oph.kouta.internal.domain.oid.{HakuOid, OrganisaatioOid}
import fi.oph.kouta.internal.elasticsearch.HakuClient
import fi.oph.kouta.internal.security.Authenticated

import scala.concurrent.Future

class HakuService(hakuClient: HakuClient) {
  def get(oid: HakuOid)(implicit authenticated: Authenticated): Future[Haku] =
    hakuClient.getHaku(oid)

  def search(ataruId: Option[String], tarjoajaOids: Option[Set[OrganisaatioOid]])(implicit
      authenticated: Authenticated
  ): Future[Seq[Haku]] =
    hakuClient.search(ataruId, tarjoajaOids.flatMap(OrganisaatioClient.getAllChildOidsFlat))
}

object HakuService extends HakuService(HakuClient)
