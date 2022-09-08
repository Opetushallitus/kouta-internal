package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.ElasticDateMath
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import com.sksamuel.elastic4s.requests.searches.queries.Query
import fi.oph.kouta.internal.domain.{Haku, OdwHaku}
import fi.oph.kouta.internal.domain.enums.Julkaisutila
import fi.oph.kouta.internal.domain.enums.Julkaisutila.Tallennettu
import fi.oph.kouta.internal.domain.indexed.HakuIndexed
import fi.oph.kouta.internal.domain.oid.{HakuOid, OrganisaatioOid}
import fi.oph.kouta.internal.util.KoutaJsonFormats
import fi.vm.sade.utils.slf4j.Logging

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HakuClient(val index: String, val client: ElasticClient)
    extends KoutaJsonFormats
    with Logging
    with ElasticsearchClient {
  def getHaku(oid: HakuOid): Future[Haku] =
    getItem[HakuIndexed](oid.s)
      .map(_.toHaku)

  private def byTarjoajaAndTila(tarjoajaOids: Option[Set[OrganisaatioOid]], haku: HakuIndexed): Boolean =
    tarjoajaOids.fold(true)(oids =>
      haku.hakukohteet.exists(hakukohde => {
        hakukohde.tila match {
          case Tallennettu => false
          case _ =>
            hakukohde.jarjestyspaikka.fold(hakukohde.toteutus.tarjoajat.exists(t => oids.contains(t.oid)))(j =>
              oids.contains(j.oid)
            )
        }
      })
    )

  def search(ataruId: Option[String], tarjoajaOids: Option[Set[OrganisaatioOid]], vuosi: Option[Int]): Future[Seq[Haku]] = {
    val ataruIdQuery = ataruId.map(termsQuery("hakulomakeAtaruId.keyword", _))
    val alkamisvuosiQuery = vuosi.map(termsQuery("alkamisvuosi.keyword", _))
    val hakuvuosiQuery = vuosi.map(termsQuery("hakuvuosi.keyword", _))
    val tarjoajaQuery = tarjoajaOids.map(oids =>
      should(
        oids.map(oid =>
          should(
            termsQuery("hakukohteet.jarjestyspaikka.oid", oid.toString),
            termsQuery("hakukohteet.toteutus.tarjoajat.oid", oid.toString)
          )
        )
      )
    )
    val query = ataruIdQuery ++ tarjoajaQuery ++ alkamisvuosiQuery ++ hakuvuosiQuery
    searchItems[HakuIndexed](if (query.isEmpty) None else Some(must(query)))
      .map(_.filter(byTarjoajaAndTila(tarjoajaOids, _)).map(_.toHaku))
  }

  def hakuOidsByJulkaisutila(
      julkaisuTilat: Option[Seq[Julkaisutila]],
      modifiedDateStartFrom: Option[LocalDate],
      offset: Int,
      limit: Option[Int]
  ): Future[Seq[HakuOid]] = {
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
    searchItemBulks[HakuIndexed](
      if (allQueries.isEmpty) None
      else
        Some(must(allQueries)),
      offset,
      limit
    ).map(_.map(_.oid))
  }

  def findByOids(hakuOids: Set[HakuOid]): Future[Seq[Haku]] = {
    val hakuQuery = should(termsQuery("oid", hakuOids.map(_.toString)))
    findHakuIndexedByOids(hakuOids).map(_.map(_.toHaku))
  }

  def findOdwHautByOids(hakuOids: Set[HakuOid]): Future[Seq[OdwHaku]] = {
    findHakuIndexedByOids(hakuOids).map(_.map(_.toOdwHaku))
  }

  def findHakuIndexedByOids(hakuOids: Set[HakuOid]): Future[Seq[HakuIndexed]] = {
    val hakuQuery = should(termsQuery("oid", hakuOids.map(_.toString)))
    searchItemBulks[HakuIndexed](Some(must(hakuQuery)), 0, None)
  }
}

object HakuClient extends HakuClient("haku-kouta", ElasticsearchClient.client)
