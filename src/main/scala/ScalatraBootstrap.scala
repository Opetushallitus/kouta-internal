import fi.oph.kouta.external._
import fi.oph.kouta.external.servlet.HealthcheckServlet
import javax.servlet.ServletContext
import org.scalatra._

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {

    super.init(context)

    context.mount(new MyScalatraServlet, "/*")

    implicit val swagger: KoutaBackendSwagger = new KoutaBackendSwagger

    context.mount(new HealthcheckServlet(), "/healthcheck", "healthcheck")
    context.mount(new SwaggerServlet, "/swagger")

  }
}
