package fi.oph.kouta.external

import fi.oph.kouta.external.domain.enums.Kieli

package object domain {
  type Kielistetty = Map[Kieli, String]

  case class Lisatieto(otsikkoKoodiUri: String, teksti: Kielistetty)
}
