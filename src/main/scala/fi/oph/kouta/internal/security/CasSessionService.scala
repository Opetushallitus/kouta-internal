package fi.oph.kouta.internal.security

import fi.oph.kouta.internal.KoutaConfigurationFactory
import fi.oph.kouta.internal.client.KayttooikeusClient
import fi.oph.kouta.internal.database.SessionDAO
import fi.vm.sade.utils.cas.CasClient.Username
import fi.oph.kouta.logging.Logging

import java.util.UUID
import java.util.concurrent.TimeUnit

object CasSessionService
    extends CasSessionService(
      ProductionSecurityContext(KoutaConfigurationFactory.configuration.securityConfiguration),
      KayttooikeusClient,
      SessionDAO
    )

class CasSessionService(
    securityContext: SecurityContext,
    userDetailsService: KayttooikeusClient,
    sessionDAO: SessionDAO
) extends Logging {
  logger.info(s"Using security context ${securityContext.getClass.getSimpleName}")

  val serviceIdentifier: String = securityContext.casServiceIdentifier
  val casUrl: String            = securityContext.casUrl

  private val casClient = securityContext.casClient

  private def validateServiceTicket(ticket: ServiceTicket): Either[Throwable, Username] = {
    val ServiceTicket(s) = ticket
    try {
      Right(
        casClient
          .validateServiceTicketWithVirkailijaUsername(securityContext.casServiceIdentifier, s)
          .get(30, TimeUnit.SECONDS)
      )
    } catch {
      case t: Throwable =>
        Left(AuthenticationFailedException(s"Failed to validate service ticket $s", t))
    }
  }

  private def storeSession(ticket: ServiceTicket, user: KayttooikeusUserDetails): (UUID, CasSession) = {
    val session = CasSession(ticket, user.oid, user.authorities)
    logger.debug(s"Storing to session: ${session.casTicket} ${session.personOid} ${session.authorities}")
    val id = sessionDAO.store(session)
    (id, session)
  }

  private def createSession(ticket: ServiceTicket): Either[Throwable, (UUID, CasSession)] = {
    validateServiceTicket(ticket)
      .map(userDetailsService.getUserByUsername)
      .map(storeSession(ticket, _))
  }

  private def getSession(id: UUID): Either[Throwable, (UUID, Session)] =
    sessionDAO
      .get(id)
      .map(session => (id, session))
      .toRight(new AuthenticationFailedException(s"Session $id doesn't exist"))

  def getSession(ticket: Option[ServiceTicket], id: Option[UUID]): Either[Throwable, (UUID, Session)] = {
    logger.trace(s"Getting session with ticket $ticket and session id $id")
    (ticket, id) match {
      case (None, None) =>
        logger.trace("No session found")
        Left(new AuthenticationFailedException("No credentials given"))
      case (None, Some(i)) => getSession(i)
      case (Some(t), None) => createSession(t)
      case (Some(t), Some(i)) =>
        getSession(i).left.flatMap {
          case _: AuthenticationFailedException => createSession(t)
          case e                                => Left(e)
        }
    }
  }

  def deleteSession(ticket: ServiceTicket): Boolean = sessionDAO.delete(ticket)
}
