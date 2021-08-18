package fi.oph.kouta.internal

import com.sksamuel.elastic4s.http.{ElasticClient, ElasticProperties}
import org.testcontainers.elasticsearch.ElasticsearchContainer

object TempElasticClient {
  val url    = s"http://localhost:${TempElastic.start()}"
  val client = ElasticClient(ElasticProperties(url))
}

object TempElastic {
  var elastic: Option[ElasticsearchContainer] = None
  var port: Int                               = -1

  def start(): Int = port

  def create(): ElasticsearchContainer = {
    val embeddedElastic = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.10.2")

    embeddedElastic.start()
    port = embeddedElastic.getMappedPort(9200)
    elastic = Some(embeddedElastic)
    embeddedElastic
  }

  def stop(): Unit = {
    elastic.foreach(_.stop())
    elastic.foreach(_.close())
    port = -1
    elastic = None
  }

  def get(): ElasticsearchContainer = elastic.getOrElse(create())
}
