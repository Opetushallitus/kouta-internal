package fi.oph.kouta.internal.domain

import java.time.LocalDateTime
import fi.oph.kouta.domain.Koulutustyyppi
import fi.oph.kouta.internal.domain.enums.{Julkaisutila, Kieli}
import fi.oph.kouta.internal.domain.oid.{KoulutusOid, OrganisaatioOid, UserOid}
import fi.oph.kouta.internal.swagger.SwaggerModel

import java.util.UUID

@SwaggerModel(
  """    BaseKoulutus:
    |      type: object
    |      properties:
    |        oid:
    |          type: string
    |          description: Koulutuksen yksilöivä tunniste. Järjestelmän generoima.
    |          example: "1.2.246.562.13.00000000000000000009"
    |        johtaaTutkintoon:
    |          type: boolean
    |          description: Onko koulutus tutkintoon johtavaa
    |        koulutustyyppi:
    |          type: string
    |          description: "Koulutuksen tyyppi. Sallitut arvot: 'amm' (ammatillinen), 'yo' (yliopisto), 'lk' (lukio), 'amk' (ammattikorkea), 'amm-ope-erityisope-ja-opo' (Ammatillinen opettaja-, erityisopettaja ja opinto-ohjaajakoulutus), 'amm-tutkinnon-osa', 'amm-osaamisala', 'amm-muu', 'tuva' (tutkintokoulutukseen valmentava koulutus), 'telma' (työhön ja itsenäiseen elämään valmentava koulutus), 'vapaa-sivistystyö-opistovuosi', 'vapaa-sivistystyo-muu', 'aikuisten-perusopetus', 'muu'"
    |          $ref: '#/components/schemas/Koulutustyyppi'
    |          example: amm
    |        koulutusKoodiUri:
    |          type: string
    |          deprecated: true
    |          description: Koulutuksen koodi URI. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/koulutus/11)
    |          example: koulutus_371101#1
    |        koulutusKoodiUrit:
    |          type: array
    |          description: Koulutuksen koodi URIt. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/koulutus/11)
    |          items:
    |            type: string
    |          example:
    |            - koulutus_371101#1
    |            - koulutus_201000#1
    |        tila:
    |          type: string
    |          example: "julkaistu"
    |          enum:
    |            - julkaistu
    |            - arkistoitu
    |            - tallennettu
    |          description: Koulutuksen julkaisutila. Jos koulutus on julkaistu, se näkyy oppijalle Opintopolussa.
    |        tarjoajat:
    |          type: array
    |          description: Koulutusta tarjoavien organisaatioiden yksilöivät organisaatio-oidit
    |          items:
    |            type: string
    |          example:
    |            - 1.2.246.562.10.00101010101
    |            - 1.2.246.562.10.00101010102
    |        julkinen:
    |          type: boolean
    |          description: Voivatko muut oppilaitokset käyttää koulutusta
    |        kielivalinta:
    |          type: array
    |          description: Kielet, joille koulutuksen nimi, kuvailutiedot ja muut tekstit on käännetty
    |          items:
    |            $ref: '#/components/schemas/Kieli'
    |          example:
    |            - fi
    |            - sv
    |        nimi:
    |          type: object
    |          description: Koulutuksen Opintopolussa näytettävä nimi eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Nimi'
    |        metadata:
    |          type: object
    |          oneOf:
    |            - $ref: '#/components/schemas/YliopistoKoulutusMetadata'
    |            - $ref: '#/components/schemas/AmmatillinenKoulutusMetadata'
    |            - $ref: '#/components/schemas/AmmatillinenTutkinnonOsaKoulutusMetadata'
    |            - $ref: '#/components/schemas/AmmatillinenOsaamisalaKoulutusMetadata'
    |            - $ref: '#/components/schemas/AmmatillinenOsaamisalaKoulutusMetadata'
    |            - $ref: '#/components/schemas/AmmattikorkeaKoulutusMetadata'
    |            - $ref: '#/components/schemas/AmmOpeErityisopeJaOpoKoulutusMetadata'
    |            - $ref: '#/components/schemas/LukioKoulutusMetadata'
    |            - $ref: '#/components/schemas/TuvaKoulutusMetadata'
    |            - $ref: '#/components/schemas/TelmaKoulutusMetadata'
    |            - $ref: '#/components/schemas/AmmatillinenMuuKoulutusMetadata'
    |            - $ref: '#/components/schemas/VapaaSivistystyoKoulutusMetadata'
    |            - $ref: '#/components/schemas/AikuistenPerusopetusKoulutusMetadata'
    |          example:
    |            koulutustyyppi: amm
    |            koulutusalaKoodiUrit:
    |              - kansallinenkoulutusluokitus2016koulutusalataso2_054#1
    |              - kansallinenkoulutusluokitus2016koulutusalataso2_055#1
    |            kuvaus:
    |              fi: Suomenkielinen kuvaus
    |              sv: Ruotsinkielinen kuvaus
    |            lisatiedot:
    |              - otsikkoKoodiUri: koulutuksenjarjestamisenlisaosiot_3#1
    |                teksti:
    |                  fi: Opintojen suomenkielinen lisätietokuvaus
    |                  sv: Opintojen ruotsinkielinen lisätietokuvaus
    |        sorakuvausId:
    |          type: string
    |          description: Koulutukseen liittyvän SORA-kuvauksen yksilöivä tunniste
    |          example: "ea596a9c-5940-497e-b5b7-aded3a2352a7"
    |        muokkaaja:
    |          type: string
    |          description: Koulutusta viimeksi muokanneen virkailijan henkilö-oid
    |          example: 1.2.246.562.10.00101010101
    |        organisaatioOid:
    |           type: string
    |           description: Koulutuksen luoneen organisaation oid
    |           example: 1.2.246.562.10.00101010101
    |        modified:
    |           type: string
    |           format: date-time
    |           description: Koulutuksen viimeisin muokkausaika. Järjestelmän generoima
    |           example: 2019-08-23T09:55
    |        externalId:
    |           type: string
    |           description: Ulkoinen tunniste (esim. oppilaitoksen järjestelmän yksilöivä tunniste)
    |"""
)
trait BaseKoulutus extends PerustiedotWithOid {
    def johtaaTutkintoon: Boolean
    def koulutustyyppi: Option[Koulutustyyppi]
    def koulutusKoodiUrit: Seq[String]
    def tarjoajat: List[OrganisaatioOid]
    def metadata: Option[KoulutusMetadata]
    def julkinen: Boolean
    def sorakuvausId: Option[UUID]
    def externalId: Option[String]
}

