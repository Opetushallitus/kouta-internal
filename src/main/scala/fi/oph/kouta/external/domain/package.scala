package fi.oph.kouta.external

import java.time.LocalDateTime
import java.util.UUID

import fi.oph.kouta.external.domain.Kielistetty
import fi.oph.kouta.external.domain.enums.Kieli

package object domain {
  type Kielistetty = Map[Kieli, String]
}

case class Lisatieto(otsikkoKoodiUri: String, teksti: Kielistetty)

case class Yhteyshenkilo(
    nimi: Kielistetty,
    titteli: Kielistetty,
    sahkoposti: Kielistetty,
    puhelinnumero: Kielistetty,
    wwwSivu: Kielistetty
)

case class Ajanjakso(alkaa: LocalDateTime, paattyy: LocalDateTime)

case class Valintakoe(id: Option[UUID], tyyppi: Option[String], tilaisuudet: List[Valintakoetilaisuus])

case class Valintakoetilaisuus(osoite: Option[Osoite], aika: Option[Ajanjakso], lisatietoja: Kielistetty)

case class Osoite(osoite: Kielistetty, postinumero: Option[String], postitoimipaikka: Kielistetty)
