import fi.oph.kouta.internal.database.KoutaDatabase
import fi.oph.kouta.internal.servlet._
import fi.oph.kouta.internal.swagger.SwaggerServlet
import javax.servlet.ServletContext
import org.scalatra._

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    super.init(context)

    context.mount(AuthServlet, "/auth", "auth")

    context.mount(KoulutusServlet, "/koulutus", "koulutus")
    context.mount(ValintaperusteServlet, "/valintaperuste", "valintaperuste")
    context.mount(HakuServlet, "/haku", "haku")
    context.mount(HakukohdeServlet, "/hakukohde", "hakukohde")
    context.mount(ToteutusServlet, "/toteutus", "toteutus")

    context.mount(HealthcheckServlet, "/healthcheck", "healthcheck")
    context.mount(new SwaggerServlet, "/swagger")

    context.mount(OdwServlet, "/odw", "odw")
  }

  override def destroy(context: ServletContext): Unit = {
    super.destroy(context)
    KoutaDatabase.destroy()
  }

}
