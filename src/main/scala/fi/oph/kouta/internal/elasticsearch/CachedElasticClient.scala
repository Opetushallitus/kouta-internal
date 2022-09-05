package fi.oph.kouta.internal.elasticsearch

import com.github.blemale.scaffeine.Scaffeine

import scala.concurrent.duration._
import com.sksamuel.elastic4s.requests.get.GetRequest
import com.sksamuel.elastic4s.requests.searches.{SearchRequest, SearchScrollRequest}
import com.sksamuel.elastic4s.{CommonRequestOptions, ElasticClient, Executor, Functor, Handler, Response}
import fi.oph.kouta.internal.KoutaConfigurationFactory
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future
import boopickle.Default._

object G {
  def main(args: Array[String]): Unit = {

  }
}

case class CachedElasticClient(client: ElasticClient) extends Logging {
  private lazy val cache = Scaffeine()
    .softValues()
    .expireAfterWrite(KoutaConfigurationFactory.configuration.elasticSearchConfiguration.cacheTimeoutSeconds.seconds)
    .build[Any, Future[Response[Any]]]()

  def execute[T, U](t: T)(implicit
      executor: Executor[Future],
      functor: Functor[Future],
      handler: Handler[T, U],
      manifest: Manifest[U],
      options: CommonRequestOptions
  ): Future[Response[U]] = {
    val key = t
    def executeForReal(): Future[Response[U]] = {
      val f = client.execute(t)
      f.failed.foreach(t => {
        cache.invalidate(key)
        logger.error(s"ElasticSearch query failed: ${t.getMessage}", t)
      })(global)
      f
    }


    t match {
      case get: GetRequest =>
        cache.get(key, _ => executeForReal()).mapTo[Response[U]]
      case search: SearchRequest =>
        cache.get(key, _ => executeForReal()).mapTo[Response[U]]
      case search: SearchScrollRequest =>
        cache.get(key, _ => executeForReal()).mapTo[Response[U]]
      case _ =>
        executeForReal()
    }
  }
}
