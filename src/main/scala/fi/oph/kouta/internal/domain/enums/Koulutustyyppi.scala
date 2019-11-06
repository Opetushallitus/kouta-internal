package fi.oph.kouta.internal.domain.enums

sealed abstract class Koulutustyyppi(val name: String) extends BasicType

object Koulutustyyppi extends BasicTypeCompanion[Koulutustyyppi] {

  case object Amm extends Koulutustyyppi("amm")
  case object Lk  extends Koulutustyyppi("lk")
  case object Muu extends Koulutustyyppi("muu")
  case object Yo  extends Koulutustyyppi("yo")
  case object Amk extends Koulutustyyppi("amk")

  val all: List[Koulutustyyppi] = List(Amm, Lk, Muu, Yo, Amk)
}
