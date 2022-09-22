package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.ElasticApi.{must, rangeQuery, should, termsQuery}
import com.sksamuel.elastic4s.{ElasticClient, ElasticDateMath}
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import com.sksamuel.elastic4s.requests.searches.queries.Query
import fi.oph.kouta.internal.domain.enums.Julkaisutila
import fi.oph.kouta.internal.domain.indexed.KoulutusIndexed
import fi.oph.kouta.internal.domain.oid.KoulutusOid
import fi.oph.kouta.internal.domain.{Koulutus, OdwKoulutus}
import fi.oph.kouta.internal.util.{ElasticCache, KoutaJsonFormats}
import fi.vm.sade.utils.slf4j.Logging

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class KoulutusClient(val index: String, val client: ElasticClient)
  extends KoutaJsonFormats
    with Logging
    with ElasticsearchClient {

  private val koulutusCache = ElasticCache[KoulutusOid, KoulutusIndexed]()

  def getKoulutus(oid: KoulutusOid): Future[Koulutus] =
    findByOidsIndexed(Set(oid)).flatMap {
      case r if r.nonEmpty =>
        Future.successful(r.head.toKoulutus)
      case _ =>
        Future.failed(new RuntimeException(s"Koulutus not found from Elastic with oid $oid"))
    }

  def koulutusOidsByJulkaisutila(julkaisuTilat: Option[Seq[Julkaisutila]],
                                 modifiedDateStartFrom: Option[LocalDate],
                                 offset: Int,
                                 limit: Option[Int]
                                ): Future[Seq[KoulutusOid]] = {
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

    searchItemBulks[KoulutusIndexed](
      if (allQueries.isEmpty) None
      else
        Some(must(allQueries)),
      offset,
      limit
    ).map(_.map(_.oid))
  }

  private def findByOidsForReal(oids: Set[KoulutusOid]): Future[Seq[KoulutusIndexed]] = {
    val koulutusQuery = should(termsQuery("oid", oids.map(_.toString)))
    searchItemBulks[KoulutusIndexed](Some(must(koulutusQuery)), 0, None)
  }

  private def findByOidsIndexed(oids: Set[KoulutusOid]): Future[Seq[KoulutusIndexed]] =
    koulutusCache.getMany(oids, missing => findByOidsForReal(missing.toSet)
      .map(h => h.map(hh => hh.oid -> hh).toMap))

  def findByOidsFor(oids: Set[KoulutusOid]): Future[Seq[Koulutus]] =
    findByOidsIndexed(oids).map(_.map(_.toKoulutus))

  def findOdwByOids(oids: Set[KoulutusOid]): Future[Seq[OdwKoulutus]] =
    findByOidsIndexed(oids).map(_.map(_.toOdwKoulutus))
}

object KoulutusClient extends KoulutusClient("koulutus-kouta", ElasticsearchClient.client)
