package fi.oph.kouta.external.security

import java.time.LocalDateTime
import java.util.UUID

import scala.collection.mutable

trait SessionDAO {
  def delete(ticket: ServiceTicket): Boolean
  def delete(id: UUID): Boolean
  def store(session: Session): UUID
  def store(session: CasSession, id: UUID): UUID
  def get(id: UUID): Option[Session]
}

object SessionDAO extends SessionDAO {

  // Super temporary in-memory map
  val sessionMap: mutable.Map[UUID, CasSession] = mutable.Map()

  override def store(session: Session): UUID = session match {
    case cs: CasSession =>
      val id = UUID.randomUUID()
      sessionMap.put(id, cs)
      id
  }

  override def store(session: CasSession, id: UUID): UUID = {
    sessionMap.put(id, session)
    id
  }

  override def delete(id: UUID): Boolean =
    sessionMap.remove(id).nonEmpty

  override def delete(ticket: ServiceTicket): Boolean =
    sessionMap.find { case (_, session) => session.casTicket == ticket } match {
      case Some((id, session)) =>
        sessionMap.remove(id)
        true
      case None =>
        false
    }

  override def get(id: UUID): Option[Session] = {
    sessionMap.get(id).flatMap { session =>
      if (LocalDateTime.now().minusMinutes(60).isBefore(session.lastRead)) {
        if (LocalDateTime.now().minusMinutes(30).isAfter(session.lastRead)) {
          val newSession = session.copy(lastRead = LocalDateTime.now())
          sessionMap.put(id, newSession)
        }
        Some(session)
      } else {
        sessionMap.remove(id)
        None
      }
    }
  }
}
