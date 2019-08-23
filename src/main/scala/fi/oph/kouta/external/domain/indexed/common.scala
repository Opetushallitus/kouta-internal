package fi.oph.kouta.external.domain.indexed

import fi.oph.kouta.external.domain.oid.{OrganisaatioOid, UserOid}

case class Muokkaaja(oid: UserOid)

case class Organisaatio(oid: OrganisaatioOid)

case class KoodiUri(koodiUri: String)
