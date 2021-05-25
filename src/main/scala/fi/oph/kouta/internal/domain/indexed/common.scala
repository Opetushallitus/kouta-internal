package fi.oph.kouta.internal.domain.indexed

import java.util.UUID

import fi.oph.kouta.internal.domain.{Kielistetty, Lisatieto, Valintakoe, Valintakoetilaisuus}
import fi.oph.kouta.internal.domain.oid.{OrganisaatioOid, UserOid}

case class Muokkaaja(oid: UserOid)

case class Organisaatio(oid: OrganisaatioOid)

case class KoodiUri(koodiUri: String)

case class UuidObject(id: UUID)

case class LisatietoIndexed(otsikko: KoodiUri, teksti: Kielistetty) {
  def toLisatieto = Lisatieto(otsikkoKoodiUri = otsikko.koodiUri, teksti)
}

case class ValintakoeIndexed(id: Option[UUID], tyyppi: Option[KoodiUri], tilaisuudet: List[Valintakoetilaisuus]) {
  def toValintakoe: Valintakoe = Valintakoe(
    id = id,
    tyyppi = tyyppi.map(_.koodiUri),
    tilaisuudet = tilaisuudet
  )
}
