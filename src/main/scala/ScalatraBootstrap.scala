import fi.oph.kouta.external.servlet._
import fi.oph.kouta.external.{KoutaExternalSwagger, SwaggerServlet}
import javax.servlet.ServletContext
import org.scalatra._

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    super.init(context)

    implicit val swagger: KoutaExternalSwagger = new KoutaExternalSwagger

    context.mount(new AuthServlet(), "/auth", "auth")

    context.mount(new KoulutusServlet(), "/koulutus", "koulutus")
    context.mount(new ValintaperusteServlet(), "/valintaperuste", "valintaperuste")
    context.mount(new HakuServlet(), "/haku", "haku")
    context.mount(new ToteutusServlet(), "/toteutus", "toteutus")

    context.mount(new HealthcheckServlet(), "/healthcheck", "healthcheck")
    context.mount(new SwaggerServlet, "/swagger")

  }
}
