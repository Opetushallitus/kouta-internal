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
  def toValintaperusteMetadata: ValintaperusteMetadata
}

case class AmmatillinenValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi = Amm,
    valintatavat: Seq[AmmatillinenValintatapaIndexed]
) extends ValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: ValintaperusteMetadata = AmmatillinenValintaperusteMetadata(
    tyyppi = tyyppi,
    valintatavat = valintatavat.map(_.toAmmatillinenValintatapa)
  )
}

case class TuvaValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi = Tuva,
    valintatavat: Seq[AmmatillinenValintatapaIndexed]
) extends ValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: ValintaperusteMetadata = TuvaValintaperusteMetadata(
    tyyppi = tyyppi,
    valintatavat = valintatavat.map(_.toAmmatillinenValintatapa)
  )
}

case class TelmaValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi = Telma,
    valintatavat: Seq[AmmatillinenValintatapaIndexed]
) extends ValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: ValintaperusteMetadata = TelmaValintaperusteMetadata(
    tyyppi = tyyppi,
    valintatavat = valintatavat.map(_.toAmmatillinenValintatapa)
  )
}

case class AmmatillinenMuuValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi = AmmMuu,
    valintatavat: Seq[AmmatillinenValintatapaIndexed]
) extends ValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: AmmatillinenMuuValintaperusteMetadata = AmmatillinenMuuValintaperusteMetadata(
    tyyppi = tyyppi,
    valintatavat = valintatavat.map(_.toAmmatillinenValintatapa)
  )
}

case class VapaaSivistystyoValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi,
    valintatavat: Seq[AmmatillinenValintatapaIndexed]
) extends ValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: ValintaperusteMetadata = VapaaSivistystyoValintaperusteMetadata(
    tyyppi = tyyppi,
    valintatavat = valintatavat.map(_.toAmmatillinenValintatapa)
  )
}

sealed trait KorkeakoulutusValintaperusteMetadataIndexed extends ValintaperusteMetadataIndexed {
  def valintatavat: Seq[KorkeakoulutusValintatapaIndexed]
  def kuvaus: Kielistetty
}

case class YliopistoValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi = Yo,
    valintatavat: Seq[YliopistoValintatapaIndexed],
    kuvaus: Kielistetty
) extends KorkeakoulutusValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: YliopistoValintaperusteMetadata = YliopistoValintaperusteMetadata(
    tyyppi = tyyppi,
    valintatavat = valintatavat.map(_.toYliopistoValintatapa),
    kuvaus = kuvaus
  )
}

case class AmmattikorkeakouluValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi = Amk,
    valintatavat: Seq[AmmattikorkeakouluValintatapaIndexed],
    kuvaus: Kielistetty
) extends KorkeakoulutusValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: AmmattikorkeakouluValintaperusteMetadata =
    AmmattikorkeakouluValintaperusteMetadata(
      tyyppi = tyyppi,
      valintatavat = valintatavat.map(_.toAmmattikorkeakouluValintatapa),
      kuvaus = kuvaus
    )
}

case class AmmOpeErityisopeJaOpoValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi = AmmOpeErityisopeJaOpo,
    valintatavat: Seq[AmmOpeErityisopeJaOpoValintatapaIndexed],
    kuvaus: Kielistetty
) extends KorkeakoulutusValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: AmmOpeErityisopeJaOpoValintaperusteMetadata =
    AmmOpeErityisopeJaOpoValintaperusteMetadata(
      tyyppi = tyyppi,
      valintatavat = valintatavat.map(_.toAmmattikorkeakouluValintatapa),
      kuvaus = kuvaus
    )
}

case class KkOpintojaksoValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi = KkOpintojakso,
    valintatavat: Seq[KkOpintojaksoValintatapaIndexed],
    kuvaus: Kielistetty
) extends KorkeakoulutusValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: KkOpintojaksoValintaperusteMetadata =
    KkOpintojaksoValintaperusteMetadata(
      tyyppi = tyyppi,
      valintatavat = valintatavat.map(_.toKkOpintojaksoValintatapa),
      kuvaus = kuvaus
    )
}

case class LukioValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi = Lk,
    valintatavat: Seq[LukioValintatapaIndexed],
    kuvaus: Kielistetty
) extends ValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: LukioValintaperusteMetadata =
    LukioValintaperusteMetadata(
      tyyppi = tyyppi,
      valintatavat = valintatavat.map(_.toLukioValintatapa),
      kuvaus = kuvaus
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
  def toAmmatillinenValintatapa: AmmatillinenValintatapa = AmmatillinenValintatapa(
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
  def toAmmattikorkeakouluValintatapa: AmmattikorkeakouluValintatapa = AmmattikorkeakouluValintatapa(
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

case class AmmOpeErityisopeJaOpoValintatapaIndexed(
    nimi: Kielistetty,
    valintatapa: Option[KoodiUri],
    kuvaus: Kielistetty,
    sisalto: Seq[ValintatapaSisalto],
    kaytaMuuntotaulukkoa: Boolean,
    kynnysehto: Kielistetty,
    enimmaispisteet: Option[Double],
    vahimmaispisteet: Option[Double]
) extends KorkeakoulutusValintatapaIndexed {
  def toAmmattikorkeakouluValintatapa: AmmOpeErityisopeJaOpoValintatapa = AmmOpeErityisopeJaOpoValintatapa(
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

case class KkOpintojaksoValintatapaIndexed(
    nimi: Kielistetty,
    valintatapa: Option[KoodiUri],
    kuvaus: Kielistetty,
    sisalto: Seq[ValintatapaSisalto],
    kaytaMuuntotaulukkoa: Boolean,
    kynnysehto: Kielistetty,
    enimmaispisteet: Option[Double],
    vahimmaispisteet: Option[Double]
) extends KorkeakoulutusValintatapaIndexed {
  def toKkOpintojaksoValintatapa: KkOpintojaksoValintatapa = KkOpintojaksoValintatapa(
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
  def toYliopistoValintatapa: YliopistoValintatapa = YliopistoValintatapa(
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

case class LukioValintatapaIndexed(
    nimi: Kielistetty,
    valintatapa: Option[KoodiUri],
    kuvaus: Kielistetty,
    sisalto: Seq[ValintatapaSisalto],
    kaytaMuuntotaulukkoa: Boolean,
    kynnysehto: Kielistetty,
    enimmaispisteet: Option[Double],
    vahimmaispisteet: Option[Double]
) extends KorkeakoulutusValintatapaIndexed {
  def toLukioValintatapa: LukioValintatapa = LukioValintatapa(
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

case class AikuistenPerusopetusValintaperusteMetadataIndexed(
    tyyppi: Koulutustyyppi = AikuistenPerusopetus,
    valintatavat: Seq[AmmatillinenValintatapaIndexed]
) extends ValintaperusteMetadataIndexed {
  override def toValintaperusteMetadata: AikuistenPerusopetusValintaperusteMetadata =
    AikuistenPerusopetusValintaperusteMetadata(
      tyyppi = tyyppi,
      valintatavat = valintatavat.map(_.toAmmatillinenValintatapa)
    )
}
