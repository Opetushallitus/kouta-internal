package fi.oph.kouta.internal.servlet

import java.util.UUID
import fi.oph.kouta.internal.database.SessionDAO
import fi.oph.kouta.internal.security.{Authenticated, AuthenticationFailedException}
import fi.oph.kouta.logging.Logging
import fi.oph.kouta.util.Timer.timed
import org.scalatra.ScalatraServlet

trait CasAuthenticatedServlet {
  this: ScalatraServlet with Logging =>

  val sessionDAO: SessionDAO

  protected def authenticate: Authenticated = timed("Auth check", 20) {
    val sessionCookie    = cookies.get("session")
    val sessionAttribute = Option(request.getAttribute("session")).map(_.toString)

    logger.trace("Session cookie {}", sessionCookie)
    logger.trace("Session attribute {}", sessionAttribute)

    val session = sessionCookie
      .orElse(sessionAttribute)
      .map(UUID.fromString)
      .flatMap(id => sessionDAO.get(id).map((id, _)))

    logger.trace("Session found {}", session)

    Authenticated.tupled(session.getOrElse(throw new AuthenticationFailedException))
  }
}
