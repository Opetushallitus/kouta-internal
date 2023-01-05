package fi.oph.kouta.internal.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import fi.oph.kouta.domain.{
  AikuistenPerusopetus,
  Amk,
  Amm,
  AmmMuu,
  AmmOpeErityisopeJaOpo,
  AmmOsaamisala,
  AmmTutkinnonOsa,
  Erikoislaakari,
  Erikoistumiskoulutus,
  KkOpintojakso,
  KkOpintokokonaisuus,
  Koulutustyyppi,
  Lk,
  OpePedagOpinnot,
  Telma,
  Tuva,
  VapaaSivistystyoMuu,
  VapaaSivistystyoOpistovuosi,
  Yo
}
import fi.oph.kouta.internal.domain._
import fi.oph.kouta.internal.domain.enums.{Hakulomaketyyppi, Julkaisutila, Kieli, LiitteenToimitustapa}
import fi.oph.kouta.internal.domain.indexed._
import fi.oph.kouta.internal.domain.oid._
import org.json4s.JsonAST.{JObject, JString}
import org.json4s._
import org.json4s.jackson.Serialization.write

import scala.util.Try

trait KoutaJsonFormats extends DefaultKoutaJsonFormats {

  implicit val json4s: Serialization = org.json4s.jackson.Serialization
  implicit def jsonFormats: Formats  = koutaJsonFormats

  def toJson(data: AnyRef): String = write(data)
}

sealed trait DefaultKoutaJsonFormats {

  val ISO_LOCAL_DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm[:ss]")

  def koutaJsonFormats: Formats = genericKoutaFormats ++ Seq(
    koulutusMetadataSerializer,
    koulutusMetadataIndexedSerializer,
    toteutusMetadataSerializer,
    toteutusMetadataIndexedSerializer,
    sisaltoSerializer,
    valintaperusteMetadataSerializer,
    valintaperusteMetadataIndexedSerializer
  )

  private def genericKoutaFormats: Formats = DefaultFormats
    /*
    Ilman alla olevaa riviä, case classien optinaaliset tiedot jätetään vain pois jos sinne tuleva data on vääränmuotoista.
    Tarkoituksella näin tuotannossa ainakin toistaiseksi.
    Virheitä selviteltäessä rivin voi poistaa kommenteista jolloin näkee lokilta miksi jotain jää puuttumaan.
     */
    //.withStrictOptionParsing
    .addKeySerializers(Seq(kieliKeySerializer)) ++
    Seq(
      localDateTimeSerializer,
      stringSerializer(Julkaisutila.apply),
      stringSerializer(fi.oph.kouta.domain.Koulutustyyppi.withName),
      stringSerializer(Hakulomaketyyppi.apply),
      stringSerializer(Kieli.apply),
      stringSerializer(UUID.fromString),
      stringSerializer(LiitteenToimitustapa.apply),
      stringSerializer(HakuOid),
      stringSerializer(HakukohdeOid),
      stringSerializer(KoulutusOid),
      stringSerializer(ToteutusOid),
      stringSerializer(OrganisaatioOid),
      stringSerializer(UserOid),
      stringSerializer(GenericOid),
      stringSerializer(fi.oph.kouta.domain.Maksullisuustyyppi.withName),
      stringSerializer(fi.oph.kouta.domain.Alkamiskausityyppi.withName),
      stringSerializer(fi.oph.kouta.domain.Hakutermi.withName)
    )

  private def serializer[A: Manifest](deserializer: PartialFunction[JValue, A])(
      serializer: PartialFunction[Any, JValue]
  ) =
    new CustomSerializer[A](_ => (deserializer, serializer))

  private def keySerializer[A: Manifest](deserializer: PartialFunction[String, A])(
      serializer: PartialFunction[Any, String]
  ) =
    new CustomKeySerializer[A](_ => (deserializer, serializer))

  private def kieliKeySerializer: CustomKeySerializer[Kieli] = keySerializer { case s: String =>
    Kieli(s)
  } { case k: Kieli =>
    k.toString
  }

