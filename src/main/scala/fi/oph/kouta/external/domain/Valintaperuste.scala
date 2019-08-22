package fi.oph.kouta.external.domain

import java.time.LocalDateTime
import java.util.UUID

import fi.oph.kouta.external.domain.enums.{Julkaisutila, Kieli, Koulutustyyppi}
import fi.oph.kouta.external.domain.oid.{OrganisaatioOid, UserOid}

case class Valintaperuste(
    id: Option[UUID],
    tila: Julkaisutila,
    koulutustyyppi: Koulutustyyppi,
    hakutapaKoodiUri: Option[String],
    kohdejoukkoKoodiUri: Option[String],
    kohdejoukonTarkenneKoodiUri: Option[String],
    nimi: Kielistetty,
    julkinen: Boolean,
    sorakuvausId: Option[UUID],
    metadata: Option[ValintaperusteMetadata],
    organisaatioOid: OrganisaatioOid,
    muokkaaja: UserOid,
    kielivalinta: Seq[Kieli],
    modified: Option[LocalDateTime]
) extends PerustiedotWithId
