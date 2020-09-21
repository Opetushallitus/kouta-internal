package fi.oph.kouta.internal.database

import java.util.UUID
import java.util.concurrent.TimeUnit

import fi.oph.kouta.internal.security.{Authority, CasSession, ServiceTicket, Session}
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class SessionDAO(db: KoutaDatabase) extends SQLHelpers {
  def store(session: Session): UUID = session match {
    case CasSession(ServiceTicket(ticket), personOid, authorities) =>
      val id = UUID.randomUUID()
      db.runBlockingTransactionally(
        storeCasSession(id, ticket, personOid, authorities),
        timeout = Duration(1, TimeUnit.MINUTES)
      ).map(_ => id)
        .get
  }

  def store(session: CasSession, id: UUID): UUID =
    db.runBlockingTransactionally(
      storeCasSession(id, session.casTicket.s, session.personOid, session.authorities),
      timeout = Duration(1, TimeUnit.MINUTES)
    ).map(_ => id)
      .get

  def delete(id: UUID): Boolean =
    db.runBlockingTransactionally(deleteSession(id), timeout = Duration(10, TimeUnit.SECONDS)).get

  def delete(ticket: ServiceTicket): Boolean =
    db.runBlockingTransactionally(deleteSession(ticket), timeout = Duration(10, TimeUnit.SECONDS)).get

  def get(id: UUID): Option[Session] = {
    db.runBlockingTransactionally(getSession(id), timeout = Duration(2, TimeUnit.SECONDS)).get.map {
      case (casTicket, personOid) =>
        val authorities = db.runBlocking(searchAuthoritiesBySession(id), Duration(2, TimeUnit.SECONDS))
        CasSession(ServiceTicket(casTicket.get), personOid, authorities.map(Authority(_)).toSet)
    }
  }

  private def storeCasSession(id: UUID, ticket: String, personOid: String, authorities: Set[Authority]) = {
    DBIO.seq(
      sqlu"""insert into sessions (id, cas_ticket, person) values ($id, $ticket, $personOid)""",
      DBIO.sequence(
        authorities.map(a => sqlu"""insert into authorities (session, authority) values ($id, ${a.authority})""").toSeq
      )
    )
  }

  private def deleteSession(id: UUID) =
    sqlu"""delete from sessions where id = $id""".map(_ > 0)

  private def deleteSession(ticket: ServiceTicket) =
    sqlu"""delete from sessions where cas_ticket = ${ticket.s}""".map(_ > 0)

  private def getSession(id: UUID) =
    getSessionQuery(id).flatMap {
      case None =>
        deleteSession(id).andThen(DBIO.successful(None))
      case Some(t) =>
        updateLastRead(id).andThen(DBIO.successful(Some(t)))
    }

  private def getSessionQuery(id: UUID) =
    sql"""select cas_ticket, person from sessions
          where id = $id and last_read > now() - interval '60 minutes'"""
      .as[(Option[String], String)]
      .headOption

  private def updateLastRead(id: UUID) =
    sqlu"""update sessions set last_read = now()
           where id = $id and last_read < now() - interval '30 minutes'"""

  private def searchAuthoritiesBySession(sessionId: UUID) =
    sql"""select authority from authorities where session = $sessionId""".as[String]
}

object SessionDAO extends SessionDAO(KoutaDatabase)
