package fi.oph.kouta.internal.domain.indexed

import java.time.LocalDateTime
import java.util.UUID
import fi.oph.kouta.domain.{
  AikuistenPerusopetus,
  Amk,
  AmmOpeErityisopeJaOpo,
  Amm,
  AmmMuu,
  KkOpintojakso,
  Koulutustyyppi,
  Lk,
  Telma,
  Tuva,
  Yo
}
import fi.oph.kouta.internal.domain.enums.{Julkaisutila, Kieli}
import fi.oph.kouta.internal.domain._
import fi.vm.sade.utils.slf4j.Logging

case class ValintaperusteIndexed(
    id: Option[UUID],
    tila: Julkaisutila,
    koulutustyyppi: Koulutustyyppi,
    hakutapa: Option[KoodiUri],
    kohdejoukko: Option[KoodiUri],
    kohdejoukonTarkenne: Option[KoodiUri],
    nimi: Kielistetty,
    julkinen: Boolean,
    metadata: Option[ValintaperusteMetadataIndexed],
    organisaatio: Option[Organisaatio],
    muokkaaja: Muokkaaja,
    kielivalinta: Seq[Kieli],
    modified: Option[LocalDateTime],
    externalId: Option[String],
    valintakokeet: List[ValintakoeIndexed] // Kaytetaan ainoastaan hakukohteissa
) extends WithTila
    with Logging {
  def toValintaperuste: Valintaperuste = {
    try {
      Valintaperuste(
        id = id,
        koulutustyyppi = koulutustyyppi,
        tila = tila,
        hakutapaKoodiUri = hakutapa.map(_.koodiUri),
        kohdejoukkoKoodiUri = kohdejoukko.map(_.koodiUri),
        kohdejoukonTarkenneKoodiUri = kohdejoukonTarkenne.map(_.koodiUri),
        nimi = nimi,
        julkinen = julkinen,
        metadata = metadata.map(_.toValintaperusteMetadata),
        organisaatioOid = organisaatio.get.oid,
        muokkaaja = muokkaaja.oid,
        kielivalinta = kielivalinta,
        modified = modified,
        externalId = externalId
      )
    } catch {
      case e: Exception =>
        val msg: String = s"Failed to create Valintaperuste ($id)"
        logger.error(msg, e)
        throw new RuntimeException(msg, e)
    }
  }
}

case class SorakuvausIndexed(id: UUID)

sealed trait ValintaperusteMetadataIndexed {
  def tyyppi: Koulutustyyppi
  def valintatavat: Seq[ValintatapaIndexed]
  def kuvaus: Kielistetty
  def hakukelpoisuus: Kielistetty
  def lisatiedot: Kielistetty
  def valintakokeidenYleiskuvaus: Kielistetty
  def sisalto: Seq[Sisalto]

  def toValintaperusteMetadata: ValintaperusteMetadata
}

case class GenericValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi,
    valintatavat: Seq[ValintatapaIndexed],
    kuvaus: Kielistetty = Map(),
    hakukelpoisuus: Kielistetty = Map(),
    lisatiedot: Kielistetty = Map(),
    sisalto: Seq[Sisalto],
    valintakokeidenYleiskuvaus: Kielistetty = Map()
) extends ValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata =
    GenericValintaperusteMetadata(
      tyyppi = tyyppi,
      valintatavat = valintatavat.map(_.toValintatapa),
      kuvaus = kuvaus,
      hakukelpoisuus = hakukelpoisuus,
      lisatiedot = lisatiedot,
      sisalto = sisalto,
      valintakokeidenYleiskuvaus = valintakokeidenYleiskuvaus
    )
}

case class ValintatapaIndexed(
    nimi: Kielistetty,
    valintatapa: Option[KoodiUri],
    kuvaus: Kielistetty,
    sisalto: Seq[Sisalto],
    kaytaMuuntotaulukkoa: Boolean,
    kynnysehto: Kielistetty,
    enimmaispisteet: Option[Double],
    vahimmaispisteet: Option[Double]
) {
  def toValintatapa: Valintatapa = Valintatapa(
    nimi = nimi,
    valintatapaKoodiUri = valintatapa.map(_.koodiUri),
    sisalto = sisalto,
    kaytaMuuntotaulukkoa = kaytaMuuntotaulukkoa,
    kynnysehto = kynnysehto,
    enimmaispisteet = enimmaispisteet,
    vahimmaispisteet = vahimmaispisteet
  )
}