  private def localDateTimeSerializer: CustomSerializer[LocalDateTime] = serializer { case JString(i) =>
    LocalDateTime.from(ISO_LOCAL_DATE_TIME_FORMATTER.parse(i))
  } { case i: LocalDateTime =>
    JString(ISO_LOCAL_DATE_TIME_FORMATTER.format(i))
  }

  private def stringSerializer[A: Manifest](construct: String => A) = serializer { case JString(s) =>
    construct(s)
  } { case a: A =>
    JString(a.toString)
  }

  private def koulutusMetadataSerializer: CustomSerializer[KoulutusMetadata] = serializer[KoulutusMetadata] {
    case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats

      Try(s \ "tyyppi").toOption.collect { case JString(tyyppi) =>
        Koulutustyyppi.withName(tyyppi)
      }.getOrElse(Amm) match {
        case Yo                          => s.extract[YliopistoKoulutusMetadata]
        case Amm                         => s.extract[AmmatillinenKoulutusMetadata]
        case Amk                         => s.extract[AmmattikorkeakouluKoulutusMetadata]
        case KkOpintojakso               => s.extract[KkOpintojaksoKoulutusMetadata]
        case AmmOpeErityisopeJaOpo       => s.extract[AmmOpeErityisopeJaOpoKoulutusMetadata]
        case OpePedagOpinnot             => s.extract[OpePedagOpinnotKoulutusMetadata]
        case AmmTutkinnonOsa             => s.extract[AmmatillinenTutkinnonOsaKoulutusMetadata]
        case AmmOsaamisala               => s.extract[AmmatillinenOsaamisalaKoulutusMetadata]
        case AmmMuu                      => s.extract[AmmatillinenMuuKoulutusMetadata]
        case Lk                          => s.extract[LukioKoulutusMetadata]
        case Tuva                        => s.extract[TuvaKoulutusMetadata]
        case Telma                       => s.extract[TelmaKoulutusMetadata]
        case VapaaSivistystyoOpistovuosi => s.extract[VapaaSivistystyoKoulutusMetadata]
        case VapaaSivistystyoMuu         => s.extract[VapaaSivistystyoKoulutusMetadata]
        case AikuistenPerusopetus        => s.extract[AikuistenPerusopetusKoulutusMetadata]
        case Erikoislaakari              => s.extract[ErikoislaakariKoulutusMetadata]
        case KkOpintokokonaisuus         => s.extract[KkOpintokokonaisuusKoulutusMetadata]
        case Erikoistumiskoulutus        => s.extract[ErikoistumiskoulutusMetadata]
        case kt                          => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
      }
  } { case j: KoulutusMetadata =>
    implicit def formats: Formats = genericKoutaFormats

    Extraction.decompose(j)
  }

  private def koulutusMetadataIndexedSerializer: CustomSerializer[KoulutusMetadataIndexed] =
    serializer[KoulutusMetadataIndexed] { case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats

      Try(s \ "tyyppi").toOption.collect { case JString(tyyppi) =>
        Koulutustyyppi.withName(tyyppi)
      }.getOrElse(Amm) match {
        case Yo                          => s.extract[YliopistoKoulutusMetadataIndexed]
        case Amk                         => s.extract[AmmattikorkeakouluKoulutusMetadataIndexed]
        case KkOpintojakso               => s.extract[KkOpintojaksoKoulutusMetadataIndexed]
        case AmmOpeErityisopeJaOpo       => s.extract[AmmOpeErityisopeJaOpoKoulutusMetadataIndexed]
        case OpePedagOpinnot             => s.extract[OpePedagOpinnotKoulutusMetadataIndexed]
        case Amm                         => s.extract[AmmatillinenKoulutusMetadataIndexed]
        case AmmTutkinnonOsa             => s.extract[AmmatillinenTutkinnonOsaKoulutusMetadataIndexed]
        case AmmOsaamisala               => s.extract[AmmatillinenOsaamisalaKoulutusMetadataIndexed]
        case AmmMuu                      => s.extract[AmmatillinenMuuKoulutusMetadataIndexed]
        case Lk                          => s.extract[LukioKoulutusMetadataIndexed]
        case Tuva                        => s.extract[TuvaKoulutusMetadataIndexed]
        case Telma                       => s.extract[TelmaKoulutusMetadataIndexed]
        case VapaaSivistystyoOpistovuosi => s.extract[VapaaSivistystyoKoulutusMetadataIndexed]
        case VapaaSivistystyoMuu         => s.extract[VapaaSivistystyoKoulutusMetadataIndexed]
        case AikuistenPerusopetus        => s.extract[AikuistenPerusopetusKoulutusMetadataIndexed]
        case Erikoislaakari              => s.extract[ErikoislaakariKoulutusMetadataIndexed]
        case KkOpintokokonaisuus         => s.extract[KkOpintokokonaisuusKoulutusMetadataIndexed]
        case Erikoistumiskoulutus        => s.extract[ErikoislaakariKoulutusMetadataIndexed]
        case kt                          => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
      }
    } { case j: KoulutusMetadataIndexed =>
      implicit def formats: Formats = genericKoutaFormats

      Extraction.decompose(j)
    }

