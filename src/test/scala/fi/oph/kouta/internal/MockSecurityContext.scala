package fi.oph.kouta.internal

import fi.oph.kouta.internal.client.CallerId
import fi.oph.kouta.internal.security.{AuthenticationFailedException, Authority, SecurityContext}
import fi.vm.sade.javautils.nio.cas.{CasClient, UserDetails}
import org.asynchttpclient.{Request, Response}

import java.util.concurrent.CompletableFuture
import scala.collection.JavaConverters._

class MockSecurityContext(
    val casUrl: String,
    val casServiceIdentifier: String,
    defaultAuthorities: Set[Authority]
) extends SecurityContext
    with CallerId {

  val casClient: CasClient = new CasClient {
    override def validateServiceTicketWithVirkailijaUserDetails(
        service: String,
        serviceTicket: String
    ): CompletableFuture[UserDetails] = {

      if (serviceTicket.startsWith(MockSecurityContext.ticketPrefix(service))) {
        val username: String = serviceTicket.stripPrefix(MockSecurityContext.ticketPrefix(service))
        if (username == "testuser") {
          val henkiloOid  = "1.2.246.562.24.10000000000"
          val roles       = defaultAuthorities.map(a => s"ROLE_${a.role}").asJava
          val userDetails = new UserDetails(username, henkiloOid, null, null, roles)
          CompletableFuture.completedFuture(userDetails)
        } else {
          CompletableFuture.failedFuture(new AuthenticationFailedException(s"User not found with username: $username"))
        }
      } else {
        CompletableFuture.failedFuture(new RuntimeException("unrecognized ticket: " + serviceTicket))
      }
    }

    override def execute(request: Request): CompletableFuture[Response] = ???

    override def validateServiceTicketWithOppijaAttributes(
        s: String,
        s1: String
    ): CompletableFuture[java.util.HashMap[String, String]] = ???

    override def executeAndRetryWithCleanSessionOnStatusCodes(
        request: Request,
        set: java.util.Set[Integer]
    ): CompletableFuture[Response] = ???
  }
}

object MockSecurityContext {

  def apply(casUrl: String, casServiceIdentifier: String, defaultAuthorities: Set[Authority]): MockSecurityContext = {
    new MockSecurityContext(casUrl, casServiceIdentifier, defaultAuthorities)
  }

  def ticketFor(service: String, username: String): String = ticketPrefix(service) + username

  private def ticketPrefix(service: String): String = "mock-ticket-" + service + "-"
}
