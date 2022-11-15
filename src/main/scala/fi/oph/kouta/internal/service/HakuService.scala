package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.client.OrganisaatioClient
import fi.oph.kouta.internal.domain.Haku
import fi.oph.kouta.internal.domain.oid.{HakuOid, OrganisaatioOid}
import fi.oph.kouta.internal.elasticsearch.HakuClient
import fi.oph.kouta.internal.security.Authenticated

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HakuService(hakuClient: HakuClient) {
  def get(oid: HakuOid)(implicit authenticated: Authenticated): Future[Haku] =
    hakuClient.getHaku(oid)

  def search(
      ataruId: Option[String],
      tarjoajaOids: Option[Set[OrganisaatioOid]],
      vuosi: Option[Int] = None,
      includeHakukohdeOids: Boolean = false
  )(implicit
      authenticated: Authenticated
  ): Future[Seq[Haku]] =
    OrganisaatioClient
      .asyncGetAllChildOidsFlat(tarjoajaOids)
      .flatMap(oids => hakuClient.search(ataruId, oids, vuosi, includeHakukohdeOids))

}

object HakuService extends HakuService(HakuClient)
