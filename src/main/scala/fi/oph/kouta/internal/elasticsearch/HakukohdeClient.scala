package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.ElasticDateMath
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import com.sksamuel.elastic4s.requests.searches.queries.Query
import fi.oph.kouta.internal.domain.Hakukohde
import fi.oph.kouta.internal.domain.enums.Kieli.Fi
import fi.oph.kouta.internal.domain.{Hakukohde, Kielistetty}
import fi.oph.kouta.internal.domain.indexed.HakukohdeIndexed
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid}
import fi.oph.kouta.internal.util.KoutaJsonFormats
import fi.vm.sade.utils.slf4j.Logging
import fi.oph.kouta.internal.domain.enums.Julkaisutila

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HakukohdeClient(val index: String, val client: ElasticClient)
    extends KoutaJsonFormats
    with Logging
    with ElasticsearchClient {
  def getHakukohde(oid: HakukohdeOid): Future[Hakukohde] =
    getItem[HakukohdeIndexed](oid.s).map(_.toHakukohde)

  def search(
      hakuOid: Option[HakuOid],
      tarjoajaOids: Option[Set[OrganisaatioOid]],
      q: Option[String],
      oikeusHakukohteeseenFn: OrganisaatioOid => Option[Boolean]
  ): Future[Seq[Hakukohde]] = {
    val hakuQuery = hakuOid.map(oid => termsQuery("hakuOid", oid.toString))
    val tarjoajaQuery = tarjoajaOids.map(oids =>
      should(
        oids.map(oid =>
          should(
            termsQuery("jarjestyspaikka.oid", oid.toString),
            not(existsQuery("jarjestyspaikka")).must(termsQuery("toteutus.tarjoajat.oid", oid.toString))
          )
        )
      )
    )
    val qQuery = q.map(q => {
      val wildcardQ = "*" + q + "*"
      should(
        wildcardQuery("nimi.fi", wildcardQ),
        wildcardQuery("nimi.sv", wildcardQ),
        wildcardQuery("nimi.en", wildcardQ),
        wildcardQuery("jarjestyspaikka.nimi.fi", wildcardQ),
        wildcardQuery("jarjestyspaikka.nimi.sv", wildcardQ),
        wildcardQuery("jarjestyspaikka.nimi.en", wildcardQ),
        wildcardQuery("toteutus.tarjoajat.nimi.fi", wildcardQ),
        wildcardQuery("toteutus.tarjoajat.nimi.sv", wildcardQ),
        wildcardQuery("toteutus.tarjoajat.nimi.en", wildcardQ)
      ).minimumShouldMatch(1)
    })
    //
    implicit val userOrdering: Ordering[Kielistetty] = Ordering.by(_.get(Fi))

    searchItems[HakukohdeIndexed](Some(must(hakuQuery ++ tarjoajaQuery ++ qQuery)))
      .map(_.map(_.toHakukohde(oikeusHakukohteeseenFn)))
      .map(res => res.sortBy(hk => hk.organisaatioNimi))
  }

  def hakukohdeOidsByJulkaisutila(
      julkaisuTilat: Option[Seq[Julkaisutila]],
      modifiedDateStartFrom: Option[LocalDate],
      offset: Int,
      limit: Option[Int]
  ): Future[Seq[HakukohdeOid]] = {
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

    searchItemBulks[HakukohdeIndexed](
      if (allQueries.isEmpty) None
      else
        Some(must(allQueries)),
      offset,
      limit
    ).map(_.map(_.oid))
  }

  def findByOids(
      hakukohteetOids: Set[HakukohdeOid],
      oikeusHakukohteeseenFn: OrganisaatioOid => Option[Boolean]
  ): Future[Seq[Hakukohde]] = {
    val hakukohteetQuery = should(termsQuery("oid", hakukohteetOids.map(_.toString)))
    searchItems[HakukohdeIndexed](Some(must(hakukohteetQuery))).map(_.map(_.toHakukohde(oikeusHakukohteeseenFn)))
  }

  def findByOids(hakukohteetOids: Set[HakukohdeOid]): Future[Seq[Hakukohde]] = {
    val hakukohteetQuery = should(termsQuery("oid", hakukohteetOids.map(_.toString)))
    searchItemBulks[HakukohdeIndexed](Some(must(hakukohteetQuery)), 0, None).map(_.map(_.toHakukohde))
  }
}

object HakukohdeClient extends HakukohdeClient("hakukohde-kouta", ElasticsearchClient.client)
