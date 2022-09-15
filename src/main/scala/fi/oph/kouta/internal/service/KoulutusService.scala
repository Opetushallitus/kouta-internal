package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.domain.Koulutus
import fi.oph.kouta.internal.domain.oid.{HakuOid, KoulutusOid}
import fi.oph.kouta.internal.elasticsearch.KoulutusClient
import fi.oph.kouta.internal.security.Authenticated

import scala.concurrent.Future

class KoulutusService(koulutusClient: KoulutusClient) {
  def get(oid: KoulutusOid)(implicit authenticated: Authenticated): Future[Koulutus] =
    koulutusClient.getKoulutus(oid)

  def getByHakuOid(oid: HakuOid)(implicit authenticated: Authenticated): Future[Seq[Koulutus]] =
    koulutusClient.getKoulutusByHakuOid(oid)
}

object KoulutusService extends KoulutusService(KoulutusClient)
