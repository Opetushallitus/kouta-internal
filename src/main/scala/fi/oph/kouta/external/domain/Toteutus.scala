package fi.oph.kouta.external.domain

import java.time.LocalDateTime

import fi.oph.kouta.external.domain.enums.{Julkaisutila, Kieli}
import fi.oph.kouta.external.domain.oid.{KoulutusOid, OrganisaatioOid, ToteutusOid, UserOid}

case class Toteutus(
    oid: Option[ToteutusOid],
    koulutusOid: KoulutusOid,
    tila: Julkaisutila,
    tarjoajat: List[OrganisaatioOid],
    nimi: Kielistetty,
    metadata: Option[ToteutusMetadata],
    muokkaaja: UserOid,
    organisaatioOid: OrganisaatioOid,
    kielivalinta: Seq[Kieli],
    modified: Option[LocalDateTime]
) extends PerustiedotWithOid
