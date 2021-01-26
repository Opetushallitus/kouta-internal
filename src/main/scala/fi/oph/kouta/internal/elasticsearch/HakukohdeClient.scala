package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.http.ElasticClient
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.internal.domain.Hakukohde
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
      q: Option[String]
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
        termsQuery("nimi.fi.keyword", q),
        termsQuery("nimi.sv.keyword", q),
        termsQuery("nimi.en.keyword", q),
        termsQuery("jarjestyspaikka.nimi.fi.keyword", q),
        termsQuery("jarjestyspaikka.nimi.sv.keyword", q),
        termsQuery("jarjestyspaikka.nimi.en.keyword", q),
        termsQuery("toteutus.tarjoajat.nimi.fi.keyword", q),
        termsQuery("toteutus.tarjoajat.nimi.sv.keyword", q),
        termsQuery("toteutus.tarjoajat.nimi.en.keyword", q)
      )
    )
    searchItems[HakukohdeIndexed](Some(must(hakuQuery ++ tarjoajaQuery ++ qQuery))).map(_.map(_.toHakukohde))
  }
}

object HakukohdeClient extends HakukohdeClient("hakukohde-kouta", ElasticsearchClient.client)
