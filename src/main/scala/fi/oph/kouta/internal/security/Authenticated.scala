package fi.oph.kouta.internal.security

import java.util.UUID

case class Authenticated(id: UUID, session: Session)
