package fi.oph.kouta.internal.domain.indexed

import java.time.{LocalDateTime, ZoneOffset}
import java.util.UUID
import fi.oph.kouta.internal.domain.enums.{Hakulomaketyyppi, Julkaisutila, Kieli}
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid}
import fi.oph.kouta.internal.domain._
import fi.vm.sade.utils.slf4j.Logging
import org.joda.time.LocalDate

import java.time.temporal.TemporalField

case class EmbeddedToteutusIndexed(tarjoajat: List[Organisaatio])

case class EmbeddedHakukohdeIndexed(
    oid: HakukohdeOid,
    jarjestyspaikka: Option[Organisaatio],
    toteutus: EmbeddedToteutusIndexed,
    tila: Julkaisutila
)

case class HakuIndexed(
    oid: HakuOid,
    tila: Julkaisutila,
    nimi: Kielistetty,
    hakukohteet: List[EmbeddedHakukohdeIndexed],
    hakutapa: Option[KoodiUri],
    hakukohteenLiittamisenTakaraja: Option[LocalDateTime],
    hakukohteenMuokkaamisenTakaraja: Option[LocalDateTime],
    ajastettuJulkaisu: Option[LocalDateTime],
    kohdejoukko: KoodiUri,
    kohdejoukonTarkenne: Option[KoodiUri],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeAtaruId: Option[UUID],
    hakulomakeKuvaus: Kielistetty,
    hakulomakeLinkki: Kielistetty,
    metadata: Option[HakuMetadata],
    organisaatio: Option[Organisaatio],
    hakuajat: List[Ajanjakso],
    valintakokeet: List[ValintakoeIndexed],
    muokkaaja: Muokkaaja,
    kielivalinta: Seq[Kieli],
    modified: Option[LocalDateTime],
    externalId: Option[String]
) extends WithTila
    with Logging {
  def toHaku(includeHakukohdeOids: Boolean = false): Haku = {
    def getHakukausiUri(ajanjakso: Ajanjakso): String = {
      ajanjakso.paattyy.map(_.getMonthValue).getOrElse {
        ajanjakso.alkaa.getMonthValue
      }
    } match {
      case m if m >= 1 && m <= 7  => "kausi_k#1"
      case m if m >= 8 && m <= 12 => "kausi_s#1"
      case _                      => ""
    }

    try {
      Haku(
        oid = oid,
        hakukohdeOids = if(includeHakukohdeOids) Some(hakukohteet.map(_.oid)) else None,
        tila = tila,
        nimi = nimi,
        hakutapaKoodiUri = hakutapa.map(_.koodiUri),
        hakukohteenLiittamisenTakaraja = hakukohteenLiittamisenTakaraja,
        hakukohteenMuokkaamisenTakaraja = hakukohteenMuokkaamisenTakaraja,
        ajastettuJulkaisu = ajastettuJulkaisu,
        alkamiskausiKoodiUri = metadata.flatMap(m =>
          m.koulutuksenAlkamiskausi
            .flatMap(_.koulutuksenAlkamiskausi.map(_.koodiUri))
        ),
        hakuvuosi = hakuajat
          .sortBy(ha => ha.alkaa)
          .headOption.map(ha => ha.paattyy.map(_.getYear).getOrElse(ha.alkaa.getYear)),
        hakukausi = hakuajat
          .sortBy(ha => ha.alkaa)
          .headOption.map(getHakukausiUri),
        alkamisvuosi = metadata.flatMap(m => m.koulutuksenAlkamiskausi.flatMap(_.koulutuksenAlkamisvuosi)),
        kohdejoukkoKoodiUri = kohdejoukko.koodiUri,
        kohdejoukonTarkenneKoodiUri = kohdejoukonTarkenne.map(_.koodiUri),
        hakulomaketyyppi = hakulomaketyyppi,
        hakulomakeAtaruId = hakulomakeAtaruId,
        hakulomakeKuvaus = hakulomakeKuvaus,
        hakulomakeLinkki = hakulomakeLinkki,
        metadata = metadata,
        organisaatioOid = organisaatio.get.oid,
        hakuajat = hakuajat,
        valintakokeet = valintakokeet.map(_.toValintakoe),
        muokkaaja = muokkaaja.oid,
        kielivalinta = kielivalinta,
        modified = modified,
        externalId = externalId
      )
    } catch {
      case e: Exception => {
        val msg: String = s"Failed to create Haku (${oid})"
        logger.error(msg, e)
        throw new RuntimeException(msg, e)
      }
    }
  }
  implicit val localDateTimeOrdering: Ordering[LocalDateTime] = Ordering.by(_.toEpochSecond(ZoneOffset.UTC))
  def toOdwHaku: OdwHaku = {
    def hakukausi: Option[String] = {
      hakuajat
        .sortBy(ha => ha.alkaa)
        .headOption
        .map(ha =>
          ha.paattyy
            .map(_.getMonthValue)
            .getOrElse(ha.alkaa.getMonthValue) match {
            case m if m >= 1 && m <= 7  => "kausi_k#1"
            case m if m >= 8 && m <= 12 => "kausi_s#1"
            case _                      => ""
          }
        )
    }
    def hakuvuosi: Option[String] = {
      hakuajat
        .sortBy(ha => ha.alkaa)
        .headOption
        .map(ha =>
          ha.paattyy
            .map(_.getYear.toString)
            .getOrElse(ha.alkaa.getYear.toString)
        )

    }
    try {
      OdwHaku(
        oid = oid,
        tila = tila,
        nimi = nimi,
        hakutapaKoodiUri = hakutapa.map(_.koodiUri),
        hakukohteenLiittamisenTakaraja = hakukohteenLiittamisenTakaraja,
        hakukohteenMuokkaamisenTakaraja = hakukohteenMuokkaamisenTakaraja,
        ajastettuJulkaisu = ajastettuJulkaisu,
        alkamiskausiKoodiUri = metadata.flatMap(m =>
          m.koulutuksenAlkamiskausi
            .flatMap(_.koulutuksenAlkamiskausi.map(_.koodiUri))
        ),
        alkamisvuosi = metadata.flatMap(m => m.koulutuksenAlkamiskausi.flatMap(_.koulutuksenAlkamisvuosi)),
        kohdejoukkoKoodiUri = kohdejoukko.koodiUri,
        kohdejoukonTarkenneKoodiUri = kohdejoukonTarkenne.map(_.koodiUri),
        hakulomaketyyppi = hakulomaketyyppi,
        hakulomakeAtaruId = hakulomakeAtaruId,
        hakulomakeKuvaus = hakulomakeKuvaus,
        hakulomakeLinkki = hakulomakeLinkki,
        metadata = metadata,
        organisaatioOid = organisaatio.get.oid,
        hakuajat = hakuajat,
        valintakokeet = valintakokeet.map(_.toValintakoe),
        muokkaaja = muokkaaja.oid,
        kielivalinta = kielivalinta,
        modified = modified,
        externalId = externalId,
        hakuvuosi = hakuvuosi,
        hakukausi = hakukausi
      )
    } catch {
      case e: Exception => {
        val msg: String = s"Failed to create Haku (${oid})"
        logger.error(msg, e)
        throw new RuntimeException(msg, e)
      }
    }
  }
}
