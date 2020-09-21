package fi.oph.kouta.internal.service

import fi.oph.kouta.internal.domain.Haku
import fi.oph.kouta.internal.domain.oid.HakuOid
import fi.oph.kouta.internal.elasticsearch.HakuClient
import fi.oph.kouta.internal.security.{Authenticated, Role, RoleEntity}

import scala.concurrent.Future

class HakuService(hakuClient: HakuClient) extends RoleEntityAuthorizationService {

  override val roleEntity: RoleEntity = Role.Haku

  def get(oid: HakuOid)(implicit authenticated: Authenticated): Future[Haku] =
    authorizeGet(hakuClient.getHaku(oid))

  def searchByAtaruId(ataruId: String)(implicit authenticated: Authenticated): Future[Seq[Haku]] =
    hakuClient.searchByAtaruId(ataruId)
}

object HakuService extends HakuService(HakuClient)
