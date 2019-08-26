package fi.oph.kouta.external.domain

import fi.oph.kouta.external.domain.enums.Koulutustyyppi

sealed trait ToteutusMetadata {
  val tyyppi: Koulutustyyppi
  val kuvaus: Kielistetty
  val opetus: Option[Opetus]
  val asiasanat: List[Keyword]
  val ammattinimikkeet: List[Keyword]
  val yhteyshenkilo: Option[Yhteyshenkilo]
}

sealed trait KorkeakoulutusToteutusMetadata extends ToteutusMetadata {
  val alemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisala]
  val ylemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisala]
}

case class AmmatillinenToteutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    osaamisalat: List[AmmatillinenOsaamisala],
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilo: Option[Yhteyshenkilo]
) extends ToteutusMetadata

case class YliopistoToteutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilo: Option[Yhteyshenkilo],
    alemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisala],
    ylemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisala]
) extends KorkeakoulutusToteutusMetadata

case class AmmattikorkeakouluToteutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilo: Option[Yhteyshenkilo],
    alemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisala],
    ylemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisala]
) extends KorkeakoulutusToteutusMetadata

trait Osaamisala {
  val linkki: Kielistetty
  val otsikko: Kielistetty
}

case class AmmatillinenOsaamisala(koodi: String, linkki: Kielistetty, otsikko: Kielistetty) extends Osaamisala

case class KorkeakouluOsaamisala(nimi: Kielistetty, kuvaus: Kielistetty, linkki: Kielistetty, otsikko: Kielistetty)
    extends Osaamisala

case class Opetus(
    opetuskieliKoodiUrit: Seq[String],
    opetuskieletKuvaus: Kielistetty,
    opetusaikaKoodiUrit: Seq[String],
    opetusaikaKuvaus: Kielistetty,
    opetustapaKoodiUrit: Seq[String],
    opetustapaKuvaus: Kielistetty,
    onkoMaksullinen: Option[Boolean],
    maksullisuusKuvaus: Kielistetty,
    maksunMaara: Option[Double],
    alkamiskausiKoodiUri: Option[String],
    alkamisvuosi: Option[String],
    alkamisaikaKuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    onkoLukuvuosimaksua: Option[Boolean],
    lukuvuosimaksu: Kielistetty,
    lukuvuosimaksuKuvaus: Kielistetty,
    onkoStipendia: Option[Boolean],
    stipendinMaara: Kielistetty,
    stipendinKuvaus: Kielistetty
)