@SwaggerModel(
  """    Koulutus:
    |      type: object
    |      allOf:
    |        - $ref: '#/components/schemas/BaseKoulutus'
    |"""
)
case class Koulutus(
  oid: KoulutusOid,
  johtaaTutkintoon: Boolean,
  koulutustyyppi: Option[Koulutustyyppi],
  koulutusKoodiUrit: Seq[String],
  tila: Julkaisutila,
  tarjoajat: List[OrganisaatioOid],
  nimi: Kielistetty,
  metadata: Option[KoulutusMetadata],
  julkinen: Boolean,
  sorakuvausId: Option[UUID],
  muokkaaja: UserOid,
  organisaatioOid: OrganisaatioOid,
  kielivalinta: Seq[Kieli],
  modified: Option[LocalDateTime],
  externalId: Option[String]
) extends BaseKoulutus

@SwaggerModel(
  """    KoulutusKoodienAlatJaAsteet:
    |      type: object
    |      properties:
    |        koulutusKoodiUri:
    |          type: string
    |          example: koulutus_371101#1
    |        koulutusalaKoodiUrit:
    |          type: array
    |          description: Koulutus alan koodi URIt. Viittaavat esim. [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kansallinenkoulutusluokitus2016koulutusalataso1/1)
    |          items:
    |            type: string
    |          example:
    |            - kansallinenkoulutusluokitus2016koulutusalataso2_054#1
    |            - kansallinenkoulutusluokitus2016koulutusalataso2_055#1
    |        koulutusasteKoodiUrit:
    |          type: array
    |          description: Koulutus asteen koodi URIt. Viittaavat esim. [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kansallinenkoulutusluokitus2016koulutusastetaso1/1)
    |          items:
    |            type: string
    |
    |"""
)
case class KoulutusKoodienAlatJaAsteet(
  koulutusKoodiUri: Option[String],
  koulutusalaKoodiUrit: Seq[String],
  koulutusasteKoodiUrit: Seq[String]
)

@SwaggerModel(
  """    OdwKoulutus:
    |      type: object
    |      allOf:
    |        - $ref: '#/components/schemas/BaseKoulutus'
    |      properties:
    |        koulutuskoodienAlatJaAsteet:
    |          type: array
    |          items:
    |            type: object
    |            allOf:
    |              - $ref: '#/components/schemas/KoulutusKoodienAlatJaAsteet'
    |
    |"""
)
case class OdwKoulutus(
  oid: KoulutusOid,
  johtaaTutkintoon: Boolean,
  koulutustyyppi: Option[Koulutustyyppi],
  koulutusKoodiUrit: Seq[String],
  tila: Julkaisutila,
  tarjoajat: List[OrganisaatioOid],
  nimi: Kielistetty,
  metadata: Option[KoulutusMetadata],
  julkinen: Boolean,
  sorakuvausId: Option[UUID],
  muokkaaja: UserOid,
  organisaatioOid: OrganisaatioOid,
  kielivalinta: Seq[Kieli],
  modified: Option[LocalDateTime],
  externalId: Option[String],
  koulutuskoodienAlatJaAsteet: Seq[KoulutusKoodienAlatJaAsteet]
) extends BaseKoulutus