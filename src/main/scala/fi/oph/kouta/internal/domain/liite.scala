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
    |          type: object
    |          description: Liitteen tyyppi.
    |          allOf:
    |             - $ref: '#/components/schemas/LiitteenTyyppi'
    |        nimi:
    |          type: object
    |          description: Liitteen Opinion's näytettävä nimi eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
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
    tyyppi: Option[LiitteenTyyppi],
    nimi: Kielistetty,
    kuvaus: Option[Kielistetty],
    toimitusaika: Option[LocalDateTime],
    toimitustapa: Option[LiitteenToimitustapa],
    toimitusosoite: Option[LiitteenToimitusosoite]
)

@SwaggerModel(
  """    LiitteenTyyppi:
                |      type: object
                |      properties:
                |        koodiUri:
                |          type: string
                |          description: Liitteen tyyppi. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/liitetyypitamm/1)
                |          example: liitetyypitamm_3#1
                |        nimi:
                |          type: object
                |          description: Liitetyypin koodin arvo
                |          allOf:
                |            - $ref: '#/components/schemas/Nimi'
                |"""
)
case class LiitteenTyyppi(koodiUri: Option[String], nimi: Option[Kielistetty])

@SwaggerModel("""    LiitteenToimitusosoite:
    |      type: object
    |      properties:
    |        osoite:
    |          type: object
    |          description: Liitteen toimitusosoite
    |          allOf:
    |            - $ref: '#/components/schemas/LiitteenOsoite'
    |        sahkoposti:
    |          type: string
    |          description: Sähköpostiosoite, johon liite voidaan toimittaa
    |          allOf:
    |            - $ref: '#/components/schemas/Teksti'
    |""")
case class LiitteenToimitusosoite(osoite: Option[LiitteenOsoite], sahkoposti: Option[String])

@SwaggerModel("""    LiitteenOsoite:
                |      type: object
                |      properties:
                |        osoite:
                |          type: object
                |          description: Liitteen katuosoite
                |          allOf:
                |            - $ref: '#/components/schemas/Teksti'
                |        postinumero:
                |          type: object
                |          description: Postinumero ja postitoimipaikka kielistettynä
                |          allOf:
                |            - $ref: '#/components/schemas/LiitteenPostinumero'
                |""")
case class LiitteenOsoite(osoite: Option[Kielistetty], postinumero: Option[LiitteenPostinumero])

@SwaggerModel("""    LiitteenPostinumero:
                |      type: object
                |      properties:
                |        koodiUri:
                |          type: string
                |          description: Postinumeron koodiston koodi
                |          example: posti_00530#2
                |        nimi:
                |          type: object
                |          description: Postitoimipaikka kielistettynä
                |          allOf:
                |            - $ref: '#/components/schemas/Teksti'
                |""")
case class LiitteenPostinumero(koodiUri: Option[String], nimi: Option[Kielistetty])
