package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.ElasticApi.{must, rangeQuery, should, termsQuery}
import com.sksamuel.elastic4s.{ElasticClient, ElasticDateMath}
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import com.sksamuel.elastic4s.requests.searches.queries.Query
import fi.oph.kouta.internal.domain.Toteutus
import fi.oph.kouta.internal.domain.enums.Julkaisutila
import fi.oph.kouta.internal.domain.indexed.ToteutusIndexed
import fi.oph.kouta.internal.domain.oid.{HakuOid, ToteutusOid}
import fi.oph.kouta.internal.util.{ElasticCache, KoutaJsonFormats}
import fi.vm.sade.utils.slf4j.Logging

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.NoSuchElementException
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ToteutusClient(val index: String, val client: ElasticClient)
  extends KoutaJsonFormats
    with Logging
    with ElasticsearchClient {

  private val toteutusCache = ElasticCache[ToteutusOid, ToteutusIndexed]()

  def getToteutus(oid: ToteutusOid): Future[Toteutus] =
    findByOids(Set(oid)).flatMap {
      case r if r.nonEmpty =>
        Future.successful(r.head)
      case _ =>
        Future.failed(new NoSuchElementException(s"Toteutus not found from Elastic! Didn't find id $oid"))
    }

  def getToteutusByHakuOid(oid: HakuOid): Future[Seq[Toteutus]] = {
    val query = termsQuery("haut.keyword", oid.toString)
    searchItems[ToteutusIndexed](Some(must(query))).map(_.map(_.toToteutus))
  }

  private def findByOidsForReal(oids: Set[ToteutusOid]): Future[Seq[ToteutusIndexed]] = {
    val toteutusQuery = should(termsQuery("oid", oids.map(_.toString)))
    searchItemBulks[ToteutusIndexed](Some(must(toteutusQuery)), 0, None)
  }

  def findByOids(oids: Set[ToteutusOid]): Future[Seq[Toteutus]] = {
    toteutusCache
      .getMany(oids, missing => findByOidsForReal(missing.toSet).map(h => h.map(hh => hh.oid -> hh).toMap))
      .map(_.map(_.toToteutus))
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
