package fi.oph.kouta.external

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import fi.vm.sade.utils.slf4j.Logging
import org.eclipse.jetty.util.resource.Resource

class JettyLauncher(val port: Int) {
  val server  = new Server(port)
  val context = new WebAppContext()
  context.setBaseResource(Resource.newClassPathResource("webapp"))
  context.setDescriptor("WEB-INF/web.xml")
  context.setContextPath("/kouta-external")
  server.setHandler(context)

  def start: Server = {
    server.start()
    server
  }
}

object JettyLauncher extends Logging {
  val DEFAULT_PORT = "8080"

  def main(args: Array[String]) {
    val port = System.getProperty("kouta-external.port", DEFAULT_PORT).toInt
    logger.info(s"Starting standalone Kouta-external Jetty on port $port...")
    new JettyLauncher(port).start.join()
  }
}
