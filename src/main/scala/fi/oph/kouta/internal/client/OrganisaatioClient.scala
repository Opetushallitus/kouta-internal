package fi.oph.kouta.internal.client

import fi.oph.kouta.internal.KoutaConfigurationFactory
import fi.oph.kouta.internal.domain.oid.OrganisaatioOid
import fi.oph.kouta.internal.util.KoutaJsonFormats
import fi.vm.sade.properties.OphProperties
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.annotation.tailrec

object OrganisaatioClient extends HttpClient with KoutaJsonFormats {
  val urlProperties: OphProperties = KoutaConfigurationFactory.configuration.urlProperties
  private val rootOrganisaatioOid  = KoutaConfigurationFactory.configuration.securityConfiguration.rootOrganisaatio

  case class OrganisaatioResponse(numHits: Int, organisaatiot: List[OidAndChildren])
  case class OidAndChildren(oid: OrganisaatioOid, children: List[OidAndChildren], parentOidPath: String)

  def getAllChildOidsFlat(oid: OrganisaatioOid): Option[Set[OrganisaatioOid]] = getHierarkia(oid, children(oid, _))
  def getAllChildOidsFlat(oids: Set[OrganisaatioOid]): Option[Set[OrganisaatioOid]] =
    oids.foldLeft[Option[Set[OrganisaatioOid]]](Some(Set.empty))((result, oid) =>
      result.flatMap(r => getAllChildOidsFlat(oid).map(r ++ _))
    )

  private def getHierarkia[R](oid: OrganisaatioOid, result: List[OidAndChildren] => R): Option[R] = {
    if (rootOrganisaatioOid == oid) {
      None
    } else {
      val url = urlProperties.url("organisaatio-service.organisaatio.hierarkia", queryParams(oid.toString))
      get(url) { response =>
        Some(result(parse(response).extract[OrganisaatioResponse].organisaatiot))
      }
    }
  }

  private def queryParams(oid: String) =
    toQueryParams("oid" -> oid, "aktiiviset" -> "true", "suunnitellut" -> "true", "lakkautetut" -> "false")

  private def children(oid: OrganisaatioOid, organisaatiot: List[OidAndChildren]): Set[OrganisaatioOid] =
    find(oid, organisaatiot).fold(Set.empty[OrganisaatioOid])(x => childOidsFlat(x) + x.oid)

  @tailrec
  private def find(oid: OrganisaatioOid, level: List[OidAndChildren]): Option[OidAndChildren] =
    level.find(_.oid == oid) match {
      case None if level.isEmpty => None
      case Some(c)               => Some(c)
      case None                  => find(oid, level.flatMap(_.children))
    }

  private def childOidsFlat(item: OidAndChildren): Set[OrganisaatioOid] =
    item.children.foldLeft(Set.empty[OrganisaatioOid])((s, c) => s ++ childOidsFlat(c) + c.oid)

}
