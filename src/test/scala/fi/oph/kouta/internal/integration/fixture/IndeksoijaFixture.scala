package fi.oph.kouta.internal.integration.fixture

import clojure.java.api.Clojure
import fi.oph.kouta.internal.TempElasticClientHolder
import fi.oph.kouta.internal.domain.oid.HakuOid

trait IndeksoijaFixture {
  val require = Clojure.`var`("clojure.core", "require")

  val elasticUtils = "clj-elasticsearch.elastic-utils"
  require.invoke(Clojure.read(elasticUtils))

  val indexerFixture = "kouta-indeksoija-service.fixture.kouta-indexer-fixture"
  require.invoke(Clojure.read(indexerFixture))
  val _indexAll = Clojure.`var`(indexerFixture, "index-all")
  val indexWithoutRelated = Clojure.`var`(indexerFixture, "index-oids-without-related-indices")

  val intern = Clojure.`var`("clojure.core", "intern")
  intern.invoke(
    Clojure.read("clj-elasticsearch.elastic-utils"),
    Clojure.read("elastic-host"),
    Clojure.read(TempElasticClientHolder.elasticUrl)
  )

  def indexAll(): Unit = _indexAll.invoke()

  def indexHaku(hakuOid: HakuOid): Unit = {
    indexWithoutRelated.invoke(Clojure.read(s"{ :haut [${'"'}${hakuOid.s}${'"'}]}"))
  }
}
