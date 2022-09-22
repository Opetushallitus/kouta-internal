package fi.oph.kouta.internal.elasticsearch

import com.github.blemale.scaffeine.Scaffeine
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.internal.KoutaConfigurationFactory
import fi.oph.kouta.internal.domain.Valintaperuste
import fi.oph.kouta.internal.domain.indexed.ValintaperusteIndexed
import fi.oph.kouta.internal.util.KoutaJsonFormats
import fi.vm.sade.utils.slf4j.Logging

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationLong

class ValintaperusteClient(val index: String, val client: ElasticClient)
  extends KoutaJsonFormats
    with Logging
    with ElasticsearchClient {

  private val valintaperusteCache = Scaffeine()
    .expireAfterWrite(KoutaConfigurationFactory.configuration.elasticSearchConfiguration.cacheTimeoutSeconds.seconds)
    .buildAsync[UUID, Valintaperuste]()

  def getValintaperuste(id: UUID): Future[Valintaperuste] =
    valintaperusteCache.getFuture(id, id =>
      getItem[ValintaperusteIndexed](id.toString).map(_.toValintaperuste))
}

object ValintaperusteClient extends ValintaperusteClient("valintaperuste-kouta", ElasticsearchClient.client)
