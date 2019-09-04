package fi.oph.kouta.external.domain.enums

import fi.oph.kouta.external.swagger.SwaggerModel

@SwaggerModel(
  """    Kieli:
    |      type: string
    |      enum:
    |        - fi
    |        - sv
    |        - en
    |""")
sealed abstract class Kieli(val name: String) extends BasicType

object Kieli extends BasicTypeCompanion[Kieli] {
  case object Fi extends Kieli("fi")
  case object Sv extends Kieli("sv")
  case object En extends Kieli("en")

  def all: List[Kieli] = List(Fi, Sv, En)
}
