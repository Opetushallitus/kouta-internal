package fi.oph.kouta.internal.domain

import java.util.UUID

import fi.oph.kouta.internal.swagger.SwaggerModel

@SwaggerModel(
  """    Valintatapa:
    |      type: object
    |      properties:
    |        valintatapaKoodiUri:
    |          type: string
    |          description: Valintatapa. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/valintatapajono/1)
    |          example: valintatapajono_av#1
    |        nimi:
    |          type: object
    |          description: Valintatapakuvauksen Opintopolussa näytettävä nimi eri kielillä. Kielet on määritetty valintaperusteen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Nimi'
    |        sisalto:
    |          type: array
    |          description: Valintatavan sisältö. Voi sisältää sekä teksti- että taulukkoelementtejä.
    |          items:
    |            type: object
    |            oneOf:
    |              - $ref: '#/components/schemas/SisaltoTeksti'
    |              - $ref: '#/components/schemas/SisaltoTaulukko'
    |        kaytaMuuntotaulukkoa:
    |          type: boolean
    |          description: "Käytetäänkö muuntotaulukkoa?"
    |        kynnysehto:
    |          type: object
    |          description: Kynnysehdon kuvausteksti eri kielillä. Kielet on määritetty valintaperusteen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Kuvaus'
    |        enimmaispisteet:
    |          type: double
    |          description: Valintatavan enimmäispisteet
    |          example: 20.0
    |        vahimmaispisteet:
    |          type: double
    |          description: Valintatavan vähimmäispisteet
    |          example: 10.0
    |"""
)
case class Valintatapa(
    nimi: Kielistetty = Map(),
    valintatapaKoodiUri: Option[String] = None,
    sisalto: Seq[Sisalto],
    kaytaMuuntotaulukkoa: Boolean = false,
    kynnysehto: Kielistetty = Map(),
    enimmaispisteet: Option[Double] = None,
    vahimmaispisteet: Option[Double] = None
)

case class Row(index: Int, isHeader: Boolean = false, columns: Seq[Column] = Seq())

case class Column(index: Int, text: Kielistetty = Map())
