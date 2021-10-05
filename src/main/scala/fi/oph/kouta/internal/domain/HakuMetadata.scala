package fi.oph.kouta.internal.domain

import fi.oph.kouta.internal.domain.indexed.KoulutuksenAlkamiskausi
import fi.oph.kouta.internal.swagger.SwaggerModel

//TODO fix model
@SwaggerModel("""    HakuMetadata:
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
    |""")
case class HakuMetadata(
    yhteyshenkilot: Seq[Yhteyshenkilo],
    tulevaisuudenAikataulu: Seq[Ajanjakso],
    koulutuksenAlkamiskausi: Option[KoulutuksenAlkamiskausi]
)
