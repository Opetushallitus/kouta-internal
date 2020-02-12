package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.http.ElasticClient
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.internal.domain.Koulutus
import fi.oph.kouta.internal.domain.indexed.KoulutusIndexed
import fi.oph.kouta.internal.domain.oid.KoulutusOid
import fi.oph.kouta.internal.util.KoutaJsonFormats
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class KoulutusClient(val index: String, val client: ElasticClient) extends KoutaJsonFormats with Logging with ElasticsearchClient {
  def getKoulutus(oid: KoulutusOid): Future[Koulutus] =
    getItem[KoulutusIndexed](oid.s).map(_.toKoulutus)
}

object KoulutusClient extends KoulutusClient("koulutus-kouta", ElasticsearchClient.client)