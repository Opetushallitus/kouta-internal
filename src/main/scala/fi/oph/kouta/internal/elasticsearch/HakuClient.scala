package fi.oph.kouta.internal.elasticsearch


import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.internal.domain.Haku
import fi.oph.kouta.internal.domain.indexed.HakuIndexed
import fi.oph.kouta.internal.domain.oid.HakuOid
import fi.oph.kouta.internal.util.KoutaJsonFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HakuClient(override val index: String, elasticsearchClientHolder: ElasticsearchClientHolder)
  extends ElasticsearchClient(index, "haku", elasticsearchClientHolder)
    with KoutaJsonFormats {

  def getHaku(oid: HakuOid): Future[Haku] =
    getItem(oid.s)
      .map(_.to[HakuIndexed])
      .map(_.toHaku)

  def searchByAtaruId(id: String): Future[Seq[Haku]] =
    simpleSearch("hakulomakeAtaruId", id)
      .map(_.to[HakuIndexed])
      .map(_.map(_.toHaku))
}
