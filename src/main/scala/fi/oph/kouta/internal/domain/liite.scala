package fi.oph.kouta.internal.domain

import java.time.LocalDateTime
import java.util.UUID

import fi.oph.kouta.internal.domain.enums.LiitteenToimitustapa
import fi.oph.kouta.internal.swagger.SwaggerModel

@SwaggerModel(
  """    Liite:
    |      type: object
    |      properties:
    |        id:
    |          type: string
    |          description: Liitteen yksilöivä tunniste. Järjestelmän generoima.
    |          example: "ea596a9c-5940-497e-b5b7-aded3a2352a7"
    |        tyyppi:
    |          type: string
    |          description: Liitteen tyyppi. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/liitetyypitamm/1)
    |          example: liitetyypitamm_3#1
    |        nimi:
    |          type: object
    |          description: Liitteen Opintopolussa näytettävä nimi eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Nimi'
    |        kuvaus:
    |          type: object
    |          description: Liitteen Opintopolussa näytettävä kuvaus eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Kuvaus'
    |        toimitusaika:
    |          type: string
    |          description: Liitteen toimitusaika, jos ei ole sama kuin kaikilla hakukohteen liitteillä
    |          format: date-time
    |          example: 2019-08-23T09:55
    |        toimitustapa:
    |          type: string
    |          description: Liitteen toimitustapa, jos ei ole sama kuin kaikilla hakukohteen liitteillä
    |          example: "hakijapalvelu"
    |          enum:
    |            - hakijapalvelu
    |            - osoite
    |            - lomake
    |        toimitusosoite:
    |          type: object
    |          description: Liitteen toimitusosoite, jos ei ole sama kuin kaikilla hakukohteen liitteillä
    |          allOf:
    |            - $ref: '#/components/schemas/LiitteenToimitusosoite'
    |"""
)
case class Liite(
    id: Option[UUID],
    koodiUri: Option[String],
    nimi: Kielistetty,
    kuvaus: Option[Kielistetty],
    toimitusaika: Option[LocalDateTime],
    toimitustapa: Option[LiitteenToimitustapa],
    toimitusosoite: Option[LiitteenToimitusosoite]
)

@SwaggerModel("""    LiitteenToimitusosoite:
    |      type: object
    |      properties:
    |        osoite:
    |          type: object
    |          description: Liitteen toimitusosoite
    |          allOf:
    |            - $ref: '#/components/schemas/Osoite'
    |        sahkoposti:
    |          type: object
    |          description: Sähköpostiosoite, johon liite voidaan toimittaa
    |          allOf:
    |            - $ref: '#/components/schemas/Teksti'
    |""")
case class LiitteenToimitusosoite(osoite: Option[Osoite], sahkoposti: Option[String])
