package fi.oph.kouta.external.domain.indexed

import java.time.LocalDateTime
import java.util.UUID

import fi.oph.kouta.external.domain.enums.{Julkaisutila, Kieli, Koulutustyyppi}
import fi.oph.kouta.external.domain._

case class ValintaperusteIndexed(
    id: Option[UUID],
    tila: Julkaisutila,
    koulutustyyppi: Koulutustyyppi,
    hakutapa: Option[KoodiUri],
    kohdejoukko: Option[KoodiUri],
    kohdejoukonTarkenne: Option[KoodiUri],
    nimi: Kielistetty,
    julkinen: Boolean,
    sorakuvaus: Option[SorakuvausIndexed],
    metadata: Option[ValintaperusteMetadataIndexed],
    organisaatio: Organisaatio,
    muokkaaja: Muokkaaja,
    kielivalinta: Seq[Kieli],
    modified: Option[LocalDateTime]
) {
  def toValintaperuste: Valintaperuste = Valintaperuste(
    id = id,
    koulutustyyppi = koulutustyyppi,
    tila = tila,
    hakutapaKoodiUri = hakutapa.map(_.koodiUri),
    kohdejoukkoKoodiUri = kohdejoukko.map(_.koodiUri),
    kohdejoukonTarkenneKoodiUri = kohdejoukonTarkenne.map(_.koodiUri),
    nimi = nimi,
    julkinen = julkinen,
    sorakuvausId = sorakuvaus.map(_.id),
    metadata = metadata.map(_.toValintaperusteMetadata),
    organisaatioOid = organisaatio.oid,
    muokkaaja = muokkaaja.oid,
    kielivalinta = kielivalinta,
    modified = modified
  )
}

case class SorakuvausIndexed(id: UUID)

sealed trait ValintaperusteMetadataIndexed {
  def koulutustyyppi: Koulutustyyppi
  def valintatavat: Seq[ValintatapaIndexed]
  def kielitaitovaatimukset: Seq[ValintaperusteKielitaitovaatimusIndexed]
  def toValintaperusteMetadata: ValintaperusteMetadata
}

case class AmmatillinenValintaperusteMetadataIndexed(
    koulutustyyppi: Koulutustyyppi = Koulutustyyppi.Amm,
    valintatavat: Seq[AmmatillinenValintatapaIndexed],
    kielitaitovaatimukset: Seq[ValintaperusteKielitaitovaatimusIndexed]
) extends ValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: ValintaperusteMetadata = AmmatillinenValintaperusteMetadata(
    koulutustyyppi = koulutustyyppi,
    valintatavat = valintatavat.map(_.toAmmatillinenValintatapa)                ,
    kielitaitovaatimukset = kielitaitovaatimukset.map(_.toValintaperusteKielitaitovaatimus)
  )
}

sealed trait KorkeakoulutusValintaperusteMetadataIndexed extends ValintaperusteMetadataIndexed {
  def valintatavat: Seq[KorkeakoulutusValintatapaIndexed]
  def kielitaitovaatimukset: Seq[ValintaperusteKielitaitovaatimusIndexed]
  def osaamistausta: Seq[KoodiUri]
  def kuvaus: Kielistetty
}

case class YliopistoValintaperusteMetadataIndexed(
    koulutustyyppi: Koulutustyyppi,
    valintatavat: Seq[YliopistoValintatapaIndexed],
    kielitaitovaatimukset: Seq[ValintaperusteKielitaitovaatimusIndexed],
    osaamistausta: Seq[KoodiUri],
    kuvaus: Kielistetty
) extends KorkeakoulutusValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: YliopistoValintaperusteMetadata = YliopistoValintaperusteMetadata(
    koulutustyyppi = koulutustyyppi,
    valintatavat = valintatavat.map(_.toYliopistoValintatapa),
    kielitaitovaatimukset = kielitaitovaatimukset.map(_.toValintaperusteKielitaitovaatimus),
    osaamistaustaKoodiUrit = osaamistausta.map(_.koodiUri),
    kuvaus = kuvaus
  )
}

case class AmmattikorkeakouluValintaperusteMetadataIndexed(
    koulutustyyppi: Koulutustyyppi,
    valintatavat: Seq[AmmattikorkeakouluValintatapaIndexed],
    kielitaitovaatimukset: Seq[ValintaperusteKielitaitovaatimusIndexed],
    osaamistausta: Seq[KoodiUri],
    kuvaus: Kielistetty
) extends KorkeakoulutusValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: AmmattikorkeakouluValintaperusteMetadata = AmmattikorkeakouluValintaperusteMetadata(
    koulutustyyppi = koulutustyyppi,
    valintatavat = valintatavat.map(_.toAmmattikorkeakouluValintatapa),
    kielitaitovaatimukset = kielitaitovaatimukset.map(_.toValintaperusteKielitaitovaatimus),
    osaamistaustaKoodiUrit = osaamistausta.map(_.koodiUri),
    kuvaus = kuvaus
  )
}

