import fi.oph.kouta.internal.KoutaConfigurationFactory
import fi.oph.kouta.internal.database.KoutaDatabase
import fi.oph.kouta.internal.elasticsearch.DefaultElasticsearchClientHolder
import fi.oph.kouta.internal.servlet._
import fi.oph.kouta.internal.swagger.SwaggerServlet
import javax.servlet.ServletContext
import org.scalatra._

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    super.init(context)

    KoutaConfigurationFactory.init()
    KoutaDatabase.init()

    context.mount(new AuthServlet(), "/auth", "auth")

    context.mount(new KoulutusServlet(), "/koulutus", "koulutus")
    context.mount(new ValintaperusteServlet(), "/valintaperuste", "valintaperuste")
    context.mount(new HakuServlet(DefaultElasticsearchClientHolder), "/haku", "haku")
    context.mount(new HakukohdeServlet(), "/hakukohde", "hakukohde")
    context.mount(new ToteutusServlet(), "/toteutus", "toteutus")

    context.mount(new HealthcheckServlet(), "/healthcheck", "healthcheck")
    context.mount(new SwaggerServlet, "/swagger")

  }

  override def destroy(context: ServletContext): Unit = {
    super.destroy(context)
    KoutaDatabase.destroy()
  }

}
