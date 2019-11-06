package fi.oph.kouta.internal.security

import fi.oph.kouta.internal.domain.oid.OrganisaatioOid

case class OrganizationAuthorizationFailedException(oids: Iterable[OrganisaatioOid]) extends RuntimeException

object OrganizationAuthorizationFailedException {
  def apply(oid: OrganisaatioOid): OrganizationAuthorizationFailedException =
    OrganizationAuthorizationFailedException(Seq(oid))

  def apply(): OrganizationAuthorizationFailedException = OrganizationAuthorizationFailedException(Seq.empty)
}

case class RoleAuthorizationFailedException(acceptedRoles: Seq[Role], existingRoles: Iterable[Role])
    extends RuntimeException(
      s"Authorization failed, missing role. Accepted roles: ${acceptedRoles.map(_.name).mkString(",")}. " +
        s"Existing roles: ${existingRoles.map(_.name).mkString(",")}."
    )

case class AuthenticationFailedException(msg: String, cause: Throwable) extends RuntimeException(msg, cause) {
  def this(msg: String) = this(msg, null)

  def this() = this(null, null)
}
