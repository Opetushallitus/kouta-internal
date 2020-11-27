package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.KoutaConfigurationFactory
import fi.oph.kouta.internal.client.OrganisaatioClient
import fi.oph.kouta.internal.domain.oid.OrganisaatioOid
import fi.oph.kouta.internal.security._
import fi.vm.sade.utils.slf4j.Logging

import scala.collection.IterableView

trait AuthorizationService extends Logging {

  private lazy val rootOrganisaatioOid = KoutaConfigurationFactory.configuration.securityConfiguration.rootOrganisaatio

  protected lazy val indexerRoles: Seq[Role] = Seq(Role.Indexer)

  def withAuthorizedChildOrganizationOids[R](
      roles: Seq[Role]
  )(f: IterableView[OrganisaatioOid, Iterable[_]] => R)(implicit authenticated: Authenticated): R =
    organizationsForRoles(roles) match {
      case oids if oids.isEmpty => throw RoleAuthorizationFailedException(roles, authenticated.session.roles)
      case oids                 => f(oids.view ++ lazyFlatChildren(oids))
    }

  def authorize[R](allowedOrganization: OrganisaatioOid, authorizedOrganizations: Iterable[OrganisaatioOid])(r: R): R =
    authorizeRootOrAny(Set(allowedOrganization), authorizedOrganizations)(r)

  def authorizeRootOrAny[R](
      allowedOrganizations: Set[OrganisaatioOid],
      authorizedOrganizations: Iterable[OrganisaatioOid]
  )(r: R): R =
    if (
      authorizedOrganizations
        .exists(authorized => authorized == rootOrganisaatioOid || allowedOrganizations.contains(authorized))
    ) {
      r
    } else {
      throw OrganizationAuthorizationFailedException(allowedOrganizations)
    }

  private def organizationsForRoles(roles: Seq[Role])(implicit authenticated: Authenticated): Set[OrganisaatioOid] =
    roles.flatMap { role =>
      authenticated.session.roleMap.get(role)
    }.fold(Set())(_ union _)

  private def lazyFlatChildren(orgs: Set[OrganisaatioOid]): IterableView[OrganisaatioOid, Iterable[_]] =
    orgs.view.filterNot(_ == rootOrganisaatioOid).flatMap(oid => OrganisaatioClient.getAllChildOidsFlat(oid).get)

  def hasRootAccess(roles: Seq[Role])(implicit authenticated: Authenticated): Boolean =
    roles.exists { role =>
      authenticated.session.roleMap.get(role).exists(_.contains(rootOrganisaatioOid))
    }

  def withRootAccess[R](roles: Seq[Role])(f: => R)(implicit authenticated: Authenticated): R =
    if (hasRootAccess(roles)) {
      f
    } else {
      throw OrganizationAuthorizationFailedException()
    }
}
