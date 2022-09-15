package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.ElasticApi.{must, rangeQuery, should, termsQuery}
import com.sksamuel.elastic4s.ElasticDateMath
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import com.sksamuel.elastic4s.requests.searches.queries.Query
import fi.oph.kouta.internal.domain.{Koulutus, OdwKoulutus}
import fi.oph.kouta.internal.domain.enums.Julkaisutila
import fi.oph.kouta.internal.domain.indexed.KoulutusIndexed
import fi.oph.kouta.internal.domain.oid.{HakuOid, KoulutusOid}
import fi.oph.kouta.internal.util.KoutaJsonFormats
import fi.vm.sade.utils.slf4j.Logging

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class KoulutusClient(val index: String, val client: ElasticClient)
    extends KoutaJsonFormats
    with Logging
    with ElasticsearchClient {
  def getKoulutus(oid: KoulutusOid): Future[Koulutus] =
    getItem[KoulutusIndexed](oid.s).map(_.toKoulutus)

  def getKoulutusByHakuOid(oid: HakuOid): Future[Seq[Koulutus]] = {
    val query = termsQuery("haut.keyword", oid)
    searchItems[KoulutusIndexed](Some(must(query))).map(_.map(_.toKoulutus))
  }

  def koulutusOidsByJulkaisutila(
      julkaisuTilat: Option[Seq[Julkaisutila]],
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

  def findByOids(oids: Set[KoulutusOid]): Future[Seq[Koulutus]] = {
    val koulutusQuery = should(termsQuery("oid", oids.map(_.toString)))
    searchItemBulks[KoulutusIndexed](Some(must(koulutusQuery)), 0, None).map(_.map(_.toKoulutus))
  }

  def findOdwByOids(oids: Set[KoulutusOid]): Future[Seq[OdwKoulutus]] = {
    val koulutusQuery = should(termsQuery("oid", oids.map(_.toString)))
    searchItemBulks[KoulutusIndexed](Some(must(koulutusQuery)), 0, None).map(_.map(_.toOdwKoulutus))
  }
}

object KoulutusClient extends KoulutusClient("koulutus-kouta", ElasticsearchClient.client)
