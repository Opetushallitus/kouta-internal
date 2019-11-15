package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.internal.domain.Toteutus
import fi.oph.kouta.internal.domain.indexed.ToteutusIndexed
import fi.oph.kouta.internal.domain.oid.ToteutusOid
import fi.oph.kouta.internal.util.KoutaJsonFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ToteutusClient(override val index: String, elasticsearchClientHolder: ElasticsearchClientHolder)
  extends ElasticsearchClient(index, "toteutus", elasticsearchClientHolder)
    with KoutaJsonFormats {

  def getToteutus(oid: ToteutusOid): Future[Toteutus] =
    getItem(oid.s)
      .map(_.to[ToteutusIndexed])
      .map(_.toToteutus)
}
