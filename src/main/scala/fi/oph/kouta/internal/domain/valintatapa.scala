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
    |        kuvaus:
    |          type: object
    |          description: Valintatavan kuvausteksti eri kielillä. Kielet on määritetty valintaperusteen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Kuvaus'
    |        sisalto:
    |          type: array
    |          description: Valintatavan sisältö. Voi sisältää sekä teksti- että taulukkoelementtejä.
    |          items:
    |            type: object
    |            oneOf:
    |              - $ref: '#/components/schemas/ValintatapaSisaltoTeksti'
    |              - $ref: '#/components/schemas/ValintatapaSisaltoTaulukko'
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
sealed trait Valintatapa {
  def valintatapaKoodiUri: Option[String]
  def kuvaus: Kielistetty
  def sisalto: Seq[ValintatapaSisalto]
  def kaytaMuuntotaulukkoa: Boolean
  def kynnysehto: Kielistetty
  def enimmaispisteet: Option[Double]
  def vahimmaispisteet: Option[Double]
}

@SwaggerModel("""    AmmatillinenValintatapa:
    |      type: object
    |      description: Ammatillisen koulutuksen valintatapakuvaus
    |      allOf:
    |        - $ref: '#/components/schemas/Valintatapa'
    |""")
case class AmmatillinenValintatapa(
    valintatapaKoodiUri: Option[String] = None,
    kuvaus: Kielistetty = Map(),
    sisalto: Seq[ValintatapaSisalto],
    kaytaMuuntotaulukkoa: Boolean = false,
    kynnysehto: Kielistetty = Map(),
    enimmaispisteet: Option[Double] = None,
    vahimmaispisteet: Option[Double] = None
) extends Valintatapa

@SwaggerModel(
  """    KorkeakoulutusValintatapa:
    |      type: object
    |      description: Korkeakoulutuksen valintatapakuvaus
    |      allOf:
    |        - $ref: '#/components/schemas/Valintatapa'
    |      properties:
    |        nimi:
    |          type: object
    |          description: Valintatapakuvauksen Opintopolussa näytettävä nimi eri kielillä. Kielet on määritetty valintaperusteen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Nimi'
    |"""
)
sealed trait KorkeakoulutusValintatapa extends Valintatapa {
  def nimi: Kielistetty
}

@SwaggerModel("""    AmmattikorkeakouluValintatapa:
    |      type: object
    |      description: Ammattikorkeakoulutuksen valintatapakuvaus
    |      allOf:
    |        - $ref: '#/components/schemas/KorkeakoulutusValintatapa'
    |""")
case class AmmattikorkeakouluValintatapa(
    nimi: Kielistetty = Map(),
    valintatapaKoodiUri: Option[String] = None,
    kuvaus: Kielistetty = Map(),
    sisalto: Seq[ValintatapaSisalto],
    kaytaMuuntotaulukkoa: Boolean = false,
    kynnysehto: Kielistetty = Map(),
    enimmaispisteet: Option[Double] = None,
    vahimmaispisteet: Option[Double] = None
) extends KorkeakoulutusValintatapa

@SwaggerModel("""    YliopistoValintatapa:
    |      type: object
    |      description: Yliopistokoulutuksen valintatapakuvaus
    |      allOf:
    |        - $ref: '#/components/schemas/KorkeakoulutusValintatapa'
    |""")
case class YliopistoValintatapa(
    nimi: Kielistetty = Map(),
    valintatapaKoodiUri: Option[String] = None,
    kuvaus: Kielistetty = Map(),
    sisalto: Seq[ValintatapaSisalto],
    kaytaMuuntotaulukkoa: Boolean = false,
    kynnysehto: Kielistetty = Map(),
    enimmaispisteet: Option[Double] = None,
    vahimmaispisteet: Option[Double] = None
) extends KorkeakoulutusValintatapa

sealed trait ValintatapaSisalto

@SwaggerModel(
  """    ValintatapaSisaltoTaulukko:
    |      type: object
    |      description: Taulukkomuotoinen valintatavan sisällön kuvaus
    |      properties:
    |        id:
    |          type: string
    |          description: Taulukon yksilöivä tunnus
    |          example: "ea596a9c-5940-497e-b5b7-aded3a2352a7"
    |        nimi:
    |          type: object
    |          description: Taulukon Opintopolussa näytettävä nimi eri kielillä. Kielet on määritetty valintaperusteen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Nimi'
    |        rows:
    |          type: array
    |          description: Taukon rivit
    |          items:
    |            type: object
    |            properties:
    |              index:
    |                type: integer
    |                description: Rivin järjestysnumero
    |              isHeader:
    |                type: boolean
    |                description: Onko rivi otsikkorivi
    |              columns:
    |                type: array
    |                description: Rivin sarakkeet
    |                items:
    |                  type: object
    |                  properties:
    |                    index:
    |                      type: integer
    |                      description: Sarakkeen järjestysnumero
    |                    text:
    |                      type: object
    |                      description: Sarakkeen Opintopolussa näytettävä teksti eri kielillä.
    |                        Kielet on määritetty valintaperusteen kielivalinnassa.
    |                      allOf:
    |                        - $ref: '#/components/schemas/Teksti'
    |"""
)
case class Taulukko(id: Option[UUID], nimi: Kielistetty = Map(), rows: Seq[Row] = Seq()) extends ValintatapaSisalto

@SwaggerModel(
  """    ValintatapaSisaltoTeksti:
    |      type: object
    |      description: Tekstimuotoinen valintatavan sisällön kuvaus
    |      properties:
    |        teksti:
    |          type: object
    |          description: Valintatavan Opintopolussa näytettävä kuvausteksti eri kielillä. Kielet on määritetty valintaperusteen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Teksti'
    |"""
)
case class ValintatapaSisaltoTeksti(teksti: Kielistetty) extends ValintatapaSisalto

case class Row(index: Int, isHeader: Boolean = false, columns: Seq[Column] = Seq())

case class Column(index: Int, text: Kielistetty = Map())
