package fi.oph.kouta.external.domain.enums

sealed abstract class Hakulomaketyyppi (val name: String) extends BasicType

object Hakulomaketyyppi extends BasicTypeCompanion[Hakulomaketyyppi] {
  case object Ataru extends Hakulomaketyyppi ("ataru")
  case object HakuApp extends Hakulomaketyyppi ("haku-app")
  case object MuuHakulomake extends Hakulomaketyyppi ("muu")
  case object EiSähköistä extends Hakulomaketyyppi ("ei sähköistä")

  val all: List[Hakulomaketyyppi] = List(Ataru, HakuApp, MuuHakulomake, EiSähköistä)
}
