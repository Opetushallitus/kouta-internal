package fi.oph.kouta.external.domain

import java.time.LocalDateTime
import java.util.UUID
import fi.oph.kouta.external.domain.oid._
import fi.oph.kouta.external.domain.enums._

sealed trait Perustiedot{
  val tila: Julkaisutila
  val nimi: Kielistetty
  val muokkaaja: UserOid
  val kielivalinta: Seq[Kieli]
  val organisaatioOid: OrganisaatioOid
  val modified: Option[LocalDateTime]
}

abstract class PerustiedotWithOid extends Perustiedot {
  val oid: Option[Oid]
}

abstract class PerustiedotWithId extends Perustiedot {
  val id: Option[UUID]
}
