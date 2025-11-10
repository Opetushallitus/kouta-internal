package fi.oph.kouta.internal

import com.typesafe.config.{Config => TypesafeConfig}
import fi.oph.kouta.internal.domain.oid.OrganisaatioOid
import fi.oph.kouta.util.{KoutaBaseConfig, KoutaConfigFactory}
import fi.vm.sade.properties.OphProperties

case class KoutaDatabaseConfiguration(
    url: String,
    port: Int,
    username: String,
    password: String,
    numThreads: Option[Int],
    maxConnections: Option[Int],
    minConnections: Option[Int],
    registerMbeans: Option[Boolean],
    initializationFailTimeout: Option[Int],
    leakDetectionThresholdMillis: Option[Int]
)

case class SecurityConfiguration(
    username: String,
    password: String,
    casUrl: String,
    casServiceIdentifier: String,
    rootOrganisaatio: OrganisaatioOid
)

case class ElasticSearchConfiguration(
    elasticUrl: String,
    cacheTimeoutSeconds: Long,
    authEnabled: Boolean,
    username: String,
    password: String
)

case class CasClientConfiguration(username: String, password: String, casUrl: String)

case class KoutaConfiguration(config: TypesafeConfig, urlProperties: OphProperties)
    extends KoutaBaseConfig(config, urlProperties) {

  val databaseConfiguration: KoutaDatabaseConfiguration =
    KoutaDatabaseConfiguration(
      url = config.getString("kouta-internal.db.url"),
      port = config.getInt("kouta-internal.db.port"),
      username = config.getString("kouta-internal.db.user"),
      password = config.getString("kouta-internal.db.password"),
      numThreads = Option(config.getInt("kouta-internal.db.numThreads")),
      maxConnections = Option(config.getInt("kouta-internal.db.maxConnections")),
      minConnections = Option(config.getInt("kouta-internal.db.minConnections")),
      registerMbeans = Option(config.getBoolean("kouta-internal.db.registerMbeans")),
      initializationFailTimeout = Option(config.getInt("kouta-internal.db.initializationFailTimeout")),
      leakDetectionThresholdMillis = Option(config.getInt("kouta-internal.db.leakDetectionThresholdMillis"))
    )

  val securityConfiguration = SecurityConfiguration(
    username = config.getString("kouta-internal.cas.username"),
    password = config.getString("kouta-internal.cas.password"),
    casUrl = config.getString("cas.url"),
    casServiceIdentifier = config.getString("kouta-internal.cas.service"),
    rootOrganisaatio = OrganisaatioOid(config.getString("root.organisaatio.oid"))
  )

  val elasticSearchConfiguration = ElasticSearchConfiguration(
    config.getString("kouta-internal.elasticsearch.url"),
    config.getLong("kouta-internal.elasticsearch.cacheTimeoutSeconds"),
    config.getBoolean("kouta-internal.elasticsearch.auth-enabled"),
    config.getString("kouta-internal.elasticsearch.username"),
    config.getString("kouta-internal.elasticsearch.password")
  )

  val clientConfiguration = CasClientConfiguration(
    username = config.getString("kouta-internal.cas.username"),
    password = config.getString("kouta-internal.cas.password"),
    casUrl = config.getString("cas.url")
  )
}

trait KoutaConfigurationConstants {
  val SystemPropertyNameConfigProfile = "kouta-internal.config-profile"
  val SystemPropertyNameTemplate      = "kouta-internal.template-file"

  val ConfigProfileDefault  = "default"
  val ConfigProfileTemplate = "template"
}

object KoutaConfigurationFactory extends KoutaConfigFactory[KoutaConfiguration]("kouta-internal") {
  def createConfigCaseClass(config: TypesafeConfig, urlProperties: OphProperties) =
    KoutaConfiguration(config, urlProperties)
}
