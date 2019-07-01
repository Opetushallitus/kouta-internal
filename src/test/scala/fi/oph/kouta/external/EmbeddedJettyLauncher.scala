package fi.oph.kouta.external

import fi.vm.sade.utils.slf4j.Logging

object EmbeddedJettyLauncher extends Logging {

  val DefaultPort = "8097"

  val TestDataGeneratorSessionId = "ea596a9c-5940-497e-b5b7-aded3a2352a7"

  def main(args: Array[String]) {
    val port = System.getProperty("kouta-external.port", DefaultPort).toInt
    logger.info(s"Starting standalone Kouta-external Jetty on port $port...")
    new JettyLauncher(port).start.join
  }
}
