package fi.oph.kouta.internal.client

import java.util.{Map => JavaMap}

import fi.vm.sade.utils.http.DefaultHttpClient
import scalaj.http.HttpOptions._

trait HttpClient {
  private val DefaultConnTimeout = 30000
  private val DefaultReadTimeout = 120000

  private val DefaultOptions: Seq[HttpOption] = Seq(
    connTimeout(DefaultConnTimeout),
    readTimeout(DefaultReadTimeout)
  )

  private val HeaderCallerId            = ("Caller-id", "kouta-internal")
  private val HeaderClientSubSystemCode = ("clientSubSystemCode", "kouta-internal")

  def get[T](url: String, errorHandler: (String, Int, String) => Nothing = defaultErrorHandler)(parse: String => T): T =
    DefaultHttpClient
      .httpGet(url, DefaultOptions: _*)
      .header(HeaderClientSubSystemCode._1, HeaderClientSubSystemCode._2)
      .header(HeaderCallerId._1, HeaderCallerId._2)
      .responseWithHeaders match {
      case (200, _, response) => parse(response)
      case (xxx, _, response) => errorHandler(url, xxx, response)
    }

  private def defaultErrorHandler(url: String, statusCode: Int, response: String) =
    throw new RuntimeException(s"Url $url returned status code $statusCode $response")

  def toQueryParams(params: (String, String)*): JavaMap[String, String] =
    scala.collection.JavaConverters.mapAsJavaMap(Map(params: _*))
}
