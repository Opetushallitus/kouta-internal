package fi.oph.kouta.external.domain.indexed

import java.time.LocalDateTime

import fi.oph.kouta.external.domain._
import fi.oph.kouta.external.domain.enums._
import fi.oph.kouta.external.domain.oid._

case class KoulutusIndexed(
    oid: Option[KoulutusOid],
    johtaaTutkintoon: Boolean,
    koulutustyyppi: Option[Koulutustyyppi],
    koulutus: Option[KoodiUri],
    tila: Julkaisutila,
    tarjoajat: List[Organisaatio],
    nimi: Kielistetty,
    metadata: KoulutusMetadataIndexed,
    julkinen: Boolean,
    muokkaaja: Muokkaaja,
    organisaatio: Organisaatio,
    kielivalinta: Seq[Kieli],
    modified: Option[LocalDateTime]
) {
  def toKoulutus: Koulutus = Koulutus(
    oid = oid,
    johtaaTutkintoon = johtaaTutkintoon,
    koulutustyyppi = koulutustyyppi,
    koulutusKoodiUri = koulutus.map(_.koodiUri),
    tila = tila,
    tarjoajat = tarjoajat.map(_.oid),
    nimi = nimi,
    metadata = Some(metadata.toKoulutusMetadata),
    julkinen = julkinen,
    muokkaaja = muokkaaja.oid,
    organisaatioOid = organisaatio.oid,
    kielivalinta = kielivalinta,
    modified = modified
  )
}

sealed trait KoulutusMetadataIndexed {
  val tyyppi: Koulutustyyppi
  val kuvaus: Kielistetty
  val lisatiedot: Seq[LisatietoIndexed]

  def toKoulutusMetadata: KoulutusMetadata
}

case class LisatietoIndexed(otsikko: Otsikko, teksti: Kielistetty) {
  def toLisatieto = Lisatieto(otsikkoKoodiUri = otsikko.koodiUri, teksti)
}

case class Otsikko(koodiUri: String)

case class AmmatillinenKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed]
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: AmmatillinenKoulutusMetadata =
    AmmatillinenKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto)
    )
}

case class KorkeakoulutusKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    tutkintonimike: Seq[KoodiUri],
    opintojenLaajuus: Option[KoodiUri],
    kuvauksenNimi: Kielistetty
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: KorkeakoulutusKoulutusMetadata =
    KorkeakoulutusKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      tutkintonimikeKoodiUrit = tutkintonimike.map(_.koodiUri),
      opintojenLaajuusKoodiUri = opintojenLaajuus.map(_.koodiUri),
      kuvauksenNimi = kuvauksenNimi
    )
}
