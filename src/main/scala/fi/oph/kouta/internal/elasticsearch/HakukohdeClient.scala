package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.http.ElasticClient
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import com.sksamuel.elastic4s.searches.queries.BoolQuery
import fi.oph.kouta.internal.domain.enums.Kieli.Fi
import fi.oph.kouta.internal.domain.{Hakukohde, Kielistetty}
import fi.oph.kouta.internal.domain.indexed.HakukohdeIndexed
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid}
import fi.oph.kouta.internal.util.KoutaJsonFormats
import fi.vm.sade.utils.slf4j.Logging

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
    val qQuery = q.map(q =>
      should(
        matchQuery("nimi.fi", q),
        matchQuery("nimi.sv", q),
        matchQuery("nimi.en", q),
        matchQuery("jarjestyspaikka.nimi.fi", q),
        matchQuery("jarjestyspaikka.nimi.sv", q),
        matchQuery("jarjestyspaikka.nimi.en", q),
        matchQuery("toteutus.tarjoajat.nimi.fi", q),
        matchQuery("toteutus.tarjoajat.nimi.sv", q),
        matchQuery("toteutus.tarjoajat.nimi.en", q)
      ).minimumShouldMatch(1)
    )
    //
    implicit val userOrdering: Ordering[Kielistetty] = Ordering.by(_.get(Fi))

    searchItems[HakukohdeIndexed](Some(must(hakuQuery ++ tarjoajaQuery ++ qQuery)))
      .map(_.map(_.toHakukohde(oikeusHakukohteeseenFn)))
      .map(res => res.sortBy(hk => hk.organisaatioNimi))
  }

  def findByOids(
      hakukohteetOids: Set[HakukohdeOid],
      oikeusHakukohteeseenFn: OrganisaatioOid => Option[Boolean]
  ): Future[Seq[Hakukohde]] = {
    val hakukohteetQuery = should(termsQuery("oid", hakukohteetOids.map(_.toString)))
    searchItems[HakukohdeIndexed](Some(must(hakukohteetQuery))).map(_.map(_.toHakukohde(oikeusHakukohteeseenFn)))
  }
}

object HakukohdeClient extends HakukohdeClient("hakukohde-kouta", ElasticsearchClient.client)
