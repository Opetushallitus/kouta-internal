package fi.oph.kouta.internal.domain.indexed

import java.time.LocalDateTime
import java.util.UUID

import fi.oph.kouta.internal.domain.enums.{Hakulomaketyyppi, Julkaisutila, Kieli}
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid}
import fi.oph.kouta.internal.domain._

case class EmbeddedHakukohdeIndexed(
    oid: HakukohdeOid
)

case class HakuIndexed(
    oid: HakuOid,
    tila: Julkaisutila,
    nimi: Kielistetty,
    hakutapa: Option[KoodiUri],
    hakukohteenLiittamisenTakaraja: Option[LocalDateTime],
    hakukohteenMuokkaamisenTakaraja: Option[LocalDateTime],
    ajastettuJulkaisu: Option[LocalDateTime],
    alkamiskausi: Option[KoodiUri],
    alkamisvuosi: Option[String],
    kohdejoukko: Option[KoodiUri],
    kohdejoukonTarkenne: Option[KoodiUri],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeAtaruId: Option[UUID],
    hakulomakeKuvaus: Kielistetty,
    hakulomakeLinkki: Kielistetty,
    metadata: Option[HakuMetadata],
    organisaatio: Organisaatio,
    hakuajat: List[Ajanjakso],
    valintakokeet: List[ValintakoeIndexed],
    muokkaaja: Muokkaaja,
    kielivalinta: Seq[Kieli],
    modified: Option[LocalDateTime]
) extends WithTila {
  def toHaku: Haku = Haku(
    oid = oid,
    tila = tila,
    nimi = nimi,
    hakutapaKoodiUri = hakutapa.map(_.koodiUri),
    hakukohteenLiittamisenTakaraja = hakukohteenLiittamisenTakaraja,
    hakukohteenMuokkaamisenTakaraja = hakukohteenMuokkaamisenTakaraja,
    ajastettuJulkaisu = ajastettuJulkaisu,
    alkamiskausiKoodiUri = alkamiskausi.map(_.koodiUri),
    alkamisvuosi = alkamisvuosi,
    kohdejoukkoKoodiUri = kohdejoukko.map(_.koodiUri),
    kohdejoukonTarkenneKoodiUri = kohdejoukonTarkenne.map(_.koodiUri),
    hakulomaketyyppi = hakulomaketyyppi,
    hakulomakeAtaruId = hakulomakeAtaruId,
    hakulomakeKuvaus = hakulomakeKuvaus,
    hakulomakeLinkki = hakulomakeLinkki,
    metadata = metadata,
    organisaatioOid = organisaatio.oid,
    hakuajat = hakuajat,
    valintakokeet = valintakokeet.map(_.toValintakoe),
    muokkaaja = muokkaaja.oid,
    kielivalinta = kielivalinta,
    modified = modified
  )
}
