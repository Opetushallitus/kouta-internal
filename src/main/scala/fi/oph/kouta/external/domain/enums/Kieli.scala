package fi.oph.kouta.external.domain.enums

sealed abstract class Kieli(val name: String) extends BasicType

object Kieli extends BasicTypeCompanion[Kieli] {
  case object Fi extends Kieli("fi")
  case object Sv extends Kieli("sv")
  case object En extends Kieli("en")

  def all: List[Kieli] = List(Fi, Sv, En)
}
