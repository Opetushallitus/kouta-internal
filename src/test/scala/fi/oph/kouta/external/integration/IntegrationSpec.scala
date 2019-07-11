package fi.oph.kouta.external.integration

import fi.oph.kouta.external.EmbeddedJettyLauncher.setupForTestTemplate
import org.scalatra.test.scalatest.ScalatraFlatSpec

trait IntegrationSpec extends ScalatraFlatSpec {


  override def beforeAll(): Unit = {
    super.beforeAll()
    setupForTestTemplate()
  }
}
