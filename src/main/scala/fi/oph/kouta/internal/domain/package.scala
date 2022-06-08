package fi.oph.kouta.internal

import java.time.LocalDateTime
import java.util.UUID

import fi.oph.kouta.internal.domain.enums.Kieli
import fi.oph.kouta.internal.swagger.SwaggerModel

package object domain {

  @SwaggerModel("""
      |    Koulutustyyppi:
      |      type: string
      |      enum:
      |        - aikuisten-perusopetus
      |        - amk
      |        - amm
      |        - amm-muu
      |        - amm-ope-erityisope-ja-opo,
      |        - amm-osaamisala
      |        - amm-tutkinnon-osa
      |        - kk-opintojakso
      |        - lk
      |        - muu
      |        - telma
      |        - tuva
      |        - vapaa-sivistystyo-muu
      |        - vapaa-sivistystyo-opistovuosi
      |        - yo
""")
  abstract class KoulutustyyppiSwagger

  // Kielistetyn swaggerit ovat tässä, koska pelkälle typelle ei voi asettaa annotaatiota
  @SwaggerModel("""    Teksti:
      |      type: object
      |      properties:
      |        fi:
      |          type: string
      |          example: Suomenkielinen teksti
      |          description: "Suomenkielinen teksti, jos kielivalinnassa on 'fi'"
      |        sv:
      |          type: string
      |          example: Ruotsinkielinen teksti
      |          description: "Ruotsinkielinen teksti, jos kielivalinnassa on 'sv'"
      |        en:
      |          type: string
      |          example: Englanninkielinen teksti
      |          description: "Englanninkielinen teksti, jos kielivalinnassa on 'en'"
      |    Nimi:
      |      type: object
      |      properties:
      |        fi:
      |          type: string
      |          example: Suomenkielinen nimi
      |          description: "Suomenkielinen nimi, jos kielivalinnassa on 'fi'"
      |        sv:
      |          type: string
      |          example: Ruotsinkielinen nimi
      |          description: "Ruotsinkielinen nimi, jos kielivalinnassa on 'sv'"
      |        en:
      |          type: string
      |          example: Englanninkielinen nimi
      |          description: "Englanninkielinen nimi, jos kielivalinnassa on 'en'"
      |    Kuvaus:
      |      type: object
      |      properties:
      |        fi:
      |          type: string
      |          example: Suomenkielinen kuvaus
      |          description: "Suomenkielinen kuvaus, jos kielivalinnassa on 'fi'"
      |        sv:
      |          type: string
      |          example: Ruotsinkielinen kuvaus
      |          description: "Ruotsinkielinen kuvaus, jos kielivalinnassa on 'sv'"
      |        en:
      |          type: string
      |          example: Englanninkielinen kuvaus
      |          description: "Englanninkielinen kuvaus, jos kielivalinnassa on 'en'"
      |    Linkki:
      |      type: object
      |      properties:
      |        fi:
      |          type: string
      |          example: Linkki suomenkieliselle sivulle
      |          description: "Linkki suomenkieliselle sivulle, jos kielivalinnassa on 'fi'"
      |        sv:
      |          type: string
      |          example: Linkki ruotsinkieliselle sivulle
      |          description: "Linkki ruotsinkieliselle sivulle, jos kielivalinnassa on 'sv'"
      |        en:
      |          type: string
      |          example: Linkki englanninkieliselle sivulle
      |          description: "Linkki englanninkieliselle sivulle, jos kielivalinnassa on 'en'"
      |""")
  abstract class KielistettySwagger

  type Kielistetty = Map[Kieli, String]

  @SwaggerModel(
    """    Lisatieto:
      |      type: object
      |      properties:
      |        otsikkoKoodiUri:
      |          type: string
      |          description: Lisätiedon otsikon koodi URI. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/koulutuksenjarjestamisenlisaosiot/1)
      |          example: koulutuksenjarjestamisenlisaosiot_3#1
      |        teksti:
      |          type: object
      |          description: Lisätiedon teksti eri kielillä. Kielet on määritetty kielivalinnassa.
      |          allOf:
      |            - $ref: '#/components/schemas/Teksti'
      |"""
  )
  case class Lisatieto(otsikkoKoodiUri: String, teksti: Kielistetty)

  @SwaggerModel(
    """    Yhteyshenkilo:
      |      type: object
      |      properties:
      |        nimi:
      |          type: object
      |          description: Yhteyshenkilön nimi eri kielillä. Kielet on määritetty kielivalinnassa.
      |          allOf:
      |            - $ref: '#/components/schemas/Teksti'
      |        titteli:
      |          type: object
      |          description: Yhteyshenkilön titteli eri kielillä. Kielet on määritetty kielivalinnassa.
      |          allOf:
      |            - $ref: '#/components/schemas/Teksti'
      |        sahkoposti:
      |          type: object
      |          description: Yhteyshenkilön sähköpostiosoite eri kielillä. Kielet on määritetty kielivalinnassa.
      |          allOf:
      |            - $ref: '#/components/schemas/Teksti'
      |        puhelinnumero:
      |          type: object
      |          description: Yhteyshenkilön puhelinnumero eri kielillä. Kielet on määritetty kielivalinnassa.
      |          allOf:
      |            - $ref: '#/components/schemas/Teksti'
      |        wwwSivu:
      |          type: object
      |          description: Yhteyshenkilön www-sivu eri kielillä. Kielet on määritetty kielivalinnassa.
      |          allOf:
      |            - $ref: '#/components/schemas/Teksti'
      |"""
  )
  case class Yhteyshenkilo(
      nimi: Kielistetty,
      titteli: Kielistetty,
      sahkoposti: Kielistetty,
      puhelinnumero: Kielistetty,
      wwwSivu: Kielistetty
  )

  @SwaggerModel("""    Ajanjakso:
      |      type: object
      |      properties:
      |        alkaa:
      |           type: string
      |           format: date-time
      |           description: Ajanjakson alkuaika
      |           example: 2019-08-23T09:55
      |        paattyy:
      |           type: string
      |           format: date-time
      |           description: Ajanjakson päättymisaika
      |           example: 2019-08-23T09:55
      |""")
  case class Ajanjakso(alkaa: LocalDateTime, paattyy: Option[LocalDateTime])

  @SwaggerModel(
    """    Valintakoe:
      |      type: object
      |      description: Valintakokeen tiedot
      |      properties:
      |        id:
      |          type: string
      |          description: Valintakokeen yksilöivä tunniste. Järjestelmän generoima.
      |          example: "ea596a9c-5940-497e-b5b7-aded3a2352a7"
      |        tyyppi:
      |          type: string
      |          description: Valintakokeen tyyppi. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/valintakokeentyyppi/1)
      |          example: valintakokeentyyppi_1#1
      |        tilaisuudet:
      |          type: array
      |          description: Valintakokeen järjestämistilaisuudet
      |          items:
      |            $ref: '#/components/schemas/Valintakoetilaisuus'
      |        vahimmaispisteet:
      |           type: number
      |           format: double
      |           description: Vähimmäispisteet mitkä vaaditaan valintakokeen läpäisyyn
      |"""
  )
  case class Valintakoe(
      id: Option[UUID],
      tyyppi: Option[String],
      tilaisuudet: Option[List[Valintakoetilaisuus]],
      vahimmaispisteet: Option[Double]
  )

  @SwaggerModel(
    """    Valintakoetilaisuus:
      |      type: object
      |      properties:
      |        osoite:
      |          type: object
      |          description: Valintakokeen järjestämispaikan osoite
      |          allOf:
      |            - $ref: '#/components/schemas/Osoite'
      |        aika:
      |          type: array
      |          description: Valintakokeen järjestämisaika
      |          items:
      |            $ref: '#/components/schemas/Ajanjakso'
      |        lisatietoja:
      |          type: object
      |          description: Lisätietoja valintakokeesta eri kielillä. Kielet on määritetty kielivalinnassa.
      |          allOf:
      |            - $ref: '#/components/schemas/Teksti'
      |"""
  )
  case class Valintakoetilaisuus(osoite: Option[Osoite], aika: Option[Ajanjakso], lisatietoja: Kielistetty)

  @SwaggerModel("""    Osoite:
      |      type: object
      |      properties:
      |        osoite:
      |          type: object
      |          description: Osoite eri kielillä. Kielet on määritetty kielivalinnassa.
      |          allOf:
      |            - $ref: '#/components/schemas/Teksti'
      |        postinumero:
      |          type: string
      |          description: Postinumero
      |          example: "04230"
      |        postitoimipaikka:
      |          type: object
      |          description: Postitoimipaikka eri kielillä. Kielet on määritetty kielivalinnassa.
      |          allOf:
      |            - $ref: '#/components/schemas/Teksti'
      |""")
  case class Osoite(osoite: Option[Kielistetty], postinumero: Option[String], postitoimipaikka: Option[Kielistetty])

  @SwaggerModel("""    Ammattinimike:
      |      type: object
      |      properties:
      |        kieli:
      |          type: string
      |          desciption: Ammattinimikkeen kieli
      |          allOf:
      |            - $ref: '#/components/schemas/Kieli'
      |          example: fi
      |        arvo:
      |          type: string
      |          description: Ammattinimike annetulla kielellä
      |          example: insinööri
      |    Asiasana:
      |      type: object
      |      properties:
      |        kieli:
      |          type: string
      |          desciption: Asiasanan kieli
      |          allOf:
      |            - $ref: '#/components/schemas/Kieli'
      |          example: fi
      |        arvo:
      |          type: string
      |          description: Asiasana annetulla kielellä
      |          example: robotiikka
      |""")
  case class Keyword(kieli: Kieli, arvo: String)

}
