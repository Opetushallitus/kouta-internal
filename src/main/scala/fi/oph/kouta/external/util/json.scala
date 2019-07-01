package fi.oph.kouta.external.util

package fi.oph.kouta.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

import org.json4s.JsonAST.JString
import org.json4s.jackson.Serialization.write
import org.json4s.{CustomSerializer, DefaultFormats, Formats}

trait KoutaJsonFormats extends DefaultKoutaJsonFormats {

  implicit def jsonFormats: Formats = koutaJsonFormats

  def toJson(data: AnyRef): String = write(data)
}

sealed trait DefaultKoutaJsonFormats {

  val ISO_LOCAL_DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

  def koutaJsonFormats: Formats = genericKoutaFormats

  private def genericKoutaFormats: Formats = DefaultFormats ++
    Seq(
      localDateTimeSerializer,
      stringSerializer(UUID.fromString),
    )

  private def localDateTimeSerializer = new CustomSerializer[LocalDateTime](_ => ( {
    case JString(i) => LocalDateTime.from(ISO_LOCAL_DATE_TIME_FORMATTER.parse(i))
  }, {
    case i: LocalDateTime => JString(ISO_LOCAL_DATE_TIME_FORMATTER.format(i))
  }))

  private def stringSerializer[A: Manifest](construct: String => A) = new CustomSerializer[A](_ => ( {
    case JString(s) => construct(s)
  }, {
    case a: A => JString(a.toString)
  }))


}
