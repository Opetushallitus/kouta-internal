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
    metadata: Option[KoulutusMetadataIndexed],
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
    metadata = metadata.map(_.toKoulutusMetadata),
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
  val koulutusala: Seq[KoodiUri]

  def toKoulutusMetadata: KoulutusMetadata
}

case class AmmatillinenKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    koulutusala: Seq[KoodiUri]
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: AmmatillinenKoulutusMetadata =
    AmmatillinenKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      koulutusalaKoodiUrit = koulutusala.map(_.koodiUri)
    )
}

trait KorkeakoulutusKoulutusMetadataIndexed extends KoulutusMetadataIndexed {
  val kuvauksenNimi: Kielistetty
  val tutkintonimike: Seq[KoodiUri]
  val opintojenLaajuus: Option[KoodiUri]
}

case class YliopistoKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    koulutusala: Seq[KoodiUri],
    tutkintonimike: Seq[KoodiUri],
    opintojenLaajuus: Option[KoodiUri],
    kuvauksenNimi: Kielistetty
) extends KorkeakoulutusKoulutusMetadataIndexed {
  override def toKoulutusMetadata: YliopistoKoulutusMetadata = YliopistoKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
    tutkintonimikeKoodiUrit = tutkintonimike.map(_.koodiUri),
    opintojenLaajuusKoodiUri = opintojenLaajuus.map(_.koodiUri),
    kuvauksenNimi = kuvauksenNimi
  )
}

case class AmmattikorkeakouluKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    koulutusala: Seq[KoodiUri],
    tutkintonimike: Seq[KoodiUri],
    opintojenLaajuus: Option[KoodiUri],
    kuvauksenNimi: Kielistetty
) extends KorkeakoulutusKoulutusMetadataIndexed {
  override def toKoulutusMetadata: AmmattikorkeakouluKoulutusMetadata = AmmattikorkeakouluKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
    tutkintonimikeKoodiUrit = tutkintonimike.map(_.koodiUri),
    opintojenLaajuusKoodiUri = opintojenLaajuus.map(_.koodiUri),
    kuvauksenNimi = kuvauksenNimi
  )
}
