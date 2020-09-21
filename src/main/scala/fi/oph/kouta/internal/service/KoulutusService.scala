package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.domain.Koulutus
import fi.oph.kouta.internal.domain.oid.KoulutusOid
import fi.oph.kouta.internal.elasticsearch.KoulutusClient
import fi.oph.kouta.internal.security.{Authenticated, Role, RoleEntity}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class KoulutusService(koulutusClient: KoulutusClient) extends RoleEntityAuthorizationService {

  override val roleEntity: RoleEntity = Role.Koulutus

  def get(oid: KoulutusOid)(implicit authenticated: Authenticated): Future[Koulutus] = {
    koulutusClient.getKoulutus(oid).map {
      case koulutus if hasRootAccess(roleEntity.readRoles) =>
        koulutus
      case koulutus if koulutus.julkinen =>
        koulutus // TODO: sallittu vain saman koulutustyypin käyttäjille
      case koulutus =>
        withAuthorizedChildOrganizationOids(roleEntity.readRoles) { authorizedOrganizations =>
          authorize(koulutus.organisaatioOid, authorizedOrganizations) {
            koulutus
          }
        }
    }
  }
}

object KoulutusService extends KoulutusService(KoulutusClient)
