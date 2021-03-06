package fi.oph.kouta.internal.client

import java.util.{Map => JavaMap}

import fi.vm.sade.utils.http.DefaultHttpClient
import scalaj.http.HttpOptions._

trait HttpClient extends CallerId {
  private val DefaultConnTimeout = 30000
  private val DefaultReadTimeout = 120000

  private def defaultOptions(doFollowRedirects: Boolean = false): Seq[HttpOption] = Seq(
    connTimeout(DefaultConnTimeout),
    readTimeout(DefaultReadTimeout),
    followRedirects(doFollowRedirects)
  )

  private val HeaderClientSubSystemCode = ("clientSubSystemCode", callerId)

  def get[T](
      url: String,
      errorHandler: (String, Int, String) => Nothing = defaultErrorHandler,
      followRedirects: Boolean = true
  )(parse: String => T): T =
    DefaultHttpClient
      .httpGet(url, defaultOptions(followRedirects): _*)(callerId)
      .header(HeaderClientSubSystemCode._1, HeaderClientSubSystemCode._2)
      .responseWithHeaders match {
      case (200, _, response) => parse(response)
      case (xxx, _, response) => errorHandler(url, xxx, response)
    }

  private def defaultErrorHandler(url: String, statusCode: Int, response: String) =
    throw new RuntimeException(s"Url $url returned status code $statusCode $response")

  def toQueryParams(params: (String, String)*): JavaMap[String, String] =
    scala.collection.JavaConverters.mapAsJavaMap(Map(params: _*))
}
