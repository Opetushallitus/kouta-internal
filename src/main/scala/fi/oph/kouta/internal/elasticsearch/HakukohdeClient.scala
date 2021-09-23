package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.internal.domain.Hakukohde
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

  def findByOids(
      hakukohteetOids: Set[HakukohdeOid],
      oikeusHakukohteeseenFn: OrganisaatioOid => Option[Boolean]
  ): Future[Seq[Hakukohde]] = {
    val hakukohteetQuery = should(termsQuery("oid", hakukohteetOids.map(_.toString)))
    searchItems[HakukohdeIndexed](Some(must(hakukohteetQuery))).map(_.map(_.toHakukohde(oikeusHakukohteeseenFn)))
  }
}

object HakukohdeClient extends HakukohdeClient("hakukohde-kouta", ElasticsearchClient.client)
