package fi.oph.kouta.internal.domain.indexed

import com.fasterxml.jackson.annotation.{JsonCreator, JsonProperty}
import fi.oph.kouta.domain.{Alkamiskausityyppi, Modified}
import fi.oph.kouta.internal.domain.oid.OrganisaatioOid

import scala.util.Try
//import fi.oph.kouta.domain.oid.OrganisaatioOid

import java.time.{LocalDateTime, ZoneOffset}
import java.util.UUID
import fi.oph.kouta.internal.domain.enums.{Hakulomaketyyppi, Julkaisutila, Kieli}
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid, UserOid}
import fi.oph.kouta.internal.domain._
import fi.vm.sade.utils.slf4j.Logging
import org.joda.time.LocalDate

import java.time.temporal.TemporalField

case class EmbeddedToteutusIndexed(tarjoajat: List[Organisaatio])

case class EmbeddedHakukohdeIndexed(
    oid: HakukohdeOid,
    jarjestyspaikka: Option[Organisaatio],
    toteutus: EmbeddedToteutusIndexed,
    tila: Julkaisutila
)
case class EmbeddedHakukohdeIndexedES @JsonCreator() (
    @JsonProperty("oid") oid: String,
    @JsonProperty("jarjestyspaikka") jarjestyspaikka: Option[OrganisaatioES],
    @JsonProperty("toteutus") toteutus: EmbeddedToteutusIndexedES,
    @JsonProperty("tila") tila: String
)
case class OrganisaatioES @JsonCreator() (
    @JsonProperty("oid") oid: String,
    @JsonProperty("nimi") nimi: Map[String, String] = Map()
)
case class EmbeddedToteutusIndexedES @JsonCreator() (
    @JsonProperty("tarjoajat") tarjoajat: List[OrganisaatioES] = List()
)
case class HakuTapaES @JsonCreator() (
    @JsonProperty("koodiUri") koodiUri: String,
    @JsonProperty("nimi") nimi: Map[String, String] = Map()
)
case class KohdejoukkoES @JsonCreator() (
    @JsonProperty("koodiUri") koodiUri: String,
    @JsonProperty("nimi") nimi: Map[String, String] = Map()
)
case class KohdejoukonTarkenneES @JsonCreator() (
    @JsonProperty("koodiUri") koodiUri: String,
    @JsonProperty("nimi") nimi: Map[String, String] = Map()
)
case class HakuMetadataES @JsonCreator() (
    @JsonProperty("yhteyshenkilot") yhteyshenkilot: Seq[YhteyshenkiloES] = Seq(),
    @JsonProperty("tulevaisuudenAikataulu") tulevaisuudenAikataulu: List[AikaJakso] = List(),
    @JsonProperty("koulutuksenAlkamiskausi") koulutuksenAlkamiskausi: Option[KoulutuksenAlkamiskausiHakukohdeES]
)

case class YhteyshenkiloES @JsonCreator() (
    @JsonProperty("nimi") nimi: Map[String, String] = Map(),
    @JsonProperty("puhelinnumero") puhelinnumero: Map[String, String] = Map(),
    @JsonProperty("sahkoposti") sahkoposti: Map[String, String] = Map(),
    @JsonProperty("titteli") titteli: Map[String, String] = Map(),
    @JsonProperty("wwwSivu") wwwSivu: Map[String, String] = Map(),
    @JsonProperty("wwwSivuTeksti") wwwSivuTeksti: Map[String, String] = Map()
)

