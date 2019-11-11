package fi.oph.kouta.internal.integration.fixture

import clojure.java.api.Clojure
import fi.oph.kouta.internal.TempElasticClientHolder
import fi.oph.kouta.internal.domain.Haku
import fi.oph.kouta.internal.domain.oid.HakuOid
import fi.oph.kouta.internal.integration.KoutaIntegrationSpec
import fi.oph.kouta.internal.servlet.HakuServlet

abstract class HakuFixture extends KoutaIntegrationSpec {
  val HakuPath = "/haku"

  addServlet(new HakuServlet(TempElasticClientHolder), HakuPath)

  val addHakuMock = Clojure.`var`(indexerFixture, "add-haku-mock")

  def get(oid: HakuOid): Haku = get[Haku, HakuOid](HakuPath, oid)

  def addHaku(hakuOid: HakuOid): Unit = {
    addHakuMock.invoke(Clojure.read(s"${'"'}${hakuOid.s}${'"'}"))
  }
}
