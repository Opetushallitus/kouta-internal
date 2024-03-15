package fi.oph.kouta.internal.domain.indexed

import com.fasterxml.jackson.annotation.{JsonCreator, JsonProperty}
import fi.oph.kouta.domain.{Alkamiskausityyppi, Koulutustyyppi}
import fi.oph.kouta.internal.domain.LiitteenPostinumero

import fi.oph.kouta.internal.domain.enums.{Hakulomaketyyppi, Julkaisutila, Kieli, LiitteenToimitustapa}

import java.time.LocalDateTime
import java.util.UUID
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid, ToteutusOid, UserOid}
import fi.oph.kouta.internal.domain._

import fi.vm.sade.utils.slf4j.Logging
import scala.util.Try

case class YhdenPaikanSaantoES @JsonCreator() (
    @JsonProperty("voimassa") voimassa: Boolean,
    @JsonProperty("syy") syy: String
)
case class LiitteenToimitusosoiteES @JsonCreator() (
    @JsonProperty("osoite") osoite: OsoiteHakukohdeES,
    @JsonProperty("sahkoposti") sahkoposti: Option[String],
    @JsonProperty("verkkosivu") verkkosivu: Option[String]
)

case class OsoiteHakukohdeES @JsonCreator() (
    @JsonProperty("osoite") osoite: Map[String, String],
    @JsonProperty("postinumero") postinumero: LiitteenPostinumeroES
)

case class LiitteenPostinumeroES @JsonCreator() (
    @JsonProperty("postinumeroKoodiUri") postinumeroKoodiUri: String,
    @JsonProperty("nimi") nimi: Map[String, String]
)

case class LiiteES @JsonCreator() (
    @JsonProperty("id") id: String,
    @JsonProperty("tyyppi") tyyppi: LiiteTyyppiES,
    @JsonProperty("nimi") nimi: Map[String, String],
    @JsonProperty("kuvaus") kuvaus: Map[String, String],
    @JsonProperty("toimitusaika") toimitusaika: String,
    @JsonProperty("toimitustapa") toimitustapa: String,
    @JsonProperty("toimitusosoite") toimitusosoite: LiitteenToimitusosoiteES
)
case class LiiteTyyppiES @JsonCreator() (
    @JsonProperty("koodiUri") koodiUri: String,
    @JsonProperty("nimi") nimi: Map[String, String]
)
case class AloituspaikatES @JsonCreator() (
    @JsonProperty("kuvaus") kuvaus: Map[String, String],
    @JsonProperty("lukumaara") lukumaara: Int,
    @JsonProperty("ensikertalaisille") ensikertalaisille: Int
)

case class ValintakoeLisatilaisuusIndexedES @JsonCreator() (
    @JsonProperty("id") id: String,
    @JsonProperty("tilaisuudet") tilaisuudet: Seq[ValintakoetilaisuusES]
)
case class ValintakoetilaisuusES @JsonCreator() (
    @JsonProperty("aika") aika: AikaJakso,
    @JsonProperty("jarjestamispaikka") jarjestamispaikka: Map[String, String],
    @JsonProperty("lisatietoja") lisatietoja: Map[String, String],
    @JsonProperty("osoite") osoite: OsoiteES
)
case class HakukohdeMetadataES @JsonCreator() (
    @JsonProperty("kaytetaanHaunAlkamiskautta") kaytetaanHaunAlkamiskautta: Boolean,
    @JsonProperty("aloituspaikat") aloituspaikat: AloituspaikatES,
    @JsonProperty("uudenOpiskelijanUrl") uudenOpiskelijanUrl: Map[String, String],
    @JsonProperty("koulutuksenAlkamiskausi") koulutuksenAlkamiskausi: KoulutuksenAlkamiskausiHakukohdeES,
    @JsonProperty("hakukohteenLinja") hakukohteenLinja: HakukohteenLinjaES)

case class HakukohteenLinjaES @JsonCreator() (
    @JsonProperty("alinHyvaksyttyKeskiarvo") alinHyvaksyttyKeskiarvo: String,
    @JsonProperty("painotetutArvosanat") painotetutArvosanat: List[PainotettuArvosanaES],
    @JsonProperty("painotetutArvosanatOppiaineittain") painotetutArvosanatOppiaineittain: List[PainotettuArvosanaES],
    @JsonProperty("linja") linja: KoodiES)

