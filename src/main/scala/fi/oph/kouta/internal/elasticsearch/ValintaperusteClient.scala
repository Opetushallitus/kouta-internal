package fi.oph.kouta.internal.elasticsearch

import java.util.UUID

import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.internal.domain.Valintaperuste
import fi.oph.kouta.internal.domain.indexed.ValintaperusteIndexed
import fi.oph.kouta.internal.util.KoutaJsonFormats
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ValintaperusteClient(val index: String, val client: ElasticClient)
    extends KoutaJsonFormats
    with Logging
    with ElasticsearchClient {
  def getValintaperuste(id: UUID): Future[Valintaperuste] =
    getItem[ValintaperusteIndexed](id.toString).map(_.toValintaperuste)
}

object ValintaperusteClient extends ValintaperusteClient("valintaperuste-kouta", ElasticsearchClient.client)
