package fi.oph.kouta.internal

import com.typesafe.config.{Config => TypesafeConfig}
import fi.oph.kouta.internal.domain.oid.OrganisaatioOid
import fi.vm.sade.properties.OphProperties
import fi.vm.sade.utils.config.{
  ApplicationSettings,
  ApplicationSettingsLoader,
  ApplicationSettingsParser,
  ConfigTemplateProcessor
}
import fi.vm.sade.utils.slf4j.Logging

case class KoutaDatabaseConfiguration(
    url: String,
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
    kayttooikeusUrl: String,
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
    extends ApplicationSettings(config) {

  val databaseConfiguration: KoutaDatabaseConfiguration =
    KoutaDatabaseConfiguration(
      url = config.getString("kouta-internal.db.url"),
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
    kayttooikeusUrl = config.getString("kayttooikeus-service.userDetails.byUsername"),
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

object KoutaConfigurationFactory extends Logging with KoutaConfigurationConstants {

  val profile: String = System.getProperty(SystemPropertyNameConfigProfile, ConfigProfileDefault)
  logger.info(s"Using profile '$profile'")

  val configuration: KoutaConfiguration = profile match {
    case ConfigProfileDefault  => loadOphConfiguration()
    case ConfigProfileTemplate => loadTemplatedConfiguration()
    case _ =>
      throw new IllegalArgumentException(
        s"Unknown profile '$profile'! Cannot load oph-properties! Use either " +
          s"'$ConfigProfileDefault' or '$ConfigProfileTemplate' profiles."
      )
  }

  private def loadOphConfiguration(): KoutaConfiguration = {
    val configFilePath = System.getProperty("user.home") + "/oph-configuration/kouta-internal.properties"

    val applicationSettingsParser = new ApplicationSettingsParser[KoutaConfiguration] {
      override def parse(config: TypesafeConfig): KoutaConfiguration =
        KoutaConfiguration(config, new OphProperties(configFilePath))
    }

    logger.info(s"Reading properties from '$configFilePath'")
    ApplicationSettingsLoader.loadSettings(configFilePath)(applicationSettingsParser)
  }

  private def loadTemplatedConfiguration(overrideFromSystemProperties: Boolean = false): KoutaConfiguration = {
    val templateFilePath = Option(System.getProperty(SystemPropertyNameTemplate)).getOrElse(
      throw new IllegalArgumentException(
        s"Using 'template' profile but '${SystemPropertyNameTemplate}' " +
          "system property is missing. Cannot create oph-properties!"
      )
    )

    implicit val applicationSettingsParser = new ApplicationSettingsParser[KoutaConfiguration] {
      override def parse(c: TypesafeConfig): KoutaConfiguration =
        KoutaConfiguration(
          c,
          new OphProperties("src/test/resources/kouta-internal.properties") {
            addDefault("host.virkailija", c.getString("host.virkailija"))
          }
        )
    }

    logger.info(s"Reading template variables from '${templateFilePath}'")
    ConfigTemplateProcessor.createSettings("kouta-internal", templateFilePath)
  }
}
