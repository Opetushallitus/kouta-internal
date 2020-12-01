package fi.oph.kouta.internal.domain.indexed

import java.time.LocalDateTime
import java.util.UUID

import fi.oph.kouta.internal.domain.enums.{Hakulomaketyyppi, Julkaisutila, Kieli}
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid}
import fi.oph.kouta.internal.domain._
import fi.vm.sade.utils.slf4j.Logging

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
    alkamiskausi: Option[KoodiUri],
    alkamisvuosi: Option[String],
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
    modified: Option[LocalDateTime]
) extends WithTila
    with Logging {
  def toHaku: Haku = {
    try {
      Haku(
        oid = oid,
        tila = tila,
        nimi = nimi,
        hakutapaKoodiUri = hakutapa.map(_.koodiUri),
        hakukohteenLiittamisenTakaraja = hakukohteenLiittamisenTakaraja,
        hakukohteenMuokkaamisenTakaraja = hakukohteenMuokkaamisenTakaraja,
        ajastettuJulkaisu = ajastettuJulkaisu,
        alkamiskausiKoodiUri = alkamiskausi.map(_.koodiUri),
        alkamisvuosi = alkamisvuosi,
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
        modified = modified
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
