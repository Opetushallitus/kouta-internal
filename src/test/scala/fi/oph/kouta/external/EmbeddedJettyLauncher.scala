package fi.oph.kouta.external

import fi.oph.kouta.external.elasticsearch.ElasticsearchHealth
import fi.vm.sade.utils.slf4j.Logging

object EmbeddedJettyLauncher extends Logging with KoutaConfigurationConstants {

  val DefaultPort = "8097"

  val TestTemplateFilePath = "src/test/resources/dev-vars.yml"

  def main(args: Array[String]) {

    val port = System.getProperty("kouta-external.port", DefaultPort).toInt

    setupForTestTemplate()
    val elasticStatus = ElasticsearchHealth.checkStatus()
    logger.info(s"Status of Elasticsearch cluster is ${elasticStatus.toString} ${if (elasticStatus.healthy) '\u2714' else '\u274C'}")

    logger.info(s"Starting standalone Kouta-external Jetty on port $port...")
    logger.info(s"http://localhost:$port/kouta-external/swagger")
    new JettyLauncher(port).start.join()
  }

  def setupForTestTemplate() = {
    System.setProperty(SystemPropertyNameConfigProfile, ConfigProfileTemplate)
    System.setProperty(SystemPropertyNameTemplate, TestTemplateFilePath)
  }
}
