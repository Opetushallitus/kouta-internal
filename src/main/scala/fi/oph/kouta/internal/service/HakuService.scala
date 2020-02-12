package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.domain.Haku
import fi.oph.kouta.internal.domain.oid.HakuOid
import fi.oph.kouta.internal.elasticsearch.HakuClient
import fi.oph.kouta.internal.security.{Authenticated, Role, RoleEntity}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HakuService(hakuClient: HakuClient)
    extends RoleEntityAuthorizationService {

  override val roleEntity: RoleEntity = Role.Haku

  def get(oid: HakuOid)(implicit authenticated: Authenticated): Future[Haku] =
    authorizeGet(hakuClient.getHaku(oid))

  def searchByAtaruId(ataruId: String)(implicit authenticated: Authenticated): Future[Seq[Haku]] = {
    val haut = hakuClient.searchByAtaruId(ataruId)

    if (hasRootAccess(roleEntity.readRoles)) {
      haut
    } else {
      withAuthorizedChildOrganizationOids(roleEntity.readRoles) { orgs =>
        haut.map(_.filter(h => orgs.exists(_ == h.organisaatioOid)))
      }
    }
  }
}

object HakuService extends HakuService(HakuClient)