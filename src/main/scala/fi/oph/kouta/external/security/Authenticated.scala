package fi.oph.kouta.external.security

import java.util.UUID

case class Authenticated(id: UUID, session: Session)
