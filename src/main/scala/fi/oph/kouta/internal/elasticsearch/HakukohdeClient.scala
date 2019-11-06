package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.internal.domain.Hakukohde
import fi.oph.kouta.internal.domain.indexed.HakukohdeIndexed
import fi.oph.kouta.internal.domain.oid.HakukohdeOid
import fi.oph.kouta.internal.util.KoutaJsonFormats

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