case class HakuJavaClient @JsonCreator() (
    @JsonProperty("oid") oid: Option[String],
    @JsonProperty("externalId") externalId: Option[String],
    @JsonProperty("tila") tila: String,
    @JsonProperty("nimi") nimi: Map[String, String] = Map(),
    @JsonProperty("hakukohteet") hakukohteet: List[EmbeddedHakukohdeIndexedES] = List(),
    @JsonProperty("hakutapa") hakutapa: Option[HakuTapaES],
    @JsonProperty("hakukohteenLiittamisenTakaraja") hakukohteenLiittamisenTakaraja: Option[String],
    @JsonProperty("hakukohteenMuokkaamisenTakaraja") hakukohteenMuokkaamisenTakaraja: Option[String],
    @JsonProperty("ajastettuJulkaisu") ajastettuJulkaisu: Option[String],
    @JsonProperty("kohdejoukko") kohdejoukko: Option[KohdejoukkoES],
    @JsonProperty("kohdejoukonTarkenne") kohdejoukonTarkenne: Option[KohdejoukonTarkenneES],
    @JsonProperty("hakulomaketyyppi") hakulomaketyyppi: Option[String],
    @JsonProperty("hakulomakeAtaruId") hakulomakeAtaruId: Option[String],
    @JsonProperty("hakulomakeKuvaus") hakulomakeKuvaus: Map[String, String] = Map(),
    @JsonProperty("hakulomakeLinkki") hakulomakeLinkki: Map[String, String] = Map(),
    @JsonProperty("metadata") metadata: Option[HakuMetadataES],
    @JsonProperty("organisaatio") organisaatio: OrganisaatioES,
    @JsonProperty("hakuajat") hakuajat: List[AikaJakso] = List(),
    @JsonProperty("valintakokeet") valintakokeet: List[ValintakoeES] = List(),
    @JsonProperty("muokkaaja") muokkaaja: MuokkaajaES,
    @JsonProperty("kielivalinta") kielivalinta: Seq[String] = Seq(),
    @JsonProperty("modified") modified: Option[String]
) {
  def toResult(): HakuIndexed = {
    HakuIndexed(
      oid = oid.map(HakuOid).get,
      tila = Julkaisutila(tila),
      nimi = toKielistettyMap(nimi),
      hakukohteet = createHakukohteet(hakukohteet),
      hakutapa = hakutapa.map(h => KoodiUri(h.koodiUri)),
      hakukohteenLiittamisenTakaraja = hakukohteenLiittamisenTakaraja.map(parseLocalDateTime),
      hakukohteenMuokkaamisenTakaraja = hakukohteenMuokkaamisenTakaraja.map(parseLocalDateTime),
      ajastettuJulkaisu = ajastettuJulkaisu.map(parseLocalDateTime),
      kohdejoukko = kohdejoukko.map(kj => KoodiUri(kj.koodiUri)).getOrElse(null),
      kohdejoukonTarkenne = kohdejoukonTarkenne.map(kjt => KoodiUri(kjt.koodiUri)),
      hakulomaketyyppi = Some(Hakulomaketyyppi(hakulomaketyyppi.get)),
      hakulomakeAtaruId = hakulomakeAtaruId.map(UUID.fromString),
      hakulomakeKuvaus = toKielistettyMap(hakulomakeKuvaus),
      hakulomakeLinkki = toKielistettyMap(hakulomakeLinkki),
      metadata = createHakuMetadataIndexed(metadata), //TODO
      organisaatio = Some(Organisaatio(oid = OrganisaatioOid(organisaatio.oid), toKielistettyMap(organisaatio.nimi))),
      hakuajat = hakuajat.map(hakuaika => {
        Ajanjakso(
          alkaa = parseLocalDateTime(hakuaika.alkaa),
          paattyy = if (hakuaika.paattyy != null) Option.apply(parseLocalDateTime(hakuaika.paattyy)) else None
        )
      }),
      valintakokeet = getValintakokeet(valintakokeet),
      muokkaaja = Muokkaaja(UserOid(muokkaaja.oid)),
      kielivalinta = kielivalinta.map(kieli => Kieli(kieli)),
      modified = modified.map(m => LocalDateTime.parse(m)),
      externalId = externalId

      //hakukohteenLiittajaOrganisaatiot = hakukohteenLiittajaOrganisaatiot, //TODO? ei löydy
    )
  }

  def getValintakokeet(valintakoeList: List[ValintakoeES]): List[ValintakoeIndexed] = {

    valintakoeList.map(koe => {
      ValintakoeIndexed(
        id = Try(UUID.fromString(koe.id)).toOption,
        tyyppi = koe.tyyppi.map(tyyppi => KoodiUri(tyyppi.koodiUri)),
        tilaisuudet = Some(koe.tilaisuudet.map(tilaisuus => {
          Valintakoetilaisuus(
            osoite = Some(
              Osoite(
                osoite = tilaisuus.osoite.map(osoite => toKielistettyMap(osoite.osoite)),
                postinumero = Some(tilaisuus.osoite.get.postinumeroKoodiUri),
                postitoimipaikka = Some(toKielistettyMap(tilaisuus.osoite.get.postitoimipaikka))
              )
            ),
            aika = tilaisuus.aika.map(aika =>
              Ajanjakso(
                alkaa = parseLocalDateTime(aika.alkaa),
                paattyy = if (aika.paattyy != null) Option.apply(parseLocalDateTime(aika.paattyy)) else None
              )
            ),
            lisatietoja = toKielistettyMap(tilaisuus.lisatietoja)
          )
        })),
        vahimmaispisteet = koe.metadata.map(meta => meta.vahimmaispisteet).getOrElse(None),
        metadata = koe.metadata.map(metadata =>
          ValintakoeMetadataIndexed(
            vahimmaispisteet = metadata.vahimmaispisteet
          )
        )
      )
    })

  }

  def createHakuMetadataIndexed(metadataESOption: Option[HakuMetadataES]): Option[HakuMetadata] = {
    metadataESOption.map(metadataES =>
      HakuMetadata(
        yhteyshenkilot = metadataES.yhteyshenkilot.map(m =>
          Yhteyshenkilo(
            nimi = toKielistettyMap(m.nimi),
            titteli = toKielistettyMap(m.titteli),
            sahkoposti = toKielistettyMap(m.sahkoposti),
            puhelinnumero = toKielistettyMap(m.puhelinnumero),
            wwwSivu = toKielistettyMap(m.wwwSivu)
          )
        ),
        tulevaisuudenAikataulu = metadataES.tulevaisuudenAikataulu.map(m =>
          Ajanjakso(
            alkaa = parseLocalDateTime(m.alkaa),
            paattyy = if (m.paattyy != null) Option.apply(parseLocalDateTime(m.paattyy)) else None
          )
        ),
        koulutuksenAlkamiskausi = metadataES.koulutuksenAlkamiskausi.map(koulutuksenAlkamiskausi =>
          KoulutuksenAlkamiskausi(
            alkamiskausityyppi = Alkamiskausityyppi.withName(koulutuksenAlkamiskausi.alkamiskausityyppi),
            koulutuksenAlkamiskausi = Option.apply(KoodiUri(koulutuksenAlkamiskausi.koulutuksenAlkamiskausi.koodiUri)),
            koulutuksenAlkamisvuosi = Option.apply(koulutuksenAlkamiskausi.koulutuksenAlkamisvuosi)
          )
        )
      )
    )
  }
  def createHakukohteet(hakukohteet: List[EmbeddedHakukohdeIndexedES]): List[EmbeddedHakukohdeIndexed] = {
    List.empty

    hakukohteet.map(m =>
      EmbeddedHakukohdeIndexed(
        oid = HakukohdeOid(m.oid),
        jarjestyspaikka = m.jarjestyspaikka.map(j => Organisaatio(OrganisaatioOid(j.oid), toKielistettyMap(j.nimi))),
        toteutus = EmbeddedToteutusIndexed(
          m.toteutus.tarjoajat.map(t => Organisaatio(OrganisaatioOid(t.oid), toKielistettyMap(t.nimi)))
        ),
        tila = Julkaisutila(m.tila)
      )
    )
  }

  def parseLocalDateTime(dateString: String): LocalDateTime = {
    LocalDateTime.parse(dateString)
  }

  def toKielistettyMap(map: Map[String, String]): Kielistetty = {
    Map(
      Kieli.En -> map.get("en"),
      Kieli.Fi -> map.get("fi"),
      Kieli.Sv -> map.get("sv")
    ).collect { case (k, Some(v)) => (k, v) }
  }
}

