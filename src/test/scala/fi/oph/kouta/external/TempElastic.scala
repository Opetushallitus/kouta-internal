package fi.oph.kouta.external

import java.util.concurrent.TimeUnit

import com.sksamuel.elastic4s.http.{ElasticClient, ElasticProperties}
import fi.vm.sade.utils.tcp.PortFromSystemPropertyOrFindFree
import pl.allegro.tech.embeddedelasticsearch.{EmbeddedElastic, PopularProperties}

object TempElasticClientHolder {
  private lazy val elasticUrl = s"http://localhost:${TempElastic.start()}"

  lazy val client: ElasticClient = ElasticClient(ElasticProperties(elasticUrl))
}



object TempElastic {
  var elasticInstance: Option[EmbeddedElastic] = None

  private val port = new PortFromSystemPropertyOrFindFree("kouta-external.elastic.port").chosenPort
  private val timeoutInMillis = 60 * 1000

  def start() = elasticInstance.getOrElse(create()).getHttpPort

  def create(): EmbeddedElastic = {
    val embeddedElastic = EmbeddedElastic.builder()
      .withElasticVersion("6.0.0")
      .withSetting(PopularProperties.HTTP_PORT, port)
      .withSetting(PopularProperties.CLUSTER_NAME, "elasticsearch")
      .withSetting("discovery.zen.ping.unicast.hosts", Seq(s"127.0.0.1:$port"))
      .withStartTimeout(timeoutInMillis, TimeUnit.MILLISECONDS)
      .build

    elasticInstance = Some(embeddedElastic.start())

    Runtime.getRuntime.addShutdownHook(new Thread(() => stop()))
    elasticInstance.get
  }

  def stop(): Unit = elasticInstance.foreach(_.stop())

}
