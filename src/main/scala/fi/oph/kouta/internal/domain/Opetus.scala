package fi.oph.kouta.internal.domain

import fi.oph.kouta.domain.Maksullisuustyyppi
import fi.oph.kouta.internal.swagger.SwaggerModel

@SwaggerModel(
  """    Opetus:
    |      type: object
    |      properties:
    |        opetuskieliKoodiUrit:
    |          type: array
    |          description: Lista koulutuksen toteutuksen opetuskielistä. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/oppilaitoksenopetuskieli/1)
    |          items:
    |            type: string
    |            example:
    |              - oppilaitoksenopetuskieli_1#1
    |              - oppilaitoksenopetuskieli_2#1
    |        opetuskieletKuvaus:
    |          type: object
    |          description: Koulutuksen toteutuksen opetuskieliä tarkentava kuvausteksti eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Kuvaus'
    |        opetusaikaKoodiUrit:
    |          type: array
    |          description: Lista koulutuksen toteutuksen opetusajoista. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/opetusaikakk/1)
    |          items:
    |            type: string
    |            example:
    |              - opetusaikakk_1#1
    |              - opetusaikakk_2#1
    |        opetusaikaKuvaus:
    |          type: object
    |          description: Koulutuksen toteutuksen opetusaikoja tarkentava kuvausteksti eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Kuvaus'
    |        opetustapaKoodiUrit:
    |          type: array
    |          description: Lista koulutuksen toteutuksen opetustavoista. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/opetuspaikkakk/1)
    |          items:
    |            type: string
    |            example:
    |              - opetuspaikkakk_2#1
    |              - opetuspaikkakk_2#1
    |        opetustapaKuvaus:
    |          type: object
    |          description: Koulutuksen toteutuksen opetustapoja tarkentava kuvausteksti eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Kuvaus'
    |        maksut:
    |          type: array
    |          description: Opetuksen maksullisuustiedot. Ammatillisilla ja lukiototeutuksilla voi olla yhtäaikaa kaksi eri maksutyyppiä, 'lukuvuosimaksu' ja 'maksullinen', muilla toteutuksilla vain yksi arvo, joka on julkaistulle toteutukselle pakollinen.
    |          items:
    |            type: object
    |            $ref: '#/components/schemas/Maksu'
    |        maksullisuusKuvaus:
    |          type: object
    |          description: Koulutuksen toteutuksen maksullisuutta tarkentava kuvausteksti eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Kuvaus'
    |        alkamiskausiKoodiUri:
    |          type: string
    |          description: Koulutuksen toteutuksen alkamiskausi. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kausi/1)
    |          example: kausi_k#1
    |        alkamisvuosi:
    |          type: string
    |          description: Koulutuksen toteutuksen alkamisvuosi
    |          example: 2020
    |        lisatiedot:
    |          type: array
    |          description: Koulutuksen toteutukseen liittyviä lisätietoja, jotka näkyvät oppijalle Opintopolussa
    |          items:
    |            type: object
    |            $ref: '#/components/schemas/Lisatieto'
    |"""
)
case class Opetus(
    opetuskieliKoodiUrit: Seq[String],
    opetuskieletKuvaus: Kielistetty,
    opetusaikaKoodiUrit: Seq[String],
    opetusaikaKuvaus: Kielistetty,
    opetustapaKoodiUrit: Seq[String],
    opetustapaKuvaus: Kielistetty,
    maksut: Seq[Maksu],
    maksullisuusKuvaus: Kielistetty,
    alkamiskausiKoodiUri: Option[String],
    alkamisvuosi: Option[String],
    lisatiedot: Seq[Lisatieto]
)

@SwaggerModel(
  """    Maksu:
    |      type: object
    |      required:
    |        - maksullisuustyyppi
    |      properties:
    |        maksullisuustyyppi:
    |          type: string
    |          description: Maksullisuuden tyyppi. Ammatillisilla ja lukiototeutuksilla voi olla yhtäaikaa sekä 'lukuvuosimaksu' että 'maksullinen' maksuissa, muilla toteutuksilla vain yksi arvo, joka on julkaistulle toteutukselle pakollinen.
    |          enum:
    |            - 'maksullinen'
    |            - 'maksuton'
    |            - 'lukuvuosimaksu'
    |        maksunMaara:
    |          type: double
    |          description: "Koulutuksen toteutuksen maksun määrä euroissa. Pakollinen, jos maksullisuustyyppi ei ole 'maksuton'."
    |          example: 220.50
    |"""
)
case class Maksu(
    maksullisuustyyppi: Maksullisuustyyppi,
    maksunMaara: Option[Double] = None
)
