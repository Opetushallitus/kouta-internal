package fi.oph.kouta.external.domain

import java.time.LocalDateTime

import fi.oph.kouta.external.domain.enums._
import fi.oph.kouta.external.domain.oid.{KoulutusOid, OrganisaatioOid, UserOid}

case class Koulutus(
    oid: Option[KoulutusOid] = None,
    johtaaTutkintoon: Boolean,
    koulutustyyppi: Option[Koulutustyyppi] = None,
    koulutusKoodiUri: Option[String] = None,
    tila: Julkaisutila = Julkaisutila.Tallennettu,
    tarjoajat: List[OrganisaatioOid] = List(),
    nimi: Kielistetty = Map(),
    metadata: Option[KoulutusMetadata] = None,
    julkinen: Boolean = false,
    muokkaaja: UserOid,
    organisaatioOid: OrganisaatioOid,
    kielivalinta: Seq[Kieli] = Seq(),
    modified: Option[LocalDateTime]
) extends PerustiedotWithOid
