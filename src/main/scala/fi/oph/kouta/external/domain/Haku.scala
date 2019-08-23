package fi.oph.kouta.external.domain

import java.time.LocalDateTime
import java.util.UUID

import fi.oph.kouta.external.domain.enums.{Hakulomaketyyppi, Julkaisutila, Kieli}
import fi.oph.kouta.external.domain.oid.{HakuOid, OrganisaatioOid, UserOid}

case class Haku(
    oid: Option[HakuOid],
    tila: Julkaisutila,
    nimi: Kielistetty,
    hakutapaKoodiUri: Option[String],
    hakukohteenLiittamisenTakaraja: Option[LocalDateTime],
    hakukohteenMuokkaamisenTakaraja: Option[LocalDateTime],
    ajastettuJulkaisu: Option[LocalDateTime],
    alkamiskausiKoodiUri: Option[String],
    alkamisvuosi: Option[String],
    kohdejoukkoKoodiUri: Option[String],
    kohdejoukonTarkenneKoodiUri: Option[String],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeAtaruId: Option[UUID],
    hakulomakeKuvaus: Kielistetty,
    hakulomakeLinkki: Kielistetty,
    metadata: Option[HakuMetadata],
    organisaatioOid: OrganisaatioOid,
    hakuajat: List[Ajanjakso],
    valintakokeet: List[Valintakoe],
    muokkaaja: UserOid,
    kielivalinta: Seq[Kieli],
    modified: Option[LocalDateTime]
) extends PerustiedotWithOid

case class HakuMetadata(yhteyshenkilo: Option[Yhteyshenkilo], tulevaisuudenAikataulu: Seq[Ajanjakso])
