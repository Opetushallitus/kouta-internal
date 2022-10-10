package fi.oph.kouta.internal.util

import com.github.blemale.scaffeine.Scaffeine
import fi.oph.kouta.internal.KoutaConfigurationFactory

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationLong

case class ElasticCache[KEY, RESULT]() {

  private val cache = Scaffeine()
    .expireAfterWrite(KoutaConfigurationFactory.configuration.elasticSearchConfiguration.cacheTimeoutSeconds.seconds)
    .buildAsync[KEY, RESULT]()

  def get(key: KEY, fetchForReal: KEY => Future[RESULT]): Future[RESULT] =
    cache.getFuture(key, fetchForReal)

  def getMany(keys: Set[KEY], fetchForReal: Iterable[KEY] => Future[Map[KEY, RESULT]]): Future[Seq[RESULT]] =
    cache.getAllFuture(keys, fetchForReal).map(_.values.toSeq)(global)

}
