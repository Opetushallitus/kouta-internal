package fi.oph.kouta.internal.security

case class KayttooikeusUserDetails(authorities: Set[Authority], oid: String)
