package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.domain.Haku
import fi.oph.kouta.internal.domain.enums.Julkaisutila
import fi.oph.kouta.internal.elasticsearch.HakuClient

import scala.concurrent.Future

class OdwService(hakuClient: HakuClient) {
  def listAllHaut: Future[Seq[Haku]] = hakuClient.hautByJulkaisutila(Some(Seq(Julkaisutila.Julkaistu, Julkaisutila.Arkistoitu)))

}

object OdwService extends OdwService(HakuClient)
