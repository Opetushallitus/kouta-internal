package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.search.SearchResponse
import com.sksamuel.elastic4s.http.{RequestFailure, RequestSuccess}
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.internal.domain.Hakukohde
import fi.oph.kouta.internal.domain.indexed.HakukohdeIndexed
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid}
import fi.oph.kouta.internal.util.KoutaJsonFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HakukohdeClient(override val index: String, elasticsearchClientHolder: ElasticsearchClientHolder)
  extends ElasticsearchClient(index, "hakukohde", elasticsearchClientHolder)
    with KoutaJsonFormats {

  def getHakukohde(oid: HakukohdeOid): Future[Hakukohde] =
    getItem(oid.s)
      .map(_.to[HakukohdeIndexed])
      .map(_.toHakukohde)

  def searchByHakuAndTarjoaja(hakuOid: Option[HakuOid], tarjoajaOid: Option[OrganisaatioOid]): Future[Seq[Hakukohde]] =
    elasticClient.execute {
      val hakuQuery = hakuOid.map(oid => matchQuery("hakuOid", oid.toString))
      val tarjoajaQuery = tarjoajaOid.map(oid => should(
        matchQuery("tarjoajat.oid", oid.toString),
        not(existsQuery("tarjoajat")).must(matchQuery("toteutus.tarjoajat.oid", oid.toString)),
      ))
      search(index).bool(must(hakuQuery ++ tarjoajaQuery))
    }.flatMap {
      case failure: RequestFailure =>
        Future.failed(ElasticSearchException(failure.error))
      case response: RequestSuccess[SearchResponse] =>
        Future.successful(response.result.to[HakukohdeIndexed].map(_.toHakukohde))
    }
}
