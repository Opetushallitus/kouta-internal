package fi.oph.kouta.external.domain

import fi.oph.kouta.external.domain.enums.Koulutustyyppi

sealed trait KoulutusMetadata {
  val tyyppi: Koulutustyyppi
  val kuvaus: Kielistetty
  val lisatiedot: Seq[Lisatieto]
}

case class AmmatillinenKoulutusMetadata(
    tyyppi: Koulutustyyppi = Koulutustyyppi.Amm,
    kuvaus: Kielistetty = Map(),
    lisatiedot: Seq[Lisatieto] = Seq()
) extends KoulutusMetadata

case class KorkeakoulutusKoulutusMetadata(
    tyyppi: Koulutustyyppi = Koulutustyyppi.Yo,
    kuvaus: Kielistetty = Map(),
    lisatiedot: Seq[Lisatieto] = Seq(),
    tutkintonimikeKoodiUrit: Seq[String] = Seq(),
    opintojenLaajuusKoodiUri: Option[String] = None,
    kuvauksenNimi: Kielistetty = Map()
) extends KoulutusMetadata
