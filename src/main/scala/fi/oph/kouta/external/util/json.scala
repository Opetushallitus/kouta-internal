package fi.oph.kouta.external.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

import fi.oph.kouta.external.domain._
import fi.oph.kouta.external.domain.enums._
import fi.oph.kouta.external.domain.indexed._
import fi.oph.kouta.external.domain.oid._
import org.json4s.JsonAST.{JObject, JString}
import org.json4s._
import org.json4s.jackson.Serialization.write

import scala.util.Try

trait KoutaJsonFormats extends DefaultKoutaJsonFormats {

  implicit def jsonFormats: Formats = koutaJsonFormats

  def toJson(data: AnyRef): String = write(data)
}

sealed trait DefaultKoutaJsonFormats {

  val ISO_LOCAL_DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

  def koutaJsonFormats: Formats = genericKoutaFormats ++ Seq(
    koulutusMetadataSerializer,
    koulutusMetadataIndexedSerializer,
    //    toteutusMetadataSerializer,
    valintatapaSisaltoSerializer,
    valintaperusteMetadataSerializer,
    valintaperusteMetadataIndexedSerializer,
  )

  private def genericKoutaFormats: Formats = DefaultFormats
    .addKeySerializers(Seq(kieliKeySerializer)) ++
    Seq(
      localDateTimeSerializer,
      stringSerializer(Julkaisutila.apply),
      stringSerializer(Koulutustyyppi.apply),
      //      stringSerializer(Hakulomaketyyppi.apply),
      stringSerializer(Kieli.apply),
      stringSerializer(UUID.fromString),
      //      stringSerializer(LiitteenToimitustapa.apply),
      stringSerializer(HakuOid),
      stringSerializer(HakukohdeOid),
      stringSerializer(KoulutusOid),
      stringSerializer(ToteutusOid),
      stringSerializer(OrganisaatioOid),
      stringSerializer(UserOid),
      stringSerializer(GenericOid),
    )

  private def serializer[A: Manifest](deserializer: PartialFunction[JValue, A])(serializer: PartialFunction[Any, JValue]) =
    new CustomSerializer[A](_ => (deserializer, serializer))

  private def keySerializer[A: Manifest](deserializer: PartialFunction[String, A])(serializer: PartialFunction[Any, String]) =
    new CustomKeySerializer[A](_ => (deserializer, serializer))

  private def kieliKeySerializer = keySerializer {
    case s: String => Kieli(s)
  } {
    case k: Kieli => k.toString
  }

  private def localDateTimeSerializer: CustomSerializer[LocalDateTime] = serializer {
    case JString(i) => LocalDateTime.from(ISO_LOCAL_DATE_TIME_FORMATTER.parse(i))
  } {
    case i: LocalDateTime => JString(ISO_LOCAL_DATE_TIME_FORMATTER.format(i))
  }

  private def stringSerializer[A: Manifest](construct: String => A) = serializer {
    case JString(s) => construct(s)
  } {
    case a: A => JString(a.toString)
  }

  private def koulutusMetadataSerializer = serializer[KoulutusMetadata] {
    case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats

      Try(s \ "tyyppi").toOption.collect {
        case JString(tyyppi) => Koulutustyyppi(tyyppi)
      }.getOrElse(Koulutustyyppi.Amm) match {
        case Koulutustyyppi.Yo => s.extract[KorkeakoulutusKoulutusMetadata]
        case Koulutustyyppi.Amk => s.extract[KorkeakoulutusKoulutusMetadata]
        case Koulutustyyppi.Amm => s.extract[AmmatillinenKoulutusMetadata]
        case kt => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
      }
  } {
    case j: KoulutusMetadata =>
      implicit def formats: Formats = genericKoutaFormats

      Extraction.decompose(j)
  }

