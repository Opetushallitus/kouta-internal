package fi.oph.kouta.internal.integration.fixture

import clojure.java.api.Clojure
import fi.oph.kouta.internal.TempElasticClientHolder
import fi.oph.kouta.internal.domain.oid._

trait IndeksoijaFixture {
  private val require = Clojure.`var`("clojure.core", "require")

  private val elasticUtils = "clj-elasticsearch.elastic-utils"
  require.invoke(Clojure.read(elasticUtils))

  private val indexerFixture = "kouta-indeksoija-service.fixture.kouta-indexer-fixture"
  require.invoke(Clojure.read(indexerFixture))

  private val _indexAll           = Clojure.`var`(indexerFixture, "index-all")
  private val indexWithoutRelated = Clojure.`var`(indexerFixture, "index-oids-without-related-indices")
  private val teardown       = Clojure.`var`(indexerFixture, "teardown")
  private val refreshIndices = Clojure.`var`(indexerFixture, "refresh-indices")
  private val init           = Clojure.`var`(indexerFixture, "init")

  private val intern = Clojure.`var`("clojure.core", "intern")
  intern.invoke(
    Clojure.read("clj-elasticsearch.elastic-utils"),
    Clojure.read("elastic-host"),
    Clojure.read(TempElasticClientHolder.elasticUrl)
  )

  def initIndices(): Unit = {
    init.invoke()
  }

  def resetIndices(): Unit = {
    teardown.invoke()
  }

  def indexAll(): Unit = _indexAll.invoke()

  def indexHaku(hakuOid: HakuOid): Unit = {
    indexWithoutRelated.invoke(Clojure.read(s"{ :haut [${'"'}${hakuOid.s}${'"'}]}"))
  }

  def indexKoulutus(koulutusOid: KoulutusOid): Unit = {
    indexWithoutRelated.invoke(Clojure.read(s"{ :koulutukset [${'"'}${koulutusOid.s}${'"'}]}"))
  }

}
