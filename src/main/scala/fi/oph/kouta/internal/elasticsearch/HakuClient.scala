package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.http.ElasticClient
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.internal.domain.Haku
import fi.oph.kouta.internal.domain.indexed.HakuIndexed
import fi.oph.kouta.internal.domain.oid.HakuOid
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

  def searchByAtaruId(id: String): Future[Seq[Haku]] =
    searchItems[HakuIndexed](matchQuery("hakulomakeAtaruId", id)).map(_.map(_.toHaku))
}

object HakuClient extends HakuClient("haku-kouta", ElasticsearchClient.client)