  private def toteutusMetadataSerializer: CustomSerializer[ToteutusMetadata] = serializer[ToteutusMetadata] {
    case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats

      Try(s \ "tyyppi").toOption.collect { case JString(tyyppi) =>
        Koulutustyyppi.withName(tyyppi)
      }.getOrElse(Amm) match {
        case Yo                          => s.extract[YliopistoToteutusMetadata]
        case Amm                         => s.extract[AmmatillinenToteutusMetadata]
        case Amk                         => s.extract[AmmattikorkeakouluToteutusMetadata]
        case KkOpintojakso               => s.extract[KkOpintojaksoToteutusMetadata]
        case AmmOpeErityisopeJaOpo       => s.extract[AmmOpeErityisopeJaOpoToteutusMetadata]
        case OpePedagOpinnot             => s.extract[OpePedagOpinnotToteutusMetadata]
        case AmmTutkinnonOsa             => s.extract[AmmatillinenTutkinnonOsaToteutusMetadata]
        case AmmOsaamisala               => s.extract[AmmatillinenOsaamisalaToteutusMetadata]
        case AmmMuu                      => s.extract[AmmatillinenMuuToteutusMetadata]
        case Lk                          => s.extract[LukioToteutusMetadata]
        case Tuva                        => s.extract[TuvaToteutusMetadata]
        case Telma                       => s.extract[TelmaToteutusMetadata]
        case VapaaSivistystyoOpistovuosi => s.extract[VapaaSivistystyoOpistovuosiToteutusMetadata]
        case VapaaSivistystyoMuu         => s.extract[VapaaSivistystyoMuuToteutusMetadata]
        case AikuistenPerusopetus        => s.extract[AikuistenPerusopetusToteutusMetadata]
        case Erikoislaakari              => s.extract[ErikoislaakariToteutusMetadata]
        case KkOpintokokonaisuus         => s.extract[KkOpintokokonaisuusToteutusMetadata]
        case Erikoistumiskoulutus        => s.extract[ErikoistumiskoulutusToteutusMetadata]
        case kt                          => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
      }
  } { case j: ToteutusMetadata =>
    implicit def formats: Formats = genericKoutaFormats

    Extraction.decompose(j)
  }

