package fi.oph.kouta.external.security

case class KayttooikeusUserDetails(authorities: Set[Authority], oid: String)
