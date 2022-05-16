package fi.oph.kouta.internal.integration.fixture

import fi.oph.kouta.internal.domain.oid.{HakukohdeOid, HakukohderyhmaOid}
import fi.oph.kouta.internal.MockSecurityContext
import fi.oph.kouta.internal.client.{HakukohderyhmaClient, KayttooikeusClient}
import fi.oph.kouta.internal.security._
import fi.oph.kouta.internal.servlet.AuthServlet

import scala.concurrent.Future

class KayttooikeusClientMock(securityContext: SecurityContext, defaultAuthorities: Set[Authority])
    extends KayttooikeusClient {
  override def getUserByUsername(username: String): KayttooikeusUserDetails = {
    username match {
      case "testuser" => KayttooikeusUserDetails(defaultAuthorities, "test-user-oid")
      case _          => throw new AuthenticationFailedException(s"User not found with username: $username")
    }
  }
}

class HakukohderyhmaClientMock() extends HakukohderyhmaClient {
  override def getHakukohteet(oid: HakukohderyhmaOid): Future[Seq[HakukohdeOid]] = {
    oid match {
      case HakukohderyhmaOid("testOid") => Future.successful(Seq())
      case _                            => throw new RuntimeException(s"Hakukohderyhmas not found for oid: $oid")
    }
  }
}

trait AuthFixture {
  this: KoutaIntegrationSpec =>

  val authPath    = "/auth"
  val loginPath   = s"$authPath/login"
  val sessionPath = s"$authPath/session"

  val casUrl = "testCasUrl"

  val securityContext: SecurityContext           = MockSecurityContext(casUrl, serviceIdentifier, defaultAuthorities)
  val kayttooikeusClient: KayttooikeusClient     = new KayttooikeusClientMock(securityContext, defaultAuthorities)
  val hakukohderyhmaClient: HakukohderyhmaClient = new HakukohderyhmaClientMock()
  val sessionService                             = new CasSessionService(securityContext, kayttooikeusClient, sessionDAO)

  addServlet(new AuthServlet(sessionService), authPath)

  def getSessionFromCookies(cookies: String) = {
    "session=[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}".r
      .findFirstIn(cookies)
      .map(s => s.replace("session=", ""))
  }
}
