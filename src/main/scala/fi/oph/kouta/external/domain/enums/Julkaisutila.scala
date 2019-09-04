package fi.oph.kouta.external.domain.enums

import fi.oph.kouta.external.swagger.SwaggerModel

@SwaggerModel(
  """    Julkaisutila:
    |      type: string
    |      enum:
    |        - julkaistu
    |        - tallennettu
    |        - arkistoitu
    |"""
)
sealed abstract class Julkaisutila(val name: String) extends BasicType

object Julkaisutila extends BasicTypeCompanion[Julkaisutila] {

  case object Tallennettu extends Julkaisutila("tallennettu")
  case object Julkaistu   extends Julkaisutila("julkaistu")
  case object Arkistoitu  extends Julkaisutila("arkistoitu")

  val all: List[Julkaisutila] = List(Tallennettu, Julkaistu, Arkistoitu)
}

