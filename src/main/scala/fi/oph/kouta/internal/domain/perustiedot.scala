package fi.oph.kouta.internal.domain

import java.time.LocalDateTime
import java.util.UUID
import fi.oph.kouta.internal.domain.oid._
import fi.oph.kouta.internal.domain.enums._

trait WithTila {
  val tila: Julkaisutila
}

sealed trait Perustiedot extends WithTila {
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
