package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.domain.Haku
import fi.oph.kouta.internal.domain.enums.Julkaisutila
import fi.oph.kouta.internal.domain.oid.HakuOid
import fi.oph.kouta.internal.elasticsearch.HakuClient
import fi.oph.kouta.internal.security.Authenticated

import scala.concurrent.Future

class OdwService(hakuClient: HakuClient) {
  def listAllHakuOids(offset: Int, limit: Option[Int])(implicit authenticated: Authenticated): Future[Seq[HakuOid]] =
    hakuClient.hakuOidsByJulkaisutila(Some(Seq(Julkaisutila.Julkaistu, Julkaisutila.Arkistoitu)), offset, limit)

  def findByOids(hakuOids: Set[HakuOid])(implicit authenticated: Authenticated): Future[Seq[Haku]] =
    hakuClient.findByOids(hakuOids)
}

object OdwService extends OdwService(HakuClient)
