package fi.oph.kouta.internal

import fi.oph.kouta.internal.client.CallerId
import fi.oph.kouta.internal.security.{Authority, KayttooikeusUserDetails, SecurityContext}
import fi.vm.sade.javautils.nio.cas.CasClient
import fi.vm.sade.utils.cas.CasClient.SessionCookie
import org.asynchttpclient.{Request, Response}

import java.util.concurrent.CompletableFuture

class MockSecurityContext(
    val casUrl: String,
    val casServiceIdentifier: String,
    users: Map[String, KayttooikeusUserDetails]
) extends SecurityContext
    with CallerId {

  val casClient: CasClient = new CasClient {
    override def validateServiceTicketWithVirkailijaUsername(
        service: String,
        serviceTicket: String
    ): CompletableFuture[String] = {

      if (serviceTicket.startsWith(MockSecurityContext.ticketPrefix(service).toString)) {
        val username: String = serviceTicket.stripPrefix(MockSecurityContext.ticketPrefix(service).toString)
        CompletableFuture.completedFuture(username)
      } else {
        CompletableFuture.failedFuture(new RuntimeException("unrecognized ticket: " + serviceTicket))
      }
    }

    override def execute(request: Request): CompletableFuture[Response] = ???

    override def validateServiceTicketWithOppijaAttributes(
        s: SessionCookie,
        s1: SessionCookie
    ): CompletableFuture[java.util.HashMap[SessionCookie, SessionCookie]] = ???

    override def executeAndRetryWithCleanSessionOnStatusCodes(
        request: Request,
        set: java.util.Set[Integer]
    ): CompletableFuture[Response] = ???
  }
}

object MockSecurityContext {

  def apply(casUrl: String, casServiceIdentifier: String, defaultAuthorities: Set[Authority]): MockSecurityContext = {
    val users = Map("testuser" -> KayttooikeusUserDetails(defaultAuthorities, "mockoid"))

    new MockSecurityContext(casUrl, casServiceIdentifier, users)
  }

  def ticketFor(service: String, username: String): SessionCookie = ticketPrefix(service) + username

  private def ticketPrefix(service: String): SessionCookie = "mock-ticket-" + service + "-"
}
