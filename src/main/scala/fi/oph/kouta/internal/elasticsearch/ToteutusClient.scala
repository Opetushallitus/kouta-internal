package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.internal.domain.Toteutus
import fi.oph.kouta.internal.domain.indexed.ToteutusIndexed
import fi.oph.kouta.internal.domain.oid.ToteutusOid
import fi.oph.kouta.internal.util.KoutaJsonFormats
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ToteutusClient(val index: String, val client: ElasticClient)
    extends KoutaJsonFormats
    with Logging
    with ElasticsearchClient {
  def getToteutus(oid: ToteutusOid): Future[Toteutus] =
    getItem[ToteutusIndexed](oid.s).map(_.toToteutus)
}

object ToteutusClient extends ToteutusClient("toteutus-kouta", ElasticsearchClient.client)
