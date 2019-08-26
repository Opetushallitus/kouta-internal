package fi.oph.kouta.external.domain.indexed

import fi.oph.kouta.external.domain.{Kielistetty, Lisatieto}
import fi.oph.kouta.external.domain.oid.{OrganisaatioOid, UserOid}

case class Muokkaaja(oid: UserOid)

case class Organisaatio(oid: OrganisaatioOid)

case class KoodiUri(koodiUri: String)

case class LisatietoIndexed(otsikko: KoodiUri, teksti: Kielistetty) {
  def toLisatieto = Lisatieto(otsikkoKoodiUri = otsikko.koodiUri, teksti)
}