  private def koulutusMetadataIndexedSerializer = serializer[KoulutusMetadataIndexed] {
    case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats

      Try(s \ "tyyppi").toOption.collect {
        case JString(tyyppi) => Koulutustyyppi(tyyppi)
      }.getOrElse(Koulutustyyppi.Amm) match {
        case Koulutustyyppi.Yo => s.extract[KorkeakoulutusKoulutusMetadataIndexed]
        case Koulutustyyppi.Amk => s.extract[KorkeakoulutusKoulutusMetadataIndexed]
        case Koulutustyyppi.Amm => s.extract[AmmatillinenKoulutusMetadataIndexed]
        case kt => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
      }
  } {
    case j: KoulutusMetadataIndexed =>
      implicit def formats: Formats = genericKoutaFormats

      Extraction.decompose(j)
  }

  /*
  private def toteutusMetadataSerializer = new CustomSerializer[ToteutusMetadata](_ => ({
    case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats

      Try(s \ "tyyppi").toOption.collect {
        case JString(tyyppi) => Koulutustyyppi.withName(tyyppi)
      }.getOrElse(Amm) match {
        case Yo => s.extract[YliopistoToteutusMetadata]
        case Amm => s.extract[AmmatillinenToteutusMetadata]
        case Amk => s.extract[AmmattikorkeakouluToteutusMetadata]
        case kt => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
      }
  }, {
    case j: ToteutusMetadata =>
      implicit def formats: Formats = genericKoutaFormats

      Extraction.decompose(j)
  }))
*/

  private def valintaperusteMetadataSerializer = new CustomSerializer[ValintaperusteMetadata](_ => ( {
    case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats + valintatapaSisaltoSerializer

      Try(s \ "koulutustyyppi").toOption.collect {
        case JString(tyyppi) => Koulutustyyppi(tyyppi)
      }.getOrElse(Koulutustyyppi.Amm) match {
        case Koulutustyyppi.Yo => s.extract[YliopistoValintaperusteMetadata]
        case Koulutustyyppi.Amm => s.extract[AmmatillinenValintaperusteMetadata]
        case Koulutustyyppi.Amk => s.extract[AmmattikorkeakouluValintaperusteMetadata]
        case kt => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
      }
  }, {
    case j: ValintaperusteMetadata =>
      implicit def formats: Formats = genericKoutaFormats + valintatapaSisaltoSerializer

      Extraction.decompose(j)
  }))

  private def valintaperusteMetadataIndexedSerializer = new CustomSerializer[ValintaperusteMetadataIndexed](_ => ( {
    case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats + valintatapaSisaltoSerializer

      Try(s \ "koulutustyyppi").toOption.collect {
        case JString(tyyppi) => Koulutustyyppi(tyyppi)
      }.getOrElse(Koulutustyyppi.Amm) match {
        case Koulutustyyppi.Yo => s.extract[YliopistoValintaperusteMetadataIndexed]
        case Koulutustyyppi.Amm => s.extract[AmmatillinenValintaperusteMetadataIndexed]
        case Koulutustyyppi.Amk => s.extract[AmmattikorkeakouluValintaperusteMetadataIndexed]
        case kt => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
      }
  }, {
    case j: ValintaperusteMetadata =>
      implicit def formats: Formats = genericKoutaFormats + valintatapaSisaltoSerializer

      Extraction.decompose(j)
  }))

  private def valintatapaSisaltoSerializer = new CustomSerializer[ValintatapaSisalto](implicit formats => ( {
    case s: JObject =>
      Try(s \ "tyyppi").collect {
        case JString(tyyppi) if tyyppi == "teksti" =>
          Try(s \ "data").collect {
            case teksti: JObject => ValintatapaSisaltoTeksti(teksti.extract[Kielistetty])
          }.get
        case JString(tyyppi) if tyyppi == "taulukko" =>
          Try(s \ "data").collect {
            case taulukko: JObject => taulukko.extract[Taulukko]
          }.get
      }.get
  }, {
    case j: ValintatapaSisaltoTeksti =>
      implicit def formats: Formats = genericKoutaFormats

      JObject(List("tyyppi" -> JString("teksti"), "data" -> Extraction.decompose(j.teksti)))
    case j: Taulukko =>
      implicit def formats: Formats = genericKoutaFormats

      JObject(List("tyyppi" -> JString("taulukko"), "data" -> Extraction.decompose(j)))
  }))
}