  private def toteutusMetadataIndexedSerializer: CustomSerializer[ToteutusMetadataIndexed] =
    serializer[ToteutusMetadataIndexed] { case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats

      Try(s \ "tyyppi").toOption.collect { case JString(tyyppi) =>
        Koulutustyyppi.withName(tyyppi)
      }.getOrElse(Amm) match {
        case Yo                          => s.extract[YliopistoToteutusMetadataIndexed]
        case Amk                         => s.extract[AmmattikorkeakouluToteutusMetadataIndexed]
        case KkOpintojakso               => s.extract[KkOpintojaksoToteutusMetadataIndexed]
        case AmmOpeErityisopeJaOpo       => s.extract[AmmOpeErityisopeJaOpoToteutusMetadataIndexed]
        case OpePedagOpinnot             => s.extract[OpePedagOpinnotToteutusMetadataIndexed]
        case Amm                         => s.extract[AmmatillinenToteutusMetadataIndexed]
        case AmmTutkinnonOsa             => s.extract[AmmatillinenTutkinnonOsaToteutusMetadataIndexed]
        case AmmOsaamisala               => s.extract[AmmatillinenOsaamisalaToteutusMetadataIndexed]
        case AmmMuu                      => s.extract[AmmatillinenMuuToteutusMetadataIndexed]
        case Lk                          => s.extract[LukioToteutusMetadataIndexed]
        case Tuva                        => s.extract[TuvaToteutusMetadataIndexed]
        case Telma                       => s.extract[TelmaToteutusMetadataIndexed]
        case VapaaSivistystyoOpistovuosi => s.extract[VapaaSivistystyoOpistovuosiToteutusMetadataIndexed]
        case VapaaSivistystyoMuu         => s.extract[VapaaSivistystyoMuuToteutusMetadataIndexed]
        case AikuistenPerusopetus        => s.extract[AikuistenPerusopetusToteutusMetadataIndexed]
        case Erikoislaakari              => s.extract[ErikoislaakariToteutusMetadataIndexed]
        case KkOpintokokonaisuus         => s.extract[KkOpintokokonaisuusToteutusMetadataIndexed]
        case Erikoistumiskoulutus        => s.extract[ErikoistumiskoulutusToteutusMetadataIndexed]
        case kt                          => throw new UnsupportedOperationException(s"Unsupported toteutustyyppi $kt")
      }
    } { case j: ToteutusMetadataIndexed =>
      implicit def formats: Formats = genericKoutaFormats

      Extraction.decompose(j)
    }

  private def valintaperusteMetadataSerializer: CustomSerializer[ValintaperusteMetadata] =
    serializer[ValintaperusteMetadata] { case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats + sisaltoSerializer

      Try(s \ "tyyppi").toOption.collect { case JString(tyyppi) =>
        Koulutustyyppi.withName(tyyppi)
      }.getOrElse(Amm) match {
        case kt if Koulutustyyppi.values contains kt => s.extract[GenericValintaperusteMetadata]
        case kt                                      => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
      }
    } { case j: ValintaperusteMetadata =>
      implicit def formats: Formats = genericKoutaFormats + sisaltoSerializer

      Extraction.decompose(j)
    }

  private def valintaperusteMetadataIndexedSerializer: CustomSerializer[ValintaperusteMetadataIndexed] =
    serializer[ValintaperusteMetadataIndexed] { case s: JObject =>
      implicit def formats: Formats = genericKoutaFormats + sisaltoSerializer

      Try(s \ "tyyppi").toOption.collect { case JString(tyyppi) =>
        Koulutustyyppi.withName(tyyppi)
      }.getOrElse(Amm) match {
        case kt if Koulutustyyppi.values contains kt => s.extract[GenericValintaperusteMetadataIndexed]
        case kt                                      => throw new UnsupportedOperationException(s"Unsupported koulutustyyppi $kt")
      }
    } { case j: ValintaperusteMetadata =>
      implicit def formats: Formats = genericKoutaFormats + sisaltoSerializer

      Extraction.decompose(j)
    }

  private def sisaltoSerializer = new CustomSerializer[Sisalto](implicit formats =>
    (
      { case s: JObject =>
        Try(s \ "tyyppi").collect {
          case JString(tyyppi) if tyyppi == "teksti" =>
            Try(s \ "data").collect { case teksti: JObject =>
              SisaltoTeksti(teksti.extract[Kielistetty])
            }.get
          case JString(tyyppi) if tyyppi == "taulukko" =>
            Try(s \ "data").collect { case taulukko: JObject =>
              taulukko.extract[Taulukko]
            }.get
        }.get
      },
      {
        case j: SisaltoTeksti =>
          implicit def formats: Formats = genericKoutaFormats

          JObject(List("tyyppi" -> JString("teksti"), "data" -> Extraction.decompose(j.teksti)))
        case j: Taulukko =>
          implicit def formats: Formats = genericKoutaFormats

          JObject(List("tyyppi" -> JString("taulukko"), "data" -> Extraction.decompose(j)))
      }
    )
  )
}
