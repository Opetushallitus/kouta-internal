import fi.oph.kouta.external._
import fi.oph.kouta.external.servlet.{AuthServlet, HealthcheckServlet, KoulutusServlet}
import javax.servlet.ServletContext
import org.scalatra._

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    super.init(context)

    implicit val swagger: KoutaExternalSwagger = new KoutaExternalSwagger

    context.mount(new AuthServlet(), "/auth", "auth")

    context.mount(new KoulutusServlet(), "/koulutus", "koulutus")

    context.mount(new HealthcheckServlet(), "/healthcheck", "healthcheck")
    context.mount(new SwaggerServlet, "/swagger")

  }
}
