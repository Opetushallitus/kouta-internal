package fi.oph.kouta.internal.client

import com.github.blemale.scaffeine.Scaffeine
import fi.oph.kouta.internal.KoutaConfigurationFactory
import fi.oph.kouta.internal.domain.oid.OrganisaatioOid
import fi.oph.kouta.internal.util.KoutaJsonFormats
import fi.vm.sade.properties.OphProperties
import fi.vm.sade.utils.slf4j.Logging
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.concurrent.duration._
import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object OrganisaatioClient extends HttpClient with KoutaJsonFormats with Logging {
  val urlProperties: OphProperties = KoutaConfigurationFactory.configuration.urlProperties
  private val rootOrganisaatioOid  = KoutaConfigurationFactory.configuration.securityConfiguration.rootOrganisaatio

  case class OrganisaatioResponse(numHits: Int, organisaatiot: List[OidAndChildren])
  case class OidAndChildren(oid: OrganisaatioOid, children: List[OidAndChildren], parentOidPath: String)

  def asyncGetAllChildOidsFlat(oid: OrganisaatioOid): Future[Option[Set[OrganisaatioOid]]] =
    asyncGetHierarkia(oid, children(oid, _))
  def asyncGetAllChildOidsFlat(oids: Set[OrganisaatioOid]): Future[Option[Set[OrganisaatioOid]]] = {
    Future
      .sequence(
        oids
          .map(asyncGetAllChildOidsFlat)
          .map(r => r.map(_.getOrElse(Set.empty)))
      )
      .map(r => Some(r.flatten))
  }

  def asyncGetAllChildOidsFlat(oids: Option[Set[OrganisaatioOid]]): Future[Option[Set[OrganisaatioOid]]] = {
    oids match {
      case None =>
        Future.successful(None)
      case Some(someOids) =>
        asyncGetAllChildOidsFlat(someOids)
    }
  }
  private lazy val cache = Scaffeine()
    .expireAfterWrite(60.minutes)
    .buildAsync[String, Any]()

  private def asyncGetHierarkia[R](oid: OrganisaatioOid, result: List[OidAndChildren] => R): Future[Option[R]] = {
    if (rootOrganisaatioOid == oid) {
      Future.successful(None)
    } else {
      val url = urlProperties.url("organisaatio-service.organisaatio.hierarkia", queryParams(oid.toString))

      cache
        .getFuture(
          url,
          key =>
            asyncGet(key) { response =>
              Some(result(parse(response).extract[OrganisaatioResponse].organisaatiot))
            }
        )
        .mapTo[Option[R]]
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
