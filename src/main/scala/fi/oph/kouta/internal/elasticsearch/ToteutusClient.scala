package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.internal.domain.Toteutus
import fi.oph.kouta.internal.domain.indexed.ToteutusIndexed
import fi.oph.kouta.internal.domain.oid.ToteutusOid
import fi.oph.kouta.internal.util.KoutaJsonFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ToteutusClient extends ToteutusClient("toteutus-kouta")

abstract class ToteutusClient(override val index: String)
  extends ElasticsearchClient(index, "toteutus")
    with KoutaJsonFormats {

  def getToteutus(oid: ToteutusOid): Future[Toteutus] =
    getItem(oid.s)
      .map(_.to[ToteutusIndexed])
      .map(_.toToteutus)
}
