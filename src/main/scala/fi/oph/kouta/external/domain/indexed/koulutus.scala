package fi.oph.kouta.external.domain.indexed

import java.time.LocalDateTime

import fi.oph.kouta.external.domain._
import fi.oph.kouta.external.domain.enums._
import fi.oph.kouta.external.domain.oid._

case class KoulutusIndexed(
    oid: Option[KoulutusOid],
    johtaaTutkintoon: Boolean,
    koulutustyyppi: Option[Koulutustyyppi],
    koulutus: Option[Koulutustiedot],
    tila: Julkaisutila,
    tarjoajat: List[Tarjoaja],
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

case class Koulutustiedot(koodiUri: String, nimi: Kielistetty)

case class Tarjoaja(oid: OrganisaatioOid)

case class Muokkaaja(oid: UserOid)

case class Organisaatio(oid: OrganisaatioOid)

sealed trait KoulutusMetadataIndexed {
  val tyyppi: Koulutustyyppi
  val kuvaus: Map[Kieli, String]
  val lisatiedot: Seq[LisatietoIndexed]

  def toKoulutusMetadata: KoulutusMetadata
}

case class LisatietoIndexed(otsikko: Otsikko, teksti: Kielistetty) {
  def toLisatieto = Lisatieto(otsikkoKoodiUri = otsikko.koodiUri, teksti)
}

case class Otsikko(koodiUri: String)

case class AmmatillinenKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Map[Kieli, String],
    lisatiedot: Seq[LisatietoIndexed]
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: KoulutusMetadata =
    AmmatillinenKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto)
    )
}

case class KorkeakoulutusKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Map[Kieli, String],
    lisatiedot: Seq[LisatietoIndexed],
    tutkintonimike: Seq[Tutkintonimike],
    opintojenLaajuus: Option[OpintojenLaajuus],
    kuvauksenNimi: Map[Kieli, String]
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: KoulutusMetadata =
    KorkeakoulutusKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      tutkintonimikeKoodiUrit = tutkintonimike.map(_.koodiUri),
      opintojenLaajuusKoodiUri = opintojenLaajuus.map(_.koodiUri),
      kuvauksenNimi = kuvauksenNimi
    )
}

case class Tutkintonimike(koodiUri: String, nimi: Kielistetty)

case class OpintojenLaajuus(koodiUri: String, nimi: Kielistetty)
