package fi.oph.kouta.internal.domain

import fi.oph.kouta.domain.Koulutustyyppi
import fi.oph.kouta.internal.swagger.SwaggerModel

@SwaggerModel(
  """    KoulutusMetadata:
    |      type: object
    |      properties:
    |        kuvaus:
    |          type: object
    |          description: Koulutuksen kuvausteksti eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          $ref: '#/components/schemas/Kuvaus'
    |        lisatiedot:
    |          type: array
    |          description: Koulutukseen liittyviä lisätietoja, jotka näkyvät oppijalle Opintopolussa
    |          items:
    |            type: object
    |            $ref: '#/components/schemas/Lisatieto'
    |"""
)
sealed trait KoulutusMetadata {
  val tyyppi: Koulutustyyppi
  val kuvaus: Kielistetty
  val lisatiedot: Seq[Lisatieto]
}

@SwaggerModel("""    AmmatillinenKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |        - type: object
    |          properties:
    |            koulutustyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: amm
    |              enum:
    |                - amm
    |""")
case class AmmatillinenKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto]
) extends KoulutusMetadata

@SwaggerModel(
  """    KorkeakouluMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |      properties:
    |        kuvauksenNimi:
    |          type: object
    |          description: Koulutuksen kuvaukseni nimi eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          $ref: '#/components/schemas/Nimi'
    |        tutkintonimikeKoodiUrit:
    |          type: array
    |          description: Lista koulutuksen tutkintonimikkeistä. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/tutkintonimikekk/2)
    |          items:
    |            type: string
    |          example:
    |            - tutkintonimikekk_110#2
    |            - tutkintonimikekk_111#2
    |        opintojenLaajuusKoodiUri:
    |          type: string
    |          description: "Tutkinnon laajuus. Viittaa koodistoon [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/opintojenlaajuus/1)"
    |          example: opintojenlaajuus_40#1
    |        koulutusalaKoodiUrit:
    |          type: array
    |          description: Lista koulutusaloja. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kansallinenkoulutusluokitus2016koulutusalataso2/1)
    |          items:
    |            type: string
    |            example:
    |              - kansallinenkoulutusluokitus2016koulutusalataso2_054#1
    |              - kansallinenkoulutusluokitus2016koulutusalataso2_055#1
    |"""
)
trait KorkeakoulutusKoulutusMetadata extends KoulutusMetadata {
  val kuvauksenNimi: Kielistetty
  val tutkintonimikeKoodiUrit: Seq[String]
  val opintojenLaajuusKoodiUri: Option[String]
  val koulutusalaKoodiUrit: Seq[String]
}

@SwaggerModel("""    YliopistoKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KorkeakouluMetadata'
    |        - type: object
    |          properties:
    |            koulutustyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: yo
    |              enum:
    |                - yo
    |""")
case class YliopistoKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    koulutusalaKoodiUrit: Seq[String],
    tutkintonimikeKoodiUrit: Seq[String],
    opintojenLaajuusKoodiUri: Option[String],
    kuvauksenNimi: Kielistetty
) extends KorkeakoulutusKoulutusMetadata

@SwaggerModel("""    AmmattikorkeaKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KorkeakouluMetadata'
    |        - type: object
    |          properties:
    |            koulutustyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: amk
    |              enum:
    |                - amk
    |""")
case class AmmattikorkeakouluKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    koulutusalaKoodiUrit: Seq[String],
    tutkintonimikeKoodiUrit: Seq[String],
    opintojenLaajuusKoodiUri: Option[String],
    kuvauksenNimi: Kielistetty
) extends KorkeakoulutusKoulutusMetadata
