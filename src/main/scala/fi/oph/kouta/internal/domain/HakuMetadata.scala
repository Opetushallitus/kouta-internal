package fi.oph.kouta.internal.domain

import fi.oph.kouta.internal.domain.indexed.KoulutuksenAlkamiskausi
import fi.oph.kouta.internal.swagger.SwaggerModel

import java.time.LocalDateTime

@SwaggerModel(
  """    HakuMetadata:
    |      type: object
    |      properties:
    |        yhteyshenkilot:
    |          type: array
    |          description: Haun yhteyshenkilöiden tiedot
    |          items:
    |            $ref: '#/components/schemas/Yhteyshenkilo'
    |        tulevaisuudenAikataulu:
    |          type: array
    |          description: Oppijalle Opintopolussa näytettävät haun mahdolliset tulevat hakuajat
    |          items:
    |            $ref: '#/components/schemas/Ajanjakso'
    |        koulutuksenAlkamiskausi:
    |          type: object
    |          properties:
    |            alkamiskausityyppi:
    |              type: string
    |              description: Alkamiskauden tyyppi
    |              enum:
    |                - 'henkilokohtainen suunnitelma'
    |                - 'tarkka alkamisajankohta'
    |                - 'alkamiskausi ja -vuosi'
    |            koulutuksenAlkamispaivamaara:
    |              type: string
    |              description: Koulutuksen tarkka alkamisen päivämäärä
    |              example: 2019-11-20T12:00
    |            koulutuksenAlkamiskausi:
    |              type: string
    |              description: Haun koulutusten alkamiskausi. Hakukohteella voi olla eri alkamiskausi kuin haulla.
    |                Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kausi/1)
    |              example: kausi_k#1
    |            koulutuksenAlkamisvuosi:
    |              type: string
    |              description: Haun koulutusten alkamisvuosi. Hakukohteella voi olla eri alkamisvuosi kuin haulla.
    |              example: 2020
    |        varasijatayttoPaattyy:
    |          type: string
    |          format: date-time
    |          description: Haun varasijatäytön päättymisen ajankohta. Käytetään oma-opiskelijavalinnassa hakijan tiedottamiseen.
    |          example: 2019-08-23T09:55
    |"""
)
case class HakuMetadata(
    yhteyshenkilot: Seq[Yhteyshenkilo],
    tulevaisuudenAikataulu: Seq[Ajanjakso],
    koulutuksenAlkamiskausi: Option[KoulutuksenAlkamiskausi],
    varasijatayttoPaattyy: Option[LocalDateTime]
)
