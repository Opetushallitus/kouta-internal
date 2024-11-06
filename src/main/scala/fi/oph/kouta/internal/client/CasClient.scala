package fi.oph.kouta.internal.client

import fi.oph.kouta.internal.KoutaConfigurationFactory
import fi.oph.kouta.internal.util.{KoutaJsonFormats, ScalaCasConfig}
import fi.vm.sade.javautils.nio.cas.{CasClient => SadeCasClient, CasClientBuilder}
import fi.vm.sade.properties.OphProperties
import fi.oph.kouta.logging.Logging
import org.asynchttpclient.{RequestBuilder, Response}
import org.http4s.{Headers, Method}
import org.json4s.{Extraction, Writer}
import org.json4s.jackson.JsonMethods.{compact, render}

import java.util.concurrent.CompletableFuture
import scala.compat.java8.FutureConverters.toScala
import scala.concurrent.Future

object CasClient {
  type KoutaResponse[T] = Either[(Int, String), T]
}

abstract class KoutaClient extends KoutaJsonFormats with Logging with CallerId {
  protected def urlProperties: OphProperties = KoutaConfigurationFactory.configuration.urlProperties

  protected val loginParams: String
  protected val sessionCookieName: String
  protected val serviceName: String

  lazy protected val client: SadeCasClient = {
    val config = KoutaConfigurationFactory.configuration.clientConfiguration
    CasClientBuilder.build(
      ScalaCasConfig(
        config.username,
        config.password,
        config.casUrl,
        serviceName,
        callerId,
        callerId,
        loginParams,
        sessionCookieName
      )
    )
  }

  protected def fetch[T](method: Method, url: String, body: T, headers: Headers): Future[(Int, String)] = {
    implicit val writer: Writer[T] = (obj: T) => Extraction.decompose(obj)
    val requestBuilder = new RequestBuilder()
      .setMethod(method.name)
      .setUrl(url)
      .setBody(compact(render(writer.write(body))))
    headers.foreach(h => requestBuilder.addHeader(h.name, h.value))

    val request = requestBuilder.build()

    def responseToStatusAndBody(r: Response): (Int, String) = {
      (r.getStatusCode, r.getResponseBody)
    }

    val future: CompletableFuture[(Int, String)] = client.execute(request).thenApply(responseToStatusAndBody)
    toScala(future)
  }
}