case class PainotettuArvosanaES(
    @JsonProperty("painokerroin") painokerroin: Option[Double],
    @JsonProperty("koodit") koodit: PainottevaOppiaineKoodiES
)

case class PainottevaOppiaineKoodiES(@JsonProperty("oppiaine") oppiaine: OppiaineES)

case class OppiaineES(@JsonProperty("koodiUri") koodiUri: String)

case class KoodiES @JsonCreator() (
    @JsonProperty("koodiUri") koodiUri: String,
    @JsonProperty("nimi") nimi: Map[String, String]
)

case class PaateltyAlkamiskausiES @JsonCreator() (
    @JsonProperty("alkamiskausityyppi") alkamiskausityyppi: String,
    @JsonProperty("kausiUri") kausiUri: String,
    @JsonProperty("source") source: String,
    @JsonProperty("vuosi") vuosi: String
)
case class OdwKkTasotES @JsonCreator() (
    @JsonProperty("alempiKkAste") alempiKkAste: Boolean,
    @JsonProperty("ylempiKkAste") ylempiKkAste: Boolean,
    @JsonProperty("kkTutkinnonTaso") kkTutkinnonTaso: Int,
    @JsonProperty("kkTutkinnonTasoSykli") kkTutkinnonTasoSykli: Int)

case class SoraES @JsonCreator() (@JsonProperty("tila") tila: String)
case class ToteutusES @JsonCreator() (
    @JsonProperty("oid") oid: String,
    @JsonProperty("tarjoajat") tarjoajat: List[TarjoajaES]
)

case class TarjoajaES @JsonCreator() (
    @JsonProperty("oid") oid: String
)

case class ValintaperusteES @JsonCreator() (
    @JsonProperty("id") id: String,
    @JsonProperty("tila") tila: String,
    @JsonProperty("koulutustyyppi") koulutustyyppi: String,
    @JsonProperty("hakutapa") hakutapa: String,
    @JsonProperty("kohdejoukko") kohdejoukko: String,
    @JsonProperty("kohdejoukonTarkenne") kohdejoukonTarkenne: String,
    @JsonProperty("nimi") nimi: Map[String, String],
    @JsonProperty("julkinen") julkinen: Boolean,
    @JsonProperty("metadata") metadata: Object,
    @JsonProperty("organisaatio") organisaatio: OrganisaatioES,
    @JsonProperty("muokkaaja") muokkaaja: MuokkaajaES,
    @JsonProperty("kielivalinta") kielivalinta: Seq[String],
    @JsonProperty("modified") modified: Option[String],
    @JsonProperty("oid") externalId: String,
    @JsonProperty("valintakokeet") valintakokeet: List[ValintakoeES] )

