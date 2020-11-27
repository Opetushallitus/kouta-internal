package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.domain.Koulutus
import fi.oph.kouta.internal.domain.oid.KoulutusOid
import fi.oph.kouta.internal.elasticsearch.KoulutusClient
import fi.oph.kouta.internal.security.Authenticated

import scala.concurrent.Future

class KoulutusService(koulutusClient: KoulutusClient) {
  def get(oid: KoulutusOid)(implicit authenticated: Authenticated): Future[Koulutus] =
    koulutusClient.getKoulutus(oid)
}

object KoulutusService extends KoulutusService(KoulutusClient)
