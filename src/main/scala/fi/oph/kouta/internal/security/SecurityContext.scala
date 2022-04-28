package fi.oph.kouta.internal.security

import fi.oph.kouta.internal.SecurityConfiguration
import fi.oph.kouta.internal.client.CallerId
import fi.oph.kouta.internal.util.ScalaCasConfig
import fi.vm.sade.javautils.nio.cas.{CasClient, CasClientBuilder}
//import fi.vm.sade.utils.cas.CasClient

trait SecurityContext {
  def casUrl: String
  def casServiceIdentifier: String
  def casClient: CasClient
}

case class ProductionSecurityContext(casUrl: String, casClient: CasClient, casServiceIdentifier: String)
    extends SecurityContext

object ProductionSecurityContext extends CallerId {
  def apply(config: SecurityConfiguration): ProductionSecurityContext = {
//    val casClient = new CasClient(config.casUrl, org.http4s.client.blaze.defaultClient, callerId)
    val casClient = CasClientBuilder.build(
      ScalaCasConfig(
        config.username,
        config.password,
        config.casUrl,
        config.kayttooikeusUrl,
        csrf = callerId,
        callerId = callerId,
        serviceUrlSuffix = "/j_spring_cas_security_check",
        jSessionName = "JSESSIONID"
      )
    )

    ProductionSecurityContext(config.casUrl, casClient, config.casServiceIdentifier)
  }
}
