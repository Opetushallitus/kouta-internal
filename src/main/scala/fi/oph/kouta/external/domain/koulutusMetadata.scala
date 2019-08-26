package fi.oph.kouta.external.domain

import fi.oph.kouta.external.domain.enums.Koulutustyyppi

sealed trait KoulutusMetadata {
  val tyyppi: Koulutustyyppi
  val kuvaus: Kielistetty
  val lisatiedot: Seq[Lisatieto]
  val koulutusalaKoodiUrit: Seq[String]
}

case class AmmatillinenKoulutusMetadata(
    tyyppi: Koulutustyyppi = Koulutustyyppi.Amm,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    koulutusalaKoodiUrit: Seq[String]
) extends KoulutusMetadata

case class KorkeakoulutusKoulutusMetadata(
    tyyppi: Koulutustyyppi = Koulutustyyppi.Yo,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    koulutusalaKoodiUrit: Seq[String],
    tutkintonimikeKoodiUrit: Seq[String],
    opintojenLaajuusKoodiUri: Option[String],
    kuvauksenNimi: Kielistetty,
) extends KoulutusMetadata
