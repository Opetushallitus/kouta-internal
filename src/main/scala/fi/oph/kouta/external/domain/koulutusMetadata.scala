package fi.oph.kouta.external.domain

import fi.oph.kouta.external.domain.enums.{Kieli, Koulutustyyppi}

sealed trait KoulutusMetadata {
  val tyyppi: Koulutustyyppi
  val kuvaus: Map[Kieli, String]
  val lisatiedot: Seq[Lisatieto]
}

case class AmmatillinenKoulutusMetadata(
    tyyppi: Koulutustyyppi = Koulutustyyppi.Amm,
    kuvaus: Map[Kieli, String] = Map(),
    lisatiedot: Seq[Lisatieto] = Seq()
) extends KoulutusMetadata

case class KorkeakoulutusKoulutusMetadata(
    tyyppi: Koulutustyyppi = Koulutustyyppi.Yo,
    kuvaus: Map[Kieli, String] = Map(),
    lisatiedot: Seq[Lisatieto] = Seq(),
    tutkintonimikeKoodiUrit: Seq[String] = Seq(),
    opintojenLaajuusKoodiUri: Option[String] = None,
    kuvauksenNimi: Map[Kieli, String] = Map()
) extends KoulutusMetadata
