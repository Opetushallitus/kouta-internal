package fi.oph.kouta.internal.domain

import java.time.LocalDateTime

import fi.oph.kouta.domain.Koulutustyyppi
import fi.oph.kouta.internal.domain.enums.{Kieli, Julkaisutila}
import fi.oph.kouta.internal.domain.oid.{KoulutusOid, OrganisaatioOid, UserOid}
import fi.oph.kouta.internal.swagger.SwaggerModel

@SwaggerModel(
  """    Koulutus:
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
    |          description: "Koulutuksen tyyppi. Sallitut arvot: 'amm' (ammatillinen), 'yo' (yliopisto), 'lk' (lukio), 'amk' (ammattikorkea), 'muu' (muu koulutus)"
    |          enum:
    |            - amm
    |            - yo
    |            - amk
    |            - lk
    |            - muu
    |          example: amm
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
    |            - $ref: '#/components/schemas/AmmattikorkeaKoulutusMetadata'
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
    muokkaaja: UserOid,
    organisaatioOid: OrganisaatioOid,
    kielivalinta: Seq[Kieli],
    modified: Option[LocalDateTime]
) extends PerustiedotWithOid
