package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.http.ElasticClient
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.internal.domain.Haku
import fi.oph.kouta.internal.domain.enums.Julkaisutila.Tallennettu
import fi.oph.kouta.internal.domain.indexed.HakuIndexed
import fi.oph.kouta.internal.domain.oid.{HakuOid, OrganisaatioOid}
import fi.oph.kouta.internal.util.KoutaJsonFormats
import fi.vm.sade.utils.slf4j.Logging

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
        hakukohde.tila match { case Tallennettu => return false }
        hakukohde.jarjestyspaikka.fold(hakukohde.toteutus.tarjoajat.exists(t => oids.contains(t.oid)))(j =>
          oids.contains(j.oid)
        )
      })
    )

  def search(ataruId: Option[String], tarjoajaOids: Option[Set[OrganisaatioOid]]): Future[Seq[Haku]] = {
    val ataruIdQuery = ataruId.map(termsQuery("hakulomakeAtaruId.keyword", _))
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
    val query = ataruIdQuery ++ tarjoajaQuery
    searchItems[HakuIndexed](if (query.isEmpty) None else Some(must(query)))
      .map(_.filter(byTarjoajaAndTila(tarjoajaOids, _)).map(_.toHaku))
  }
}

object HakuClient extends HakuClient("haku-kouta", ElasticsearchClient.client)
