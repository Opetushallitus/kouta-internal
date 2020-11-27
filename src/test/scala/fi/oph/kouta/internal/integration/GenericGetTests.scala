package fi.oph.kouta.internal.integration

import java.util.UUID

import fi.oph.kouta.internal.integration.fixture.{AccessControlSpec, KoutaIntegrationSpec}

trait GenericGetTests[E, ID] {
  this: KoutaIntegrationSpec with AccessControlSpec =>

  val getPath: String
  val entityName: String
  val existingId: ID
  val nonExistingId: ID

  def get(id: ID, sessionId: UUID): E
  def get(id: ID, sessionId: UUID, errorStatus: Int): Unit

  def getTests(): Unit = { // Metodin sisällä, jotta entityName bindataan vasta sen jälkeen kun se on overridattu

    "GET /:id" should s"get $entityName from elastic search" in {
      get(existingId, defaultSessionId)
    }

    it should s"return 404 if $entityName not found" in {
      get(s"$getPath/$nonExistingId", headers = Seq(defaultSessionHeader)) {
        status should equal(404)
        body should include(s"Didn't find id $nonExistingId")
      }
    }

    it should "return 401 without a valid session" in {
      get(s"$getPath/$nonExistingId") {
        status should equal(401)
        body should include("Unauthorized")
      }
    }
  }
}
