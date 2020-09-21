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

  def searchByHakuAndTarjoaja(
      hakuOid: Option[HakuOid],
      tarjoajaOid: Option[OrganisaatioOid]
  ): Future[Seq[Hakukohde]] = {
    val hakuQuery = hakuOid.map(oid => matchQuery("hakuOid", oid.toString))
    val tarjoajaQuery = tarjoajaOid.map(oid =>
      should(
        matchQuery("tarjoajat.oid", oid.toString),
        not(existsQuery("tarjoajat")).must(matchQuery("toteutus.tarjoajat.oid", oid.toString))
      )
    )
    searchItems[HakukohdeIndexed](Some(must(hakuQuery ++ tarjoajaQuery))).map(_.map(_.toHakukohde))
  }
}

object HakukohdeClient extends HakukohdeClient("hakukohde-kouta", ElasticsearchClient.client)
