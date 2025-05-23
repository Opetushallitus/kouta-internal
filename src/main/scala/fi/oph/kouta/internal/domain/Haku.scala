package fi.oph.kouta.internal.domain

import java.time.LocalDateTime
import java.util.UUID
import fi.oph.kouta.internal.swagger.SwaggerModel
import fi.oph.kouta.internal.domain.enums.{Hakulomaketyyppi, Julkaisutila, Kieli}
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid, UserOid}

@SwaggerModel(
  """    BaseHaku:
    |      type: object
    |      properties:
    |        oid:
    |          type: string
    |          description: Haun yksilöivä tunniste. Järjestelmän generoima.
    |          example: "1.2.246.562.29.00000000000000000009"
    |        tila:
    |          type: string
    |          example: "julkaistu"
    |          enum:
    |            - julkaistu
    |            - arkistoitu
    |            - tallennettu
    |          description: Haun julkaisutila. Jos haku on julkaistu, se näkyy oppijalle Opintopolussa.
    |        nimi:
    |          type: object
    |          description: Haun Opintopolussa näytettävä nimi eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Nimi'
    |        hakutapaKoodiUri:
    |          type: string
    |          description: Haun hakutapa. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/hakutapa/11)
    |          example: hakutapa_03#1
    |        hakukohteenLiittamisenTakaraja:
    |          type: string
    |          format: date-time
    |          description: Viimeinen ajanhetki, jolloin hakuun saa liittää hakukohteen.
    |            Hakukohteita ei saa lisätä enää sen jälkeen, kun haku on käynnissä.
    |          example: 2019-08-23T09:55
    |        hakukohteenMuokkaamiseenTakaraja:
    |          type: string
    |          format: date-time
    |          description: Viimeinen ajanhetki, jolloin hakuun liitettyä hakukohdetta on sallittua muokata.
    |            Hakukohteen tietoja ei saa muokata enää sen jälkeen, kun haku on käynnissä.
    |          example: 2019-08-23T09:55
    |        ajastettuJulkaisu:
    |          type: string
    |          format: date-time
    |          description: Ajanhetki, jolloin haku ja siihen liittyvät hakukohteet ja koulutukset julkaistaan
    |            automaattisesti Opintopolussa, jos ne eivät vielä ole julkisia
    |          example: 2019-08-23T09:55
    |        alkamiskausiKoodiUri:
    |          type: string
    |          description: Haun koulutusten alkamiskausi. Hakukohteella voi olla eri alkamiskausi kuin haulla.
    |            Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kausi/1)
    |          example: kausi_k#1
    |        alkamisvuosi:
    |          type: string
    |          description: Haun koulutusten alkamisvuosi. Hakukohteella voi olla eri alkamisvuosi kuin haulla.
    |          example: 2020
    |        kohdejoukkoKoodiUri:
    |          type: string
    |          description: Haun kohdejoukko. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/haunkohdejoukko/1)
    |          example: haunkohdejoukko_17#1
    |        kohdejoukonTarkenneKoodiUri:
    |          type: string
    |          description: Haun kohdejoukon tarkenne. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/haunkohdejoukontarkenne/1)
    |          example: haunkohdejoukontarkenne_1#1
    |        hakulomaketyyppi:
    |          type: string
    |          description: Hakulomakkeen tyyppi. Kertoo, käytetäänkö Atarun (hakemuspalvelun) hakulomaketta, muuta hakulomaketta
    |            (jolloin voidaan lisätä hakulomakkeeseen linkki) tai onko niin, ettei sähkököistä hakulomaketta ole lainkaan, jolloin sille olisi hyvä lisätä kuvaus.
    |            Hakukohteella voi olla eri hakulomake kuin haulla.
    |          example: "ataru"
    |          enum:
    |            - ataru
    |            - ei sähköistä
    |            - muu
    |        hakulomakeAtaruId:
    |          type: string
    |          description: Hakulomakkeen yksilöivä tunniste, jos käytössä on Atarun (hakemuspalvelun) hakulomake. Hakukohteella voi olla eri hakulomake kuin haulla.
    |          example: "ea596a9c-5940-497e-b5b7-aded3a2352a7"
    |        hakulomakeKuvaus:
    |          type: object
    |          description: Hakulomakkeen kuvausteksti eri kielillä. Kielet on määritetty haun kielivalinnassa. Hakukohteella voi olla eri hakulomake kuin haulla.
    |          allOf:
    |            - $ref: '#/components/schemas/Kuvaus'
    |        hakulomakeLinkki:
    |          type: object
    |          description: Hakulomakkeen linkki eri kielillä. Kielet on määritetty haun kielivalinnassa. Hakukohteella voi olla eri hakulomake kuin haulla.
    |          allOf:
    |            - $ref: '#/components/schemas/Linkki'
    |        hakuajat:
    |          type: array
    |          description: Haun hakuajat. Hakukohteella voi olla omat hakuajat.
    |          items:
    |            $ref: '#/components/schemas/Ajanjakso'
    |        valintakokeet:
    |          type: array
    |          description: Hakuun liittyvät valintakokeet
    |          items:
    |            $ref: '#/components/schemas/Valintakoe'
    |        metadata:
    |          type: object
    |          $ref: '#/components/schemas/HakuMetadata'
    |        kielivalinta:
    |          type: array
    |          description: Kielet, joille haun nimi, kuvailutiedot ja muut tekstit on käännetty
    |          items:
    |            $ref: '#/components/schemas/Kieli'
    |          example:
    |            - fi
    |            - sv
    |        muokkaaja:
    |          type: string
    |          description: Hakua viimeksi muokanneen virkailijan henkilö-oid
    |          example: 1.2.246.562.10.00101010101
    |        organisaatioOid:
    |           type: string
    |           description: Haun luoneen organisaation oid
    |           example: 1.2.246.562.10.00101010101
    |        modified:
    |           type: string
    |           format: date-time
    |           description: Haun viimeisin muokkausaika. Järjestelmän generoima
    |           example: 2019-08-23T09:55
    |        externalId:
    |           type: string
    |           description: Ulkoinen tunniste (esim. oppilaitoksen järjestelmän yksilöivä tunniste)
    |"""
)
trait BaseHaku extends PerustiedotWithOid {
  def hakutapaKoodiUri: Option[String]
  def hakukohteenLiittamisenTakaraja: Option[LocalDateTime]
  def hakukohteenMuokkaamisenTakaraja: Option[LocalDateTime]
  def ajastettuJulkaisu: Option[LocalDateTime]
  def alkamiskausiKoodiUri: Option[String]
  def alkamisvuosi: Option[String]
  def kohdejoukkoKoodiUri: String
  def kohdejoukonTarkenneKoodiUri: Option[String]
  def hakulomaketyyppi: Option[Hakulomaketyyppi]
  def hakulomakeAtaruId: Option[UUID]
  def hakulomakeKuvaus: Kielistetty
  def hakulomakeLinkki: Kielistetty
  def metadata: Option[HakuMetadata]
  def hakuajat: List[Ajanjakso]
  def valintakokeet: List[Valintakoe]
  def externalId: Option[String]
}

