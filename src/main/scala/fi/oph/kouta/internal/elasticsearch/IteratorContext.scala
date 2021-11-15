package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.{ElasticClient, HitReader, RequestFailure, RequestSuccess}
import com.sksamuel.elastic4s.requests.searches.{SearchHit, SearchRequest}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait ClearableIterator[T] extends Iterator[T] {
  def clear(): Unit
}

object IteratorContext {
  def iterator(client: CachedElasticClient, searchreq: SearchRequest)(implicit
      timeout: Duration
  ): ClearableIterator[SearchHit] =
    new ClearableIterator[SearchHit] {

      require(searchreq.keepAlive.isDefined, "Search request must define keep alive value")

      import com.sksamuel.elastic4s.ElasticDsl._

      private var internalIterator: Iterator[SearchHit] = Iterator.empty
      private var scrollId: Option[String]              = None
      private var allScrollIds: Set[String]             = Set()

      override def hasNext: Boolean = internalIterator.hasNext || {
        internalIterator = fetchNext()
        internalIterator.hasNext
      }

      override def next(): SearchHit = internalIterator.next()

      override def clear(): Unit = {
        if (!allScrollIds.isEmpty) {
          //TODO virheenkäsittelyä
          client.execute(clearScroll(allScrollIds)).await
        }
      }

      def fetchNext(): Iterator[SearchHit] = {

        // we're either advancing a scroll id or issuing the first query w/ the keep alive set
        val f = scrollId match {
          case Some(id) =>
            allScrollIds += id
            client.execute(searchScroll(id, searchreq.keepAlive.get))
          case None => client.execute(searchreq)
        }

        val resp = Await.result(f, timeout)

        // in a search scroll we must always use the last returned scrollId
        val response = resp match {
          case RequestSuccess(_, _, _, result) => result
          case failure: RequestFailure         => sys.error(failure.toString)
        }

        scrollId = response.scrollId
        response.hits.hits.iterator
      }
    }
}
