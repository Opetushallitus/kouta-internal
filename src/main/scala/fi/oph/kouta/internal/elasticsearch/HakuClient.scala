package fi.oph.kouta.internal.elasticsearch


import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.internal.domain.Haku
import fi.oph.kouta.internal.domain.indexed.HakuIndexed
import fi.oph.kouta.internal.domain.oid.HakuOid
import fi.oph.kouta.internal.util.KoutaJsonFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object HakuClient extends HakuClient("haku-kouta")

abstract class HakuClient(override val index: String)
  extends ElasticsearchClient(index, "haku")
    with KoutaJsonFormats {

  def getHaku(oid: HakuOid): Future[Haku] =
    getItem(oid.s)
      .map(_.to[HakuIndexed])
      .map(_.toHaku)
}
