package fi.oph.kouta.external.elasticsearch

import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.external.domain.Hakukohde
import fi.oph.kouta.external.domain.indexed.HakukohdeIndexed
import fi.oph.kouta.external.domain.oid.HakukohdeOid
import fi.oph.kouta.external.util.KoutaJsonFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object HakukohdeClient extends HakukohdeClient("hakukohde-kouta")

abstract class HakukohdeClient(override val index: String)
  extends ElasticsearchClient(index, "hakukohde")
    with KoutaJsonFormats {

  def getHakukohde(oid: HakukohdeOid): Future[Hakukohde] =
    getItem(oid.s)
      .map(_.to[HakukohdeIndexed])
      .map(_.toHakukohde)
}