case class HakukohdeJavaClient @JsonCreator() (
    @JsonProperty("oid") oid: String,
    @JsonProperty("toteutus") toteutus: ToteutusES,
    @JsonProperty("hakuOid") hakuOid: String,
    @JsonProperty("tila") tila: String,
    @JsonProperty("nimi") nimi: Map[String, String],
    @JsonProperty("hakulomaketyyppi") hakulomaketyyppi: String,
    @JsonProperty("hakulomakeAtaruId") hakulomakeAtaruId: String,
    @JsonProperty("hakulomakeKuvaus") hakulomakeKuvaus: Map[String, String],
    @JsonProperty("hakulomakeLinkki") hakulomakeLinkki: Map[String, String],
    @JsonProperty("kaytetaanHaunHakulomaketta") kaytetaanHaunHakulomaketta: Boolean,
    @JsonProperty("pohjakoulutusvaatimus") pohjakoulutusvaatimus: List[Map[String, Object]],
    @JsonProperty("muuPohjakoulutusvaatimus") muuPohjakoulutusvaatimus: Map[String,String],
    @JsonProperty("toinenAsteOnkoKaksoistutkinto") toinenAsteOnkoKaksoistutkinto: Boolean,
    @JsonProperty("kaytetaanHaunAikataulua") kaytetaanHaunAikataulua: Boolean,
    @JsonProperty("valintaperuste") valintaperuste: ValintaperusteES,
    @JsonProperty("yhdenPaikanSaanto") yhdenPaikanSaanto: YhdenPaikanSaantoES,
    @JsonProperty("koulutustyyppikoodi") koulutustyyppikoodi: String,
    @JsonProperty("salliikoHakukohdeHarkinnanvaraisuudenKysymisen") salliikoHakukohdeHarkinnanvaraisuudenKysymisen: Boolean,
    @JsonProperty("voikoHakukohteessaOllaHarkinnanvaraisestiHakeneita") voikoHakukohteessaOllaHarkinnanvaraisestiHakeneita: Boolean,
    @JsonProperty("liitteetOnkoSamaToimitusaika") liitteetOnkoSamaToimitusaika: Boolean,
    @JsonProperty("liitteetOnkoSamaToimitusosoite") liitteetOnkoSamaToimitusosoite: Boolean,
    @JsonProperty("liitteidenToimitusaika") liitteidenToimitusaika: String,
    @JsonProperty("liitteidenToimitustapa") liitteidenToimitustapa: String,
    @JsonProperty("liitteidenToimitusosoite") liitteidenToimitusosoiteES: LiitteenToimitusosoiteES,
    @JsonProperty("liitteet") liitteet: List[LiiteES],
    @JsonProperty("sora") sora: SoraES,
    @JsonProperty("valintakokeet") valintakokeet: List[ValintakoeES],
    @JsonProperty("hakuajat") hakuajat: List[AikaJakso],
    @JsonProperty("muokkaaja") muokkaaja: MuokkaajaES,
    @JsonProperty("jarjestyspaikka") jarjestyspaikka: OrganisaatioES,
    @JsonProperty("organisaatio") organisaatio: OrganisaatioES,
    @JsonProperty("kielivalinta") kielivalinta: Seq[String],
    @JsonProperty("modified") modified: String,
    @JsonProperty("metadata") metadata: HakukohdeMetadataES,
    @JsonProperty("jarjestaaUrheilijanAmmKoulutusta") jarjestaaUrheilijanAmmKoulutusta: Boolean,
    @JsonProperty("externalId") externalId: String,
    @JsonProperty("hakukohde") hakukohde: String,
    @JsonProperty("paateltyAlkamiskausi") paateltyAlkamiskausi: PaateltyAlkamiskausiES,
    @JsonProperty("odwKkTasot") odwKkTasot: OdwKkTasotES) {

  def parseLocalDateTime(dateString: String): LocalDateTime = {
    LocalDateTime.parse(dateString)
  }
  def getValintakokeet(valintakoeList: List[ValintakoeES]): List[ValintakoeIndexed] = {

    valintakoeList.map(koe => {
      ValintakoeIndexed(
        id = Try(UUID.fromString(koe.id)).toOption,
        tyyppi = koe.tyyppi.map(tyyppi => KoodiUri(tyyppi.koodiUri)),
        tilaisuudet = Some(koe.tilaisuudet.map(tilaisuus => {
          fi.oph.kouta.internal.domain.Valintakoetilaisuus(
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
                paattyy = Option.apply(
                  parseLocalDateTime(aika.paattyy)
                )
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

  def createValintaperuste(valintaperuste: ValintaperusteES): Option[ValintaperusteIndexed] = {
    Option.apply(
      ValintaperusteIndexed(
        id = Option.apply(valintaperuste.id).map(id => UUID.fromString(id)),
        tila = Julkaisutila(valintaperuste.tila),
        koulutustyyppi = Koulutustyyppi.withName(valintaperuste.koulutustyyppi),
        hakutapa = Some(KoodiUri(valintaperuste.hakutapa)),
        kohdejoukko = Some(KoodiUri(valintaperuste.kohdejoukko)),
        kohdejoukonTarkenne = Some(KoodiUri(valintaperuste.kohdejoukonTarkenne)),
        nimi = toKielistettyMap(valintaperuste.nimi),
        julkinen = valintaperuste.julkinen,
        metadata = None, // TODO
        organisaatio =
          if (valintaperuste.organisaatio != null)
            Option.apply(
              Organisaatio(
                OrganisaatioOid(valintaperuste.organisaatio.oid),
                toKielistettyMap(valintaperuste.organisaatio.nimi)
              )
            )
          else None,
        muokkaaja = if (valintaperuste.muokkaaja != null) Muokkaaja(UserOid(valintaperuste.muokkaaja.oid)) else null,
        kielivalinta = valintaperuste.kielivalinta.map(kieli => Kieli(kieli)),
        modified = valintaperuste.modified.map(m => LocalDateTime.parse(m)),
        externalId = Option.apply(valintaperuste.externalId),
        valintakokeet = getValintakokeet(valintaperuste.valintakokeet)
      )
    )
    None
  }

  def getOsoite(liitteenToimitusosoite: LiitteenToimitusosoiteES): Option[LiitteenToimitusosoite] = {
    if (liitteenToimitusosoite != null) {
      Some(
        LiitteenToimitusosoite(
          osoite = Some(
            LiitteenOsoite(
              osoite = Some(toKielistettyMap(liitteenToimitusosoite.osoite.osoite)),
              postinumero = Some(
                LiitteenPostinumero(
                  koodiUri = Some(liitteenToimitusosoite.osoite.postinumero.postinumeroKoodiUri),
                  nimi = Some(toKielistettyMap(liitteenToimitusosoite.osoite.postinumero.nimi))
                )
              )
            )
          ),
          sahkoposti = liitteenToimitusosoite.sahkoposti,
          verkkosivu = liitteenToimitusosoite.verkkosivu
        )
      )

    }
    None
  }

  def getLiitteet(liitteet: List[LiiteES]): List[Liite] = {
    if (liitteet != null) {
      liitteet
        .map(l => {
          Liite(
            id = Option.apply(UUID.fromString(l.id)),
            tyyppi =
              if (l.tyyppi != null)
                Option.apply(
                  LiitteenTyyppi(Option.apply(l.tyyppi.koodiUri), Option.apply(toKielistettyMap(l.tyyppi.nimi)))
                )
              else None,
            nimi = toKielistettyMap(l.nimi),
            kuvaus = Option.apply(toKielistettyMap(l.kuvaus)),
            toimitusaika = Option.apply(parseLocalDateTime(l.toimitusaika)),
            toimitustapa = if (l.toimitustapa != null) Option.apply(LiitteenToimitustapa(l.toimitustapa)) else null,
            if (l.toimitusosoite != null)
              Option.apply(
                LiitteenToimitusosoite(
                  osoite = Some(
                    LiitteenOsoite(
                      osoite = Some(toKielistettyMap(l.toimitusosoite.osoite.osoite)),
                      postinumero = (
                        Some(
                          LiitteenPostinumero(
                            koodiUri = Some(l.toimitusosoite.osoite.postinumero.postinumeroKoodiUri),
                            nimi = Some(toKielistettyMap(l.toimitusosoite.osoite.postinumero.nimi))
                          )
                        )
                      )
                    )
                  ),
                  l.toimitusosoite.sahkoposti,
                  l.toimitusosoite.verkkosivu
                )
              )
            else None
          )
        })
    } else List.empty
  }

  def getHakukohdeMetadataIndexed(metadataES: HakukohdeMetadataES): Option[HakukohdeMetadataIndexed] = {

    Some(
      HakukohdeMetadataIndexed(
        kaytetaanHaunAlkamiskautta = Some(metadataES.kaytetaanHaunAlkamiskautta),
        aloituspaikat = Some(
          AloituspaikatIndexed(
            lukumaara = Option.apply(metadataES.aloituspaikat.lukumaara),
            ensikertalaisille = Option.apply(metadataES.aloituspaikat.ensikertalaisille)
          )
        ),
        uudenOpiskelijanUrl = Option.apply(toKielistettyMap(metadataES.uudenOpiskelijanUrl)),
        koulutuksenAlkamiskausi = Option.apply(
          KoulutuksenAlkamiskausi(
            alkamiskausityyppi = Alkamiskausityyppi.withName(metadataES.koulutuksenAlkamiskausi.alkamiskausityyppi),
            koulutuksenAlkamiskausi = Option.apply(
              if (metadataES.koulutuksenAlkamiskausi.koulutuksenAlkamiskausi != null)
                KoodiUri(metadataES.koulutuksenAlkamiskausi.koulutuksenAlkamiskausi.koodiUri)
              else null
            ),
            koulutuksenAlkamisvuosi = Option.apply(metadataES.koulutuksenAlkamiskausi.koulutuksenAlkamisvuosi)
          )
        ),
        //
        hakukohteenLinja = Option.apply(
          HakukohteenLinjaIndexed(
            alinHyvaksyttyKeskiarvo = Option.apply((metadataES.hakukohteenLinja.alinHyvaksyttyKeskiarvo.toDouble)),
            painotetutArvosanat = metadataES.hakukohteenLinja.painotetutArvosanat.map(arvosana =>
              PainotettuArvosanaIndexed(
                painokerroin = arvosana.painokerroin,
                koodit = Option.apply(
                  PainottevaOppiaineKoodiIndexed(
                    oppiaine = Option.apply(OppiaineIndexed(koodiUri = Option.apply(arvosana.koodit.oppiaine.koodiUri)))
                  )
                )
              )
            ),
            painotetutArvosanatOppiaineittain =
              metadataES.hakukohteenLinja.painotetutArvosanatOppiaineittain.map(arvosana =>
                PainotettuArvosanaIndexed(
                  painokerroin = arvosana.painokerroin,
                  koodit = Option.apply(
                    PainottevaOppiaineKoodiIndexed(
                      oppiaine =
                        Option.apply(OppiaineIndexed(koodiUri = Option.apply(arvosana.koodit.oppiaine.koodiUri)))
                    )
                  )
                )
              ),
            linja = Option.apply(KoodiUri(metadataES.hakukohteenLinja.linja.koodiUri))
          )
        )
      )
    )

  }

  def toResult(): HakukohdeIndexed = {

    HakukohdeIndexed(
      oid = HakukohdeOid(oid),
      toteutus =
        if (toteutus != null)
          HakukohdeToteutusIndexed(
            oid = ToteutusOid(toteutus.oid),
            tarjoajat = toteutus.tarjoajat.map(tarjoaja =>
              Organisaatio(OrganisaatioOid(tarjoaja.oid), toKielistettyMap(organisaatio.nimi))
            )
          )
        else null,
      externalId = Option.apply(externalId),
      hakuOid = HakuOid(hakuOid),
      tila = if (tila != null) Julkaisutila(tila) else null,
      nimi = toKielistettyMap(nimi),
      jarjestyspaikka =
        if (organisaatio != null)
          Option.apply(Organisaatio(OrganisaatioOid(organisaatio.oid), toKielistettyMap(organisaatio.nimi)))
        else None,
      hakulomaketyyppi = Option.apply(hakulomaketyyppi).map(hakulomaketyyppi => Hakulomaketyyppi(hakulomaketyyppi)),
      hakulomakeAtaruId = Option.apply(hakulomakeAtaruId).map(hakulomakeAtaruId => UUID.fromString(hakulomakeAtaruId)),
      hakulomakeKuvaus = toKielistettyMap(hakulomakeKuvaus),
      hakulomakeLinkki = toKielistettyMap(hakulomakeLinkki),
      kaytetaanHaunHakulomaketta = Option.apply(kaytetaanHaunHakulomaketta),
      pohjakoulutusvaatimus = createPohjakoulutusvaatimus(pohjakoulutusvaatimus),
      muuPohjakoulutusvaatimus = toKielistettyMap(muuPohjakoulutusvaatimus),
      toinenAsteOnkoKaksoistutkinto = Option.apply(toinenAsteOnkoKaksoistutkinto),
      kaytetaanHaunAikataulua = Option.apply(kaytetaanHaunAikataulua),
      valintaperuste = createValintaperuste(valintaperuste),
      yhdenPaikanSaanto =
        if (yhdenPaikanSaanto != null)
          YhdenPaikanSaanto(voimassa = yhdenPaikanSaanto.voimassa, syy = yhdenPaikanSaanto.syy)
        else null,
      liitteetOnkoSamaToimitusaika = Option.apply(liitteetOnkoSamaToimitusaika),
      liitteetOnkoSamaToimitusosoite = Option.apply(liitteetOnkoSamaToimitusosoite),
      liitteidenToimitusaika = if (liitteidenToimitusaika != null) {
        Option.apply(LocalDateTime.parse(liitteidenToimitusaika))
      } else {
        None
      },
      liitteidenToimitustapa =
        if (liitteidenToimitustapa != null) Option.apply(LiitteenToimitustapa(liitteidenToimitustapa))
        else None,
      liitteidenToimitusosoite = getOsoite(liitteidenToimitusosoiteES),
      liitteet = getLiitteet(liitteet),
      valintakokeet = getValintakokeet(valintakokeet),
      hakuajat =
        if (hakuajat != null) hakuajat.map(hakuaika => {
          Ajanjakso(parseLocalDateTime(hakuaika.alkaa), Option.apply(parseLocalDateTime(hakuaika.paattyy)))
        })
        else List.empty,
      muokkaaja = if (muokkaaja != null) Muokkaaja(UserOid(muokkaaja.oid)) else null,
      metadata = getHakukohdeMetadataIndexed(metadata),
      organisaatio =
        if (organisaatio != null)
          Option.apply(Organisaatio(oid = OrganisaatioOid(organisaatio.oid), toKielistettyMap(organisaatio.nimi)))
        else null,
      kielivalinta = if (kielivalinta != null) kielivalinta.map(kieli => Kieli(kieli)) else Seq.empty,
      modified = if (modified != null) Option.apply(LocalDateTime.parse(modified)) else None,
      paateltyAlkamiskausi = Option.apply(
        if (paateltyAlkamiskausi != null) {
          PaateltyAlkamiskausi(
            alkamiskausityyppi = Alkamiskausityyppi.withName(paateltyAlkamiskausi.alkamiskausityyppi),
            kausiUri = paateltyAlkamiskausi.kausiUri,
            vuosi = paateltyAlkamiskausi.vuosi,
            source = paateltyAlkamiskausi.source
          )
        } else null
      ),
      koulutustyyppikoodi = Option.apply(koulutustyyppikoodi),
      salliikoHakukohdeHarkinnanvaraisuudenKysymisen = Option.apply(salliikoHakukohdeHarkinnanvaraisuudenKysymisen),
      voikoHakukohteessaOllaHarkinnanvaraisestiHakeneita =
        Option.apply(voikoHakukohteessaOllaHarkinnanvaraisestiHakeneita),
      sora = Option.apply(Sora(sora.tila)),
      jarjestaaUrheilijanAmmKoulutusta =
        Option.apply(jarjestaaUrheilijanAmmKoulutusta), //jarjestaaUrheilijanAmmKoulutusta:        Option[Boolean],
      hakukohde = Option.apply(KoodiUri(hakukohde)),
      odwKkTasot = Option.apply(
        OdwKkTasotIndexed(
          alempiKkAste = odwKkTasot.alempiKkAste,
          ylempiKkAste = odwKkTasot.ylempiKkAste,
          kkTutkinnonTaso = odwKkTasot.kkTutkinnonTaso,
          kkTutkinnonTasoSykli = odwKkTasot.kkTutkinnonTasoSykli
        )
      )
    )

  }

  def toKielistettyMap(map: Map[String, String]): Kielistetty = {
    Map(
      Kieli.En -> map.get("en"),
      Kieli.Fi -> map.get("fi"),
      Kieli.Sv -> map.get("sv")
    ).collect { case (k, Some(v)) => (k, v) }
  }

  def createPohjakoulutusvaatimus(pohjakoulutusvaatimus: List[Map[String, Object]]): Seq[KoodiUri] = {
    if (pohjakoulutusvaatimus != null && !pohjakoulutusvaatimus.isEmpty) {
      pohjakoulutusvaatimus
        .map(p => {
          KoodiUri(p.get("koodiUri").get.toString)
        })
        .toSeq
    } else {
      Seq.empty
    }
  }
}
case class HakukohdeToteutusIndexed(oid: ToteutusOid, tarjoajat: List[Organisaatio])

case class AloituspaikatIndexed(lukumaara: Option[Int], ensikertalaisille: Option[Int])

case class OppiaineIndexed(
    koodiUri: Option[String]
)

case class PainottevaOppiaineKoodiIndexed(
    oppiaine: Option[OppiaineIndexed]
)

case class PainotettuArvosanaIndexed(
    painokerroin: Option[Double],
    koodit: Option[PainottevaOppiaineKoodiIndexed]
) {
  def toPainotettuArvosana: PainotettuArvosana = {
    PainotettuArvosana(
      koodiUri = koodit.flatMap(_.oppiaine).flatMap(_.koodiUri),
      painokerroin = painokerroin
    )
  }
}

case class OdwKkTasotIndexed(
    alempiKkAste: Boolean,
    ylempiKkAste: Boolean,
    kkTutkinnonTaso: Int,
    kkTutkinnonTasoSykli: Int
) {
  def toOdwKkTasot: OdwKkTasot = {
    OdwKkTasot(
      alempiKkAste = alempiKkAste,
      ylempiKkAste = ylempiKkAste,
      kkTutkinnonTaso = kkTutkinnonTaso,
      kkTutkinnonTasoSykli = kkTutkinnonTasoSykli
    )
  }
}

case class HakukohteenLinjaIndexed(
    alinHyvaksyttyKeskiarvo: Option[Double],
    painotetutArvosanat: List[PainotettuArvosanaIndexed],
    painotetutArvosanatOppiaineittain: List[PainotettuArvosanaIndexed],
    linja: Option[KoodiUri]
)

case class HakukohdeMetadataIndexed(
    kaytetaanHaunAlkamiskautta: Option[Boolean],
    aloituspaikat: Option[AloituspaikatIndexed],
    uudenOpiskelijanUrl: Option[Kielistetty],
    koulutuksenAlkamiskausi: Option[KoulutuksenAlkamiskausi],
    hakukohteenLinja: Option[HakukohteenLinjaIndexed]
)

case class LukioTieto(isLukio: Boolean = true, linja: Option[KoodiUri])

case class HakukohdeIndexed(
    oid: HakukohdeOid,
    toteutus: HakukohdeToteutusIndexed,
    hakuOid: HakuOid,
    tila: Julkaisutila,
    nimi: Kielistetty,
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeAtaruId: Option[UUID],
    hakulomakeKuvaus: Kielistetty,
    hakulomakeLinkki: Kielistetty,
    kaytetaanHaunHakulomaketta: Option[Boolean],
    pohjakoulutusvaatimus: Seq[KoodiUri],
    muuPohjakoulutusvaatimus: Kielistetty,
    toinenAsteOnkoKaksoistutkinto: Option[Boolean],
    kaytetaanHaunAikataulua: Option[Boolean],
    valintaperuste: Option[ValintaperusteIndexed],
    yhdenPaikanSaanto: YhdenPaikanSaanto,
    koulutustyyppikoodi: Option[String],
    salliikoHakukohdeHarkinnanvaraisuudenKysymisen: Option[Boolean],
    voikoHakukohteessaOllaHarkinnanvaraisestiHakeneita: Option[Boolean],
    liitteetOnkoSamaToimitusaika: Option[Boolean],
    liitteetOnkoSamaToimitusosoite: Option[Boolean],
    liitteidenToimitusaika: Option[LocalDateTime],
    liitteidenToimitustapa: Option[LiitteenToimitustapa],
    liitteidenToimitusosoite: Option[LiitteenToimitusosoite],
    liitteet: List[Liite],
    sora: Option[Sora],
    valintakokeet: List[ValintakoeIndexed],
    hakuajat: List[Ajanjakso],
    muokkaaja: Muokkaaja,
    jarjestyspaikka: Option[Organisaatio],
    organisaatio: Option[Organisaatio],
    kielivalinta: Seq[Kieli],
    modified: Option[LocalDateTime],
    metadata: Option[HakukohdeMetadataIndexed],
    jarjestaaUrheilijanAmmKoulutusta: Option[Boolean],
    externalId: Option[String],
    hakukohde: Option[KoodiUri],
    paateltyAlkamiskausi: Option[PaateltyAlkamiskausi],
    odwKkTasot: Option[OdwKkTasotIndexed]
) extends WithTila
    with Logging {
  def toHakukohde(oikeusHakukohteeseenFn: OrganisaatioOid => Option[Boolean]): Hakukohde = {
    try {
      val tarjoaja = jarjestyspaikka.map(o => o.oid)
      Hakukohde(
        oid = oid,
        toteutusOid = toteutus.oid,
        hakuOid = hakuOid,
        tila = tila,
        nimi = nimi,
        alkamiskausiKoodiUri = metadata.flatMap(m =>
          m.koulutuksenAlkamiskausi
            .flatMap(_.koulutuksenAlkamiskausi.map(_.koodiUri))
        ),
        alkamisvuosi = metadata.flatMap(m => m.koulutuksenAlkamiskausi.flatMap(_.koulutuksenAlkamisvuosi)),
        kaytetaanHaunAlkamiskautta = metadata.flatMap(_.kaytetaanHaunAlkamiskautta),
        hakulomaketyyppi = hakulomaketyyppi,
        hakulomakeAtaruId = hakulomakeAtaruId,
        hakulomakeKuvaus = hakulomakeKuvaus,
        hakulomakeLinkki = hakulomakeLinkki,
        kaytetaanHaunHakulomaketta = kaytetaanHaunHakulomaketta,
        aloituspaikat = metadata.flatMap(_.aloituspaikat.flatMap(_.lukumaara)),
        ensikertalaisenAloituspaikat = metadata.flatMap(_.aloituspaikat.flatMap(_.ensikertalaisille)),
        alinHyvaksyttyKeskiarvo = metadata.flatMap(_.hakukohteenLinja.flatMap(_.alinHyvaksyttyKeskiarvo)),
        painotetutArvosanat = metadata
          .flatMap(_.hakukohteenLinja.flatMap(linja => Option.apply(linja.painotetutArvosanatOppiaineittain)))
          .getOrElse(List.empty)
          .map(_.toPainotettuArvosana),
        pohjakoulutusvaatimusKoodiUrit = pohjakoulutusvaatimus.map(_.koodiUri),
        muuPohjakoulutusvaatimus = muuPohjakoulutusvaatimus,
        toinenAsteOnkoKaksoistutkinto = toinenAsteOnkoKaksoistutkinto,
        kaytetaanHaunAikataulua = kaytetaanHaunAikataulua,
        valintaperusteId = valintaperuste.flatMap(_.id),
        valintaperusteValintakokeet =
          valintaperuste.flatMap(vp => Option.apply(vp.valintakokeet)).getOrElse(List.empty).map(_.toValintakoe),
        yhdenPaikanSaanto = yhdenPaikanSaanto,
        koulutustyyppikoodi = koulutustyyppikoodi,
        salliikoHakukohdeHarkinnanvaraisuudenKysymisen = salliikoHakukohdeHarkinnanvaraisuudenKysymisen,
        voikoHakukohteessaOllaHarkinnanvaraisestiHakeneita = voikoHakukohteessaOllaHarkinnanvaraisestiHakeneita,
        liitteetOnkoSamaToimitusaika = liitteetOnkoSamaToimitusaika,
        liitteetOnkoSamaToimitusosoite = liitteetOnkoSamaToimitusosoite,
        liitteidenToimitusaika = liitteidenToimitusaika,
        liitteidenToimitustapa = liitteidenToimitustapa,
        liitteidenToimitusosoite = liitteidenToimitusosoite,
        liitteet = liitteet,
        sora = sora,
        valintakokeet = valintakokeet.map(_.toValintakoe),
        hakuajat = hakuajat,
        muokkaaja = muokkaaja.oid,
        tarjoaja = tarjoaja,
        organisaatioOid = organisaatio.get.oid,
        organisaatioNimi = organisaatio.get.nimi,
        kielivalinta = kielivalinta,
        modified = modified,
        oikeusHakukohteeseen = tarjoaja.flatMap(t => oikeusHakukohteeseenFn(t)),
        jarjestaaUrheilijanAmmKoulutusta = jarjestaaUrheilijanAmmKoulutusta,
        externalId = externalId,
        uudenOpiskelijanUrl = metadata.flatMap(_.uudenOpiskelijanUrl),
        hakukohde = hakukohde,
        lukioTieto = metadata.flatMap(m => m.hakukohteenLinja.map(l => LukioTieto(linja = l.linja))),
        paateltyAlkamiskausi = paateltyAlkamiskausi,
        odwKkTasot = odwKkTasot.map(_.toOdwKkTasot)
      )
    } catch {
      case e: Exception => {
        val msg: String = s"Failed to create Hakukohde (${oid})"
        logger.error(msg, e)
        throw new RuntimeException(msg, e)
      }
    }
  }

  def toHakukohde: Hakukohde = {
    toHakukohde(_ => None)
  }
}