case class HakuIndexed(
    oid: HakuOid,
    tila: Julkaisutila,
    nimi: Kielistetty,
    hakukohteet: List[EmbeddedHakukohdeIndexed],
    hakutapa: Option[KoodiUri],
    hakukohteenLiittamisenTakaraja: Option[LocalDateTime],
    hakukohteenMuokkaamisenTakaraja: Option[LocalDateTime],
    ajastettuJulkaisu: Option[LocalDateTime],
    kohdejoukko: KoodiUri,
    kohdejoukonTarkenne: Option[KoodiUri],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeAtaruId: Option[UUID],
    hakulomakeKuvaus: Kielistetty,
    hakulomakeLinkki: Kielistetty,
    metadata: Option[HakuMetadata],
    organisaatio: Option[Organisaatio],
    hakuajat: List[Ajanjakso],
    valintakokeet: List[ValintakoeIndexed],
    muokkaaja: Muokkaaja,
    kielivalinta: Seq[Kieli],
    modified: Option[LocalDateTime],
    externalId: Option[String]
) extends WithTila
    with Logging {
  def toHaku(includeHakukohdeOids: Boolean = false): Haku = {
    def getHakukausiUri(ajanjakso: Ajanjakso): String = {
      ajanjakso.paattyy.map(_.getMonthValue).getOrElse {
        ajanjakso.alkaa.getMonthValue
      }
    } match {
      case m if m >= 1 && m <= 7  => "kausi_k#1"
      case m if m >= 8 && m <= 12 => "kausi_s#1"
      case _                      => ""
    }

    try {
      Haku(
        oid = oid,
        hakukohdeOids = if (includeHakukohdeOids) Some(hakukohteet.map(_.oid)) else None,
        tila = tila,
        nimi = nimi,
        hakutapaKoodiUri = hakutapa.map(_.koodiUri),
        hakukohteenLiittamisenTakaraja = hakukohteenLiittamisenTakaraja,
        hakukohteenMuokkaamisenTakaraja = hakukohteenMuokkaamisenTakaraja,
        ajastettuJulkaisu = ajastettuJulkaisu,
        alkamiskausiKoodiUri = metadata.flatMap(m =>
          m.koulutuksenAlkamiskausi
            .flatMap(_.koulutuksenAlkamiskausi.map(_.koodiUri))
        ),
        hakuvuosi = hakuajat
          .sortBy(ha => ha.alkaa)
          .headOption
          .map(ha => ha.paattyy.map(_.getYear).getOrElse(ha.alkaa.getYear)),
        hakukausi = hakuajat
          .sortBy(ha => ha.alkaa)
          .headOption
          .map(getHakukausiUri),
        alkamisvuosi = metadata.flatMap(m => m.koulutuksenAlkamiskausi.flatMap(_.koulutuksenAlkamisvuosi)),
        kohdejoukkoKoodiUri = kohdejoukko.koodiUri,
        kohdejoukonTarkenneKoodiUri = kohdejoukonTarkenne.map(_.koodiUri),
        hakulomaketyyppi = hakulomaketyyppi,
        hakulomakeAtaruId = hakulomakeAtaruId,
        hakulomakeKuvaus = hakulomakeKuvaus,
        hakulomakeLinkki = hakulomakeLinkki,
        metadata = metadata,
        organisaatioOid = organisaatio.get.oid,
        hakuajat = hakuajat,
        valintakokeet = valintakokeet.map(_.toValintakoe),
        muokkaaja = muokkaaja.oid,
        kielivalinta = kielivalinta,
        modified = modified,
        externalId = externalId
      )
    } catch {
      case e: Exception => {
        val msg: String = s"Failed to create Haku (${oid})"
        logger.error(msg, e)
        throw new RuntimeException(msg, e)
      }
    }
  }
  implicit val localDateTimeOrdering: Ordering[LocalDateTime] = Ordering.by(_.toEpochSecond(ZoneOffset.UTC))
  def toOdwHaku: OdwHaku = {
    def hakukausi: Option[String] = {
      hakuajat
        .sortBy(ha => ha.alkaa)
        .headOption
        .map(ha =>
          ha.paattyy
            .map(_.getMonthValue)
            .getOrElse(ha.alkaa.getMonthValue) match {
            case m if m >= 1 && m <= 7  => "kausi_k#1"
            case m if m >= 8 && m <= 12 => "kausi_s#1"
            case _                      => ""
          }
        )
    }
    def hakuvuosi: Option[String] = {
      hakuajat
        .sortBy(ha => ha.alkaa)
        .headOption
        .map(ha =>
          ha.paattyy
            .map(_.getYear.toString)
            .getOrElse(ha.alkaa.getYear.toString)
        )

    }
    try {
      OdwHaku(
        oid = oid,
        tila = tila,
        nimi = nimi,
        hakutapaKoodiUri = hakutapa.map(_.koodiUri),
        hakukohteenLiittamisenTakaraja = hakukohteenLiittamisenTakaraja,
        hakukohteenMuokkaamisenTakaraja = hakukohteenMuokkaamisenTakaraja,
        ajastettuJulkaisu = ajastettuJulkaisu,
        alkamiskausiKoodiUri = metadata.flatMap(m =>
          m.koulutuksenAlkamiskausi
            .flatMap(_.koulutuksenAlkamiskausi.map(_.koodiUri))
        ),
        alkamisvuosi = metadata.flatMap(m => m.koulutuksenAlkamiskausi.flatMap(_.koulutuksenAlkamisvuosi)),
        kohdejoukkoKoodiUri = kohdejoukko.koodiUri,
        kohdejoukonTarkenneKoodiUri = kohdejoukonTarkenne.map(_.koodiUri),
        hakulomaketyyppi = hakulomaketyyppi,
        hakulomakeAtaruId = hakulomakeAtaruId,
        hakulomakeKuvaus = hakulomakeKuvaus,
        hakulomakeLinkki = hakulomakeLinkki,
        metadata = metadata,
        organisaatioOid = organisaatio.get.oid,
        hakuajat = hakuajat,
        valintakokeet = valintakokeet.map(_.toValintakoe),
        muokkaaja = muokkaaja.oid,
        kielivalinta = kielivalinta,
        modified = modified,
        externalId = externalId,
        hakuvuosi = hakuvuosi,
        hakukausi = hakukausi
      )
    } catch {
      case e: Exception => {
        val msg: String = s"Failed to create Haku (${oid})"
        logger.error(msg, e)
        throw new RuntimeException(msg, e)
      }
    }
  }
}

//case class HakuMetadataIndexed(
//    yhteyshenkilot: Seq[Yhteyshenkilo],
//    tulevaisuudenAikataulu: Seq[Ajanjakso],
//    koulutuksenAlkamiskausi: Option[KoulutuksenAlkamiskausiIndexed]) {
//  def toHakuMetadata: HakuMetadata = HakuMetadata(
//    yhteyshenkilot = yhteyshenkilot,
//    tulevaisuudenAikataulu = tulevaisuudenAikataulu,
//    koulutuksenAlkamiskausi = koulutuksenAlkamiskausi.map(_.toKoulutuksenAlkamiskausi)
//  )
//}