@SwaggerModel(
  """    Haku:
    |      type: object
    |      allOf:
    |        - $ref: '#/components/schemas/BaseHaku'
    |      properties:
    |        maksullinenKkHaku:
    |           type: boolean
    |           description: Onko kyseessä hakemusmaksullinen korkeakouluhaku
    |"""
)
case class Haku(
    oid: HakuOid,
    hakukohdeOids: Option[List[HakukohdeOid]],
    totalHakukohteet: Int,
    tila: Julkaisutila,
    nimi: Kielistetty,
    hakutapaKoodiUri: Option[String],
    hakukohteenLiittamisenTakaraja: Option[LocalDateTime],
    hakukohteenMuokkaamisenTakaraja: Option[LocalDateTime],
    ajastettuJulkaisu: Option[LocalDateTime],
    alkamiskausiKoodiUri: Option[String],
    alkamisvuosi: Option[String],
    kohdejoukkoKoodiUri: String,
    kohdejoukonTarkenneKoodiUri: Option[String],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeAtaruId: Option[UUID],
    hakulomakeKuvaus: Kielistetty,
    hakulomakeLinkki: Kielistetty,
    hakuvuosi: Option[Int],
    hakukausi: Option[String],
    metadata: Option[HakuMetadata],
    organisaatioOid: OrganisaatioOid,
    hakuajat: List[Ajanjakso],
    valintakokeet: List[Valintakoe],
    muokkaaja: UserOid,
    kielivalinta: Seq[Kieli],
    modified: Option[LocalDateTime],
    externalId: Option[String],
    maksullinenKkHaku: Boolean
) extends BaseHaku

@SwaggerModel(
  """    OdwHaku:
    |      type: object
    |      allOf:
    |        - $ref: '#/components/schemas/BaseHaku'
    |      properties:
    |        hakuvuosi:
    |          type: string
    |          description: Haun hakuajoista päätelty hakuvuosi
    |          example: 2022
    |        hakukausi:
    |          type: string
    |          description: Haun hakuajoista päätelty hakukausi
    |          example: kausi_s#1
    |"""
)
case class OdwHaku(
    oid: HakuOid,
    tila: Julkaisutila,
    nimi: Kielistetty,
    hakutapaKoodiUri: Option[String],
    hakukohteenLiittamisenTakaraja: Option[LocalDateTime],
    hakukohteenMuokkaamisenTakaraja: Option[LocalDateTime],
    ajastettuJulkaisu: Option[LocalDateTime],
    alkamiskausiKoodiUri: Option[String],
    alkamisvuosi: Option[String],
    kohdejoukkoKoodiUri: String,
    kohdejoukonTarkenneKoodiUri: Option[String],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeAtaruId: Option[UUID],
    hakulomakeKuvaus: Kielistetty,
    hakulomakeLinkki: Kielistetty,
    metadata: Option[HakuMetadata],
    organisaatioOid: OrganisaatioOid,
    hakuajat: List[Ajanjakso],
    valintakokeet: List[Valintakoe],
    muokkaaja: UserOid,
    kielivalinta: Seq[Kieli],
    modified: Option[LocalDateTime],
    externalId: Option[String],
    hakuvuosi: Option[String],
    hakukausi: Option[String]
) extends BaseHaku
