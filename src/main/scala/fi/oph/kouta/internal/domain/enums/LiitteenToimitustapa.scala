package fi.oph.kouta.internal.domain.enums

import fi.oph.kouta.internal.swagger.SwaggerModel

@SwaggerModel("""    LiitteenToimitustapa:
    |      type: string
    |      enum:
    |        - hakijapalvelu
    |        - osoite
    |        - lomake
    |""")
sealed abstract class LiitteenToimitustapa(val name: String) extends BasicType

object LiitteenToimitustapa extends BasicTypeCompanion[LiitteenToimitustapa] {
  case object Lomake        extends LiitteenToimitustapa("lomake")
  case object Hakijapalvelu extends LiitteenToimitustapa("hakijapalvelu")
  case object MuuOsoite     extends LiitteenToimitustapa("osoite")

  def all = List(Lomake, Hakijapalvelu, MuuOsoite)
}
