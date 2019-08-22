package fi.oph.kouta.external.elasticsearch

import java.util.UUID

import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.external.domain.Valintaperuste
import fi.oph.kouta.external.domain.indexed.ValintaperusteIndexed
import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ValintaperusteClient extends ValintaperusteClient("valintaperuste-kouta")

abstract class ValintaperusteClient(override val index: String)
  extends ElasticsearchClient(index, "valintaperuste")
    with KoutaJsonFormats
    with Logging {

  def getValintaperuste(id: UUID): Future[Valintaperuste] =
    getItem(id.toString)
      .map(_.to[ValintaperusteIndexed])
      .map(_.toValintaperuste)
}
