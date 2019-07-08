package fi.oph.kouta.external.service

import java.util.NoSuchElementException

import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.get.GetResponse
import com.sksamuel.elastic4s.http.{RequestFailure, RequestSuccess}
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import fi.oph.kouta.external.domain.Koulutus
import fi.oph.kouta.external.domain.indexed.KoulutusIndexed
import fi.oph.kouta.external.domain.oid.KoulutusOid
import fi.oph.kouta.external.elasticsearch.{ElasticSearchException, ElasticsearchClient}
import fi.oph.kouta.external.util.KoutaJsonFormats
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object KoulutusService extends ElasticsearchClient with KoutaJsonFormats with Logging {

  def getKoulutus(oid: KoulutusOid): Future[Koulutus] = {
    elasticClient.execute {
      get(oid.s).from("koulutus-kouta")
    }.flatMap {
      case failure: RequestFailure =>
        Future.failed(ElasticSearchException(failure.error))

      case response: RequestSuccess[GetResponse] if !response.result.exists =>
        Future.failed(new NoSuchElementException(s"No such element for koulutus $oid"))

      case response: RequestSuccess[GetResponse] =>
        Future.fromTry {
          response.result
            .safeTo[KoulutusIndexed]
            .map(_.toKoulutus)
        }
    }
  }
}
