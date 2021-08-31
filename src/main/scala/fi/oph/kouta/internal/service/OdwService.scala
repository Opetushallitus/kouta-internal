package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.domain.Haku
import fi.oph.kouta.internal.domain.enums.Julkaisutila
import fi.oph.kouta.internal.elasticsearch.HakuClient
import fi.oph.kouta.internal.security.Authenticated

import scala.concurrent.Future

class OdwService(hakuClient: HakuClient) {
  def listAllHaut(offset: Int, limit: Option[Int])(implicit authenticated: Authenticated): Future[Seq[Haku]] =
    hakuClient.hautByJulkaisutila(Some(Seq(Julkaisutila.Julkaistu, Julkaisutila.Arkistoitu)), offset, limit)

}

object OdwService extends OdwService(HakuClient)
