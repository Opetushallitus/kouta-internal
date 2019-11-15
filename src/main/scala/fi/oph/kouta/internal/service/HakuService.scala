package fi.oph.kouta.internal.service

import java.util.UUID

import fi.oph.kouta.internal.domain.Haku
import fi.oph.kouta.internal.domain.oid.HakuOid
import fi.oph.kouta.internal.elasticsearch.{ElasticsearchClientHolder, HakuClient}
import fi.oph.kouta.internal.security.{Authenticated, Role, RoleEntity}
import fi.vm.sade.utils.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HakuService(elasticsearchClientHolder: ElasticsearchClientHolder)
    extends RoleEntityAuthorizationService
    with Logging {

  override val roleEntity: RoleEntity = Role.Haku

  val hakuClient = new HakuClient("haku-kouta", elasticsearchClientHolder)

  def get(oid: HakuOid)(implicit authenticated: Authenticated): Future[Haku] =
    authorizeGet(hakuClient.getHaku(oid))

  def searchByAtaruId(ataruId: UUID)(implicit authenticated: Authenticated): Future[Seq[Haku]] = {
    val res = hakuClient.searchByAtaruId(ataruId)

    if (hasRootAccess(roleEntity.readRoles)) {
      res
    } else {
      withAuthorizedChildOrganizationOids(roleEntity.readRoles) { orgs =>
        res.map(_.filter(h => orgs.exists(_ == h.organisaatioOid)))
      }
    }
  }
}
