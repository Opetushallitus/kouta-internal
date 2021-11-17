package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.ElasticDsl.{clearScroll}
import com.sksamuel.elastic4s.{ElasticClient, RequestFailure, RequestSuccess, Response}
import com.sksamuel.elastic4s.requests.searches.{SearchHit, SearchRequest, SearchResponse}
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import com.sksamuel.elastic4s.ElasticDsl._

class IterativeElasticFetch(client: ElasticClient)(implicit val executor: ExecutionContext) extends Logging {

  def fetch(searchreq: SearchRequest): Future[IndexedSeq[SearchHit]] = fetchUntilDone(searchreq, IndexedSeq.empty)

  private def fetchUntilDone(searchreq: SearchRequest, scrollId: Seq[String]): Future[IndexedSeq[SearchHit]] = {
    def handleResult(resp: Response[SearchResponse]): Future[IndexedSeq[SearchHit]] = {
      resp match {
        case RequestSuccess(_, _, _, result) =>
          val r: SearchHit = result.hits.hits.iterator.next
          if (result.hits.hits.iterator.hasNext) {
            fetchUntilDone(searchreq, scrollId ++ result.scrollId).map(_ :+ r)
          } else {
            client.execute(clearScroll(scrollId)).onComplete {
              case Success(_) =>
                logger.debug("Successfully cleared Elastic scroll indices")
              case Failure(exception) =>
                logger.error(s"Failed to clear Elastic scroll indices", exception)
            }
            Future.successful(IndexedSeq(r))
          }

        case failure: RequestFailure =>
          Future.failed(new RuntimeException(s"Iterative Elastic search failed: ${failure.toString}"))
      }
    }
    scrollId.lastOption match {
      case Some(id) =>
        client
          .execute(searchScroll(id, searchreq.keepAlive.get))
          .flatMap(handleResult)
      case None =>
        client
          .execute(searchreq)
          .flatMap(handleResult)
    }
  }
}
