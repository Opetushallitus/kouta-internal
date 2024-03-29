package fi.oph.kouta.internal

import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties}
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.utility.DockerImageName

object TempElasticClient {
  val url    = s"http://localhost:${TempElastic.start()}"
  val client = ElasticClient(JavaClient(ElasticProperties(url)))
}

private object TempElastic {
  var elastic: Option[ElasticsearchContainer] = None

  def start(): Int = {

    try {
      get().getMappedPort(9200)
    } finally {
      Runtime.getRuntime.addShutdownHook(new Thread(() => stop()))
    }
  }

  def create(): ElasticsearchContainer = {
    val dockerImager = DockerImageName
      .parse("190073735177.dkr.ecr.eu-west-1.amazonaws.com/utility/elasticsearch-kouta:8.5.2")
      .asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch")
    val embeddedElastic = new ElasticsearchContainer(dockerImager)
    embeddedElastic.getEnvMap.put("xpack.security.enabled", "false")

    embeddedElastic.start()
    elastic = Some(embeddedElastic)
    embeddedElastic
  }

  def stop(): Unit = {
    elastic.foreach(_.stop())
    elastic.foreach(_.close())
    elastic = None
  }

  def get(): ElasticsearchContainer = elastic.getOrElse(create())
}
