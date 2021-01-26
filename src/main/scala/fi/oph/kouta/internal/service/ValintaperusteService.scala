package fi.oph.kouta.internal.service

import java.util.UUID

import fi.oph.kouta.internal.domain.Valintaperuste
import fi.oph.kouta.internal.elasticsearch.ValintaperusteClient
import fi.oph.kouta.internal.security.Authenticated

import scala.concurrent.Future

class ValintaperusteService(valintaperusteClient: ValintaperusteClient) {
  def get(id: UUID)(implicit authenticated: Authenticated): Future[Valintaperuste] =
    valintaperusteClient.getValintaperuste(id)
}

object ValintaperusteService extends ValintaperusteService(ValintaperusteClient)
