package fi.oph.kouta.internal.client

import fi.oph.kouta.internal.domain.oid.{HakukohdeOid, HakukohderyhmaOid}
import fi.oph.kouta.internal.security._
import fi.oph.kouta.logging.Logging
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HakukohderyhmaClient extends KoutaClient with Logging {
  override protected val loginParams: String       = "/auth/cas"
  override protected val sessionCookieName: String = "ring-session"
  override protected val serviceName: String       = urlProperties.url("hakukohderyhmapalvelu.service")

  private implicit val formats = DefaultFormats

  def getHakukohteet(oid: HakukohderyhmaOid): Future[Seq[HakukohdeOid]] = {
    fetch("GET", urlProperties.url("hakukohderyhmapalvelu.hakukohteet", oid)).flatMap {
      case (200, body) => Future.successful(parse(body).values.asInstanceOf[Seq[String]].map(s => HakukohdeOid(s)))
      case (status, body) =>
        val errorString = s"Hakukohteet fetch failed for hakukohderyhmäoid: $oid with status $status, body: $body"
        logger.error(errorString)

        Future.failed(
          new RuntimeException(errorString)
        )
    }
  }
}