case class ValintaperusteKielitaitovaatimusIndexed(
    kieli: Option[KieliIndexed],
    kielitaidonVoiOsoittaa: Seq[KielitaidonVoiOsoittaaIndexed],
    vaatimukset: Seq[KielitaitovaatimusIndexed]
) {
  def toValintaperusteKielitaitovaatimus: ValintaperusteKielitaitovaatimus = ValintaperusteKielitaitovaatimus(
    kieliKoodiUri = kieli.map(_.koodiUri),
    kielitaidonVoiOsoittaa = kielitaidonVoiOsoittaa.map(_.toKielitaito),
    vaatimukset = vaatimukset.map(_.toKielitaitovaatimus)
  )
}

case class KieliIndexed(koodiUri: String)

case class KielitaidonVoiOsoittaaIndexed(kielitaito: Option[KoodiUri], lisatieto: Kielistetty) {
  def toKielitaito: Kielitaito = Kielitaito(kielitaito.map(_.koodiUri), lisatieto = lisatieto)
}

case class KielitaitovaatimusIndexed(
    kielitaitovaatimus: Option[KoodiUri],
    kielitaitovaatimusKuvaukset: Seq[KielitaitovaatimusKuvausIndexed]
) {
  def toKielitaitovaatimus: Kielitaitovaatimus = Kielitaitovaatimus(
    kielitaitovaatimusKoodiUri = kielitaitovaatimus.map(_.koodiUri),
    kielitaitovaatimusKuvaukset = kielitaitovaatimusKuvaukset.map(_.toKielitaitovaatimusKuvaus)
  )
}

case class KoodiUri(koodiUri: String)

case class KielitaitovaatimusKuvausIndexed(
    kielitaitovaatimusKuvaus: Option[KoodiUri],
    kielitaitovaatimusTaso: Option[String]
) {
  def toKielitaitovaatimusKuvaus: KielitaitovaatimusKuvaus = KielitaitovaatimusKuvaus(
    kielitaitovaatimusKuvausKoodiUri = kielitaitovaatimusKuvaus.map(_.koodiUri),
    kielitaitovaatimusTaso = kielitaitovaatimusTaso
  )
}

sealed trait ValintatapaIndexed {
  def valintatapa: Option[KoodiUri]
  def kuvaus: Kielistetty
  def sisalto: Seq[ValintatapaSisalto]
  def kaytaMuuntotaulukkoa: Boolean
  def kynnysehto: Kielistetty
  def enimmaispisteet: Option[Double]
  def vahimmaispisteet: Option[Double]
}

case class AmmatillinenValintatapaIndexed(
    valintatapa: Option[KoodiUri],
    kuvaus: Kielistetty,
    sisalto: Seq[ValintatapaSisalto],
    kaytaMuuntotaulukkoa: Boolean,
    kynnysehto: Kielistetty,
    enimmaispisteet: Option[Double],
    vahimmaispisteet: Option[Double]
) extends ValintatapaIndexed {
  def toAmmatillinenValintatapa = AmmatillinenValintatapa(
    valintatapaKoodiUri = valintatapa.map(_.koodiUri),
    kuvaus = kuvaus,
    sisalto = sisalto,
    kaytaMuuntotaulukkoa = kaytaMuuntotaulukkoa,
    kynnysehto = kynnysehto,
    enimmaispisteet = enimmaispisteet,
    vahimmaispisteet = vahimmaispisteet
  )
}

sealed trait KorkeakoulutusValintatapaIndexed extends ValintatapaIndexed {
  def nimi: Kielistetty
}

case class AmmattikorkeakouluValintatapaIndexed(
    nimi: Kielistetty,
    valintatapa: Option[KoodiUri],
    kuvaus: Kielistetty,
    sisalto: Seq[ValintatapaSisalto],
    kaytaMuuntotaulukkoa: Boolean,
    kynnysehto: Kielistetty,
    enimmaispisteet: Option[Double],
    vahimmaispisteet: Option[Double]
) extends KorkeakoulutusValintatapaIndexed {
  def toAmmattikorkeakouluValintatapa = AmmattikorkeakouluValintatapa(
    nimi = nimi,
    valintatapaKoodiUri = valintatapa.map(_.koodiUri),
    kuvaus = kuvaus,
    sisalto = sisalto,
    kaytaMuuntotaulukkoa = kaytaMuuntotaulukkoa,
    kynnysehto = kynnysehto,
    enimmaispisteet = enimmaispisteet,
    vahimmaispisteet = vahimmaispisteet
  )
}

case class YliopistoValintatapaIndexed(
    nimi: Kielistetty,
    valintatapa: Option[KoodiUri],
    kuvaus: Kielistetty,
    sisalto: Seq[ValintatapaSisalto],
    kaytaMuuntotaulukkoa: Boolean,
    kynnysehto: Kielistetty,
    enimmaispisteet: Option[Double],
    vahimmaispisteet: Option[Double]
) extends KorkeakoulutusValintatapaIndexed {
  def toYliopistoValintatapa = YliopistoValintatapa(
    nimi = nimi,
    valintatapaKoodiUri = valintatapa.map(_.koodiUri),
    kuvaus = kuvaus,
    sisalto = sisalto,
    kaytaMuuntotaulukkoa = kaytaMuuntotaulukkoa,
    kynnysehto = kynnysehto,
    enimmaispisteet = enimmaispisteet,
    vahimmaispisteet = vahimmaispisteet
  )
}
