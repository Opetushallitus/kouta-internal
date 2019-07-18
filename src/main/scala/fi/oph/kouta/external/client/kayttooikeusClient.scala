package fi.oph.kouta.external.client

import fi.oph.kouta.external.KoutaConfigurationFactory
import fi.oph.kouta.external.security._
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.jackson.JsonMethods.parse

object KayttooikeusClient extends KayttooikeusClient

trait KayttooikeusClient extends HttpClient with Logging {

  import org.json4s._

  private implicit val formats   = DefaultFormats
  private lazy val urlProperties = KoutaConfigurationFactory.configuration.urlProperties
  private lazy val allowOldTarjontaRole =
    KoutaConfigurationFactory.configuration.securityConfiguration.allowOldTarjontaRole

  def getUserByUsername(username: String): KayttooikeusUserDetails = {
    val url = urlProperties.url(s"kayttooikeus-service.userDetails.byUsername", username)

    val errorHandler = (_: String, status: Int, response: String) =>
      status match {
        case 404 =>
          throw new AuthenticationFailedException(
            s"User not found with username: $username, got response $status $response"
          )
        case _ =>
          throw new InternalError(s"Failed to get username $username details, got response $status $response")
      }

    get(url, errorHandler) { response =>
      val kayttooikeusDto = parse(response).extract[KayttooikeusUserResp]
      KayttooikeusUserDetails(
        kayttooikeusDto.authorities
          .map(a => Authority(a.authority.replace("ROLE_", "")))
          .toSet
          .flatMap {
            // Allow users with the old tarjonta role to use the new tarjonta while we don't have the new authorities set up in Kaytto-oikeuspalvelu
            case a if allowOldTarjontaRole && a.role.name == "APP_TARJONTA_CRUD" && a.organisaatioId.nonEmpty =>
              logger.info(
                s"Adding CRUD roles for user $username to organization ${a.organisaatioId} because they have the authority ${a.authority}"
              )
              RoleEntity.all.map(_.Crud).map((r: Role) => Authority(r, a.organisaatioId.get)).toSet
            case a => Set(a)
          },
        kayttooikeusDto.username
      )
    }
  }
}

case class KayttooikeusUserResp(authorities: List[GrantedAuthority], username: String)

case class GrantedAuthority(authority: String)
