package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.ElasticApi.{must, rangeQuery}
import com.sksamuel.elastic4s.ElasticDateMath
import com.sksamuel.elastic4s.http.ElasticClient
import com.sksamuel.elastic4s.http.ElasticDsl.{BuildableTermsNoOp, should, termsQuery}
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import com.sksamuel.elastic4s.searches.queries.Query
import fi.oph.kouta.internal.domain.Toteutus
import fi.oph.kouta.internal.domain.enums.Julkaisutila
import fi.oph.kouta.internal.domain.indexed.ToteutusIndexed
import fi.oph.kouta.internal.domain.oid.ToteutusOid
import fi.oph.kouta.internal.util.KoutaJsonFormats
import fi.vm.sade.utils.slf4j.Logging

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ToteutusClient(val index: String, val client: ElasticClient)
    extends KoutaJsonFormats
    with Logging
    with ElasticsearchClient {
  def getToteutus(oid: ToteutusOid): Future[Toteutus] =
    getItem[ToteutusIndexed](oid.s).map(_.toToteutus)

  def findByOids(oids: Set[ToteutusOid]): Future[Seq[Toteutus]] = {
    val toteutusQuery = should(termsQuery("oid", oids.map(_.toString)))
    searchItemBulks[ToteutusIndexed](Some(must(toteutusQuery)), 0, None).map(_.map(_.toToteutus))
  }

  def toteutusOidsByJulkaisutila(
      julkaisuTilat: Option[Seq[Julkaisutila]],
      modifiedDateStartFrom: Option[LocalDate],
      offset: Int,
      limit: Option[Int]
  ): Future[Seq[ToteutusOid]] = {
    var allQueries: List[Query] = List()
    if (julkaisuTilat.isDefined) {
      allQueries ++= julkaisuTilat.map(tilat =>
        should(
          tilat.map(tila =>
            should(
              termsQuery("tila.keyword", tila.name)
            )
          )
        )
      )
    }
    if (modifiedDateStartFrom.isDefined) {
      allQueries ++= Some(
        rangeQuery("modified").gt(ElasticDateMath(modifiedDateStartFrom.get.format(DateTimeFormatter.ISO_LOCAL_DATE)))
      )
    }

    searchItemBulks[ToteutusIndexed](
      if (allQueries.isEmpty) None
      else
        Some(must(allQueries)),
      offset,
      limit
    ).map(_.map(_.oid))
  }
}

object ToteutusClient extends ToteutusClient("toteutus-kouta", ElasticsearchClient.client)
