package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.domain.Toteutus
import fi.oph.kouta.internal.domain.oid.{HakuOid, ToteutusOid}
import fi.oph.kouta.internal.elasticsearch.ToteutusClient
import fi.oph.kouta.internal.security.Authenticated

import scala.concurrent.Future

class ToteutusService(toteutusClient: ToteutusClient) {
  def get(oid: ToteutusOid)(implicit authenticated: Authenticated): Future[Toteutus] =
    toteutusClient.getToteutus(oid)

  def getByHakuOid(oid: HakuOid)(implicit authenticated: Authenticated): Future[Seq[Toteutus]] =
    toteutusClient.getToteutusByHakuOid(oid)
}

object ToteutusService extends ToteutusService(ToteutusClient)
