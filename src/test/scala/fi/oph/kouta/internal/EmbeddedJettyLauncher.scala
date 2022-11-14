package fi.oph.kouta.internal

import fi.oph.kouta.internal.elasticsearch.ElasticsearchHealth
import fi.vm.sade.utils.slf4j.Logging

object EmbeddedJettyLauncher extends Logging {

  val DefaultPort = "8098"

  def main(args: Array[String]): Unit = {
    KoutaConfigurationFactory.setupWithDevTemplate();
    TestSetups.setupPostgres()

    val port = System.getProperty("kouta-internal.port", DefaultPort).toInt

    val elasticStatus = ElasticsearchHealth.checkStatus()
    logger.info(
      s"Status of Elasticsearch cluster is ${elasticStatus.toString} ${if (elasticStatus.healthy) '\u2714' else '\u274C'}"
    )

    logger.info(s"Starting standalone Kouta-intenal Jetty on port $port...")
    logger.info(s"http://localhost:$port/kouta-external/swagger")
    new JettyLauncher(port).start().join()
  }
}

object TestSetups extends Logging {
  def setupPostgres() = {
    System.getProperty("kouta-internal.embedded", "true") match {
      case x if !"false".equalsIgnoreCase(x) => TempDockerDb.start()
      case _                                 =>
    }
  }
}
