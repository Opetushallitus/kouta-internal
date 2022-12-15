package fi.oph.kouta.internal.integration.fixture

import fi.oph.kouta.internal.database.SessionDAO
import fi.oph.kouta.internal.client.HakukohderyhmaClient
import fi.oph.kouta.internal.elasticsearch.{HakuClient, HakukohdeClient, KoulutusClient, ToteutusClient, ValintaperusteClient}
import fi.oph.kouta.internal.security.{CasSession, CasSessionService, SecurityContext, ServiceTicket}
import fi.oph.kouta.internal.service.{HakuService, HakukohdeService, KoulutusService, ToteutusService, ValintaperusteService}
import fi.oph.kouta.internal.servlet.{AuthServlet, HakuServlet, HakukohdeServlet, HealthcheckServlet, KoulutusServlet, ToteutusServlet, ValintaperusteServlet}
import fi.oph.kouta.internal.swagger.SwaggerServlet
import fi.oph.kouta.internal.{KoutaConfigurationFactory, MockSecurityContext, TestSetups}
import fi.vm.sade.properties.OphProperties
import org.scalatra.test.scalatest.ScalatraFlatSpec

import java.util.UUID

trait SwaggerFixture extends ScalatraFlatSpec {
  KoutaConfigurationFactory.setupWithDefaultTemplateFile()
  TestSetups.setupPostgres()
  val KoulutusPath       = "/koulutus"
  val ToteutusPath       = "/toteutus"
  val HakukohdePath      = "/hakukohde"
  val HakuPath           = "/haku"
  val ValintaperustePath = "/valintaperuste"
  val AuthPath           = "/auth"

  val serviceIdentifier  = KoutaIntegrationSpec.serviceIdentifier
  val rootOrganisaatio   = KoutaIntegrationSpec.rootOrganisaatio
  val defaultAuthorities = KoutaIntegrationSpec.defaultAuthorities

  val defaultSessionId = UUID.randomUUID()

  val testUser = TestUser("test-user-oid", "testuser", defaultSessionId)

  var urlProperties: Option[OphProperties] = None

  val casUrl = "testCasUrl"

  override def beforeAll(): Unit = {
    super.beforeAll()
    SessionDAO.store(CasSession(ServiceTicket(testUser.ticket), testUser.oid, defaultAuthorities), testUser.sessionId)
    urlProperties = Some(KoutaConfigurationFactory.configuration.urlProperties)
    val hakukohderyhmaClient = new HakukohderyhmaClientMock()


    val koulutusService =
      new KoulutusService(KoulutusClient)
    addServlet(new KoulutusServlet(koulutusService, SessionDAO), KoulutusPath)

    val toteutusService =
      new ToteutusService(ToteutusClient)
    addServlet(new ToteutusServlet(toteutusService, SessionDAO), ToteutusPath)

    val hakuService = new HakuService(HakuClient)
    addServlet(new HakuServlet(hakuService, SessionDAO), HakuPath)

    val hakukohdeService = new HakukohdeService(
      HakukohdeClient,
      hakuService,
      hakukohderyhmaClient
    )
    addServlet(new HakukohdeServlet(hakukohdeService, SessionDAO), HakukohdePath)

    val valintaperusteService = new ValintaperusteService(
      ValintaperusteClient
    )
    addServlet(new ValintaperusteServlet(valintaperusteService, SessionDAO), ValintaperustePath)

    val securityContext: SecurityContext = MockSecurityContext(casUrl, serviceIdentifier, defaultAuthorities)
    val kayttooikeusClient               = new KayttooikeusClientMock(securityContext, defaultAuthorities)

    object MockCasSessionService extends CasSessionService(securityContext, kayttooikeusClient, SessionDAO)

    addServlet(new AuthServlet(MockCasSessionService), AuthPath)
    addServlet(HealthcheckServlet, "/healthcheck")
    addServlet(new SwaggerServlet(), "/swagger")
  }

  override def afterAll(): Unit = {
    super.afterAll()
  }
}