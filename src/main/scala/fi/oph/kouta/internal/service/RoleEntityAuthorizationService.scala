package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.domain.Perustiedot
import fi.oph.kouta.internal.security.{Authenticated, RoleEntity}

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait RoleEntityAuthorizationService extends AuthorizationService {
  protected val roleEntity: RoleEntity

  def authorizeGet[E <: Perustiedot](
      entityWithTime: Option[(E, Instant)]
  )(implicit authenticated: Authenticated): Option[(E, Instant)] =
    entityWithTime.map { case (entity, lastModified) =>
      withAuthorizedChildOrganizationOids(roleEntity.readRoles) { authorizedOrganizations =>
        authorize(entity.organisaatioOid, authorizedOrganizations) {
          (entity, lastModified)
        }
      }
    }

  def authorizeGet[E <: Perustiedot](entity: Future[E])(implicit authenticated: Authenticated): Future[E] =
    entity.map { e =>
      withAuthorizedChildOrganizationOids(roleEntity.readRoles) { authorizedOrganizations =>
        authorize(e.organisaatioOid, authorizedOrganizations) {
          e
        }
      }
    }

  def authorizePut[E <: Perustiedot, I](entity: E)(f: => I)(implicit authenticated: Authenticated): I =
    withAuthorizedChildOrganizationOids(roleEntity.createRoles) { authorizedOrganizations =>
      authorize(entity.organisaatioOid, authorizedOrganizations) {
        f
      }
    }

  def authorizeUpdate[E <: Perustiedot, I](
      entityForUpdate: => Option[(E, Instant)]
  )(f: => I)(implicit authenticated: Authenticated): I = {
    withAuthorizedChildOrganizationOids(roleEntity.updateRoles) { authorizedOrganizations =>
      entityForUpdate match {
        case None => throw new NoSuchElementException()
        case Some(existing) =>
          authorize(existing._1.organisaatioOid, authorizedOrganizations) {
            f
          }
      }
    }
  }
}
