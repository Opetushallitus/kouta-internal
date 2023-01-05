package fi.oph.kouta.internal.domain

import fi.oph.kouta.domain.{
  AmmOpeErityisopeJaOpo,
  Erikoislaakari,
  Erikoistumiskoulutus,
  Hakutermi,
  KkOpintojakso,
  KkOpintokokonaisuus,
  Koulutustyyppi,
  OpePedagOpinnot,
  Telma,
  Tuva,
  VapaaSivistystyoMuu,
  VapaaSivistystyoOpistovuosi
}
import fi.oph.kouta.internal.domain.enums.Hakulomaketyyppi
import fi.oph.kouta.internal.swagger.SwaggerModel

@SwaggerModel(
  """    ToteutusMetadata:
    |      type: object
    |      properties:
    |        kuvaus:
    |          type: object
    |          description: Toteutuksen kuvausteksti eri kielillä. Kielet on määritetty toteutuksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Kuvaus'
    |        opetus:
    |          type: object
    |          $ref: '#/components/schemas/Opetus'
    |        yhteyshenkilot:
    |          type: array
    |          description: Toteutuksen yhteyshenkilöiden tiedot
    |          items:
    |            $ref: '#/components/schemas/Yhteyshenkilo'
    |        asiasanat:
    |          type: array
    |          description: Lista toteutukseen liittyvistä asiasanoista, joiden avulla opiskelija voi hakea koulutusta Opintopolusta
    |          items:
    |            $ref: '#/components/schemas/Asiasana'
    |        ammattinimikkeet:
    |          type: array
    |          description: Lista toteutukseen liittyvistä ammattinimikkeistä, joiden avulla opiskelija voi hakea koulutusta Opintopolusta
    |          items:
    |            $ref: '#/components/schemas/Ammattinimike'
    |"""
)
sealed trait ToteutusMetadata {
  val tyyppi: Koulutustyyppi
  val kuvaus: Kielistetty
  val opetus: Option[Opetus]
  val asiasanat: List[Keyword]
  val ammattinimikkeet: List[Keyword]
  val yhteyshenkilot: Seq[Yhteyshenkilo]
}

@SwaggerModel("""    AmmatillinenToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/ToteutusMetadata'
    |        - type: object
    |          properties:
    |            osaamisalat:
    |              type: array
    |              items:
    |                $ref: '#/components/schemas/Osaamisala'
    |              description: Lista ammatillisen koulutuksen osaamisalojen kuvauksia
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: amm
    |              enum:
    |                - amm
    |""")
case class AmmatillinenToteutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    osaamisalat: List[AmmatillinenOsaamisala],
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo]
) extends ToteutusMetadata

@SwaggerModel(
  """    TutkintoonJohtamatonToteutusMetadata:
    |           allOf:
    |             - $ref: '#/components/schemas/ToteutusMetadata'
    |             - type: object
    |               properties:
    |                 hakutermi:
    |                   type: object
    |                   $ref: '#/components/schemas/HakutermiSwagger'
    |                 hakulomaketyyppi:
    |                   type: string
    |                   description: Hakulomakkeen tyyppi. Kertoo, käytetäänkö Atarun (hakemuspalvelun) hakulomaketta, muuta hakulomaketta
    |                     (jolloin voidaan lisätä hakulomakkeeseen linkki) tai onko niin, ettei sähkököistä hakulomaketta ole lainkaan, jolloin sille olisi hyvä lisätä kuvaus.
    |                   example: "ataru"
    |                   enum:
    |                     - ataru
    |                     - haku-app
    |                     - ei sähköistä
    |                     - muu
    |                 hakulomakeLinkki:
    |                   type: object
    |                   description: Hakulomakkeen linkki eri kielillä. Kielet on määritetty haun kielivalinnassa.
    |                   $ref: '#/components/schemas/Linkki'
    |                 lisatietoaHakeutumisesta:
    |                   type: object
    |                   description: Lisätietoa hakeutumisesta eri kielillä. Kielet on määritetty haun kielivalinnassa.
    |                   $ref: '#/components/schemas/Teksti'
    |                 lisatietoaValintaperusteista:
    |                   type: object
    |                   description: Lisätietoa valintaperusteista eri kielillä. Kielet on määritetty haun kielivalinnassa.
    |                   $ref: '#/components/schemas/Teksti'
    |                 hakuaika:
    |                   type: array
    |                   description: Toteutuksen hakuaika
    |                   $ref: '#/components/schemas/Ajanjakso'
    |                 aloituspaikat:
    |                   type: integer
    |                   description: Toteutuksen aloituspaikkojen lukumäärä
    |                   example: 100
    |"""
)
trait TutkintoonJohtamatonToteutusMetadata extends ToteutusMetadata {
  val hakutermi: Option[Hakutermi]
  val hakulomaketyyppi: Option[Hakulomaketyyppi]
  val hakulomakeLinkki: Kielistetty
  val lisatietoaHakeutumisesta: Kielistetty
  val lisatietoaValintaperusteista: Kielistetty
  val hakuaika: Option[Ajanjakso]
  val aloituspaikat: Option[Int]
}

@SwaggerModel("""    AmmatillinenTutkinnonOsaToteutusMetadata:
    |           allOf:
    |             - $ref: '#/components/schemas/TutkintoonJohtamatonToteutusMetadata'
    |             - type: object
    |               properties:
    |                 tyyppi:
    |                   type: string
    |                   description: Koulutuksen metatiedon tyyppi
    |                   example: amm-tutkinnon-osa
    |                   enum:
    |                     - amm-tutkinnon-osa
    |""")
case class AmmatillinenTutkinnonOsaToteutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int]
) extends TutkintoonJohtamatonToteutusMetadata

@SwaggerModel("""    AmmatillinenOsaamisalaToteutusMetadata:
    |            allOf:
    |              - $ref: '#/components/schemas/TutkintoonJohtamatonToteutusMetadata'
    |              - type: object
    |                properties:
    |                  tyyppi:
    |                    type: string
    |                    description: Koulutuksen metatiedon tyyppi
    |                    example: amm-osaamisala
    |                    enum:
    |                      - amm-osaamisala
    |""")
case class AmmatillinenOsaamisalaToteutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int]
) extends TutkintoonJohtamatonToteutusMetadata

@SwaggerModel("""    AmmatillinenMuuToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/TutkintoonJohtamatonToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: amm-muu
    |              enum:
    |                - amm-muu
    |""")
case class AmmatillinenMuuToteutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int]
) extends TutkintoonJohtamatonToteutusMetadata

@SwaggerModel("""    YliopistoToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/ToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: yo
    |              enum:
    |                - yo
    |""")
case class YliopistoToteutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo]
) extends ToteutusMetadata

@SwaggerModel("""    AmmattikorkeaToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/ToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: amk
    |              enum:
    |                - amk
    |""")
case class AmmattikorkeakouluToteutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo]
) extends ToteutusMetadata

@SwaggerModel("""    AmmOpeErityisopeJaOpoToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/ToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: amm-ope-erityisope-ja-opo
    |              enum:
    |                - amm-ope-erityisope-ja-opo
    |""")
case class AmmOpeErityisopeJaOpoToteutusMetadata(
    tyyppi: Koulutustyyppi = AmmOpeErityisopeJaOpo,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    aloituspaikat: Option[Int]
) extends ToteutusMetadata

@SwaggerModel("""    OpePedagOpinnotToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/ToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: amm-ope-erityisope-ja-opo
    |              enum:
    |                - amm-ope-erityisope-ja-opo
    |""")
case class OpePedagOpinnotToteutusMetadata(
    tyyppi: Koulutustyyppi = OpePedagOpinnot,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    aloituspaikat: Option[Int]
) extends ToteutusMetadata

@SwaggerModel("""    KkOpintojaksoToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/TutkintoonJohtamatonToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: kk-opintojakso
    |              enum:
    |                - kk-opintojakso
    |""")
case class KkOpintojaksoToteutusMetadata(
    tyyppi: Koulutustyyppi = KkOpintojakso,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int]
) extends TutkintoonJohtamatonToteutusMetadata

@SwaggerModel("""    TuvaToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/ToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: tuva
    |              enum:
    |                - tuva
    |            jarjestetaanErityisopetuksena:
    |              type: boolean
    |              description: Tieto siitä järjestetäänkö toteutus erityisopetuksena
    |""")
case class TuvaToteutusMetadata(
    tyyppi: Koulutustyyppi = Tuva,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    aloituspaikat: Option[Int],
    jarjestetaanErityisopetuksena: Boolean
) extends ToteutusMetadata

@SwaggerModel("""    TelmaToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/ToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: telma
    |              enum:
    |                - telma
    |""")
case class TelmaToteutusMetadata(
    tyyppi: Koulutustyyppi = Telma,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    aloituspaikat: Option[Int]
) extends ToteutusMetadata

@SwaggerModel("""    VapaaSivistystyoOpistovuosiToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/ToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: vapaa-sivistystyo-opistovuosi
    |              enum:
    |                - vapaa-sivistystyo-opistovuosi
    |""")
case class VapaaSivistystyoOpistovuosiToteutusMetadata(
    tyyppi: Koulutustyyppi = VapaaSivistystyoOpistovuosi,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo]
) extends ToteutusMetadata

@SwaggerModel("""    VapaaSivistystyoMuuToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/TutkintoonJohtamatonToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: vapaa-sivistystyo-muu
    |              enum:
    |                - vapaa-sivistystyo-muu
    |""")
case class VapaaSivistystyoMuuToteutusMetadata(
    tyyppi: Koulutustyyppi = VapaaSivistystyoMuu,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int]
) extends TutkintoonJohtamatonToteutusMetadata

@SwaggerModel(
  """    Kielivalikoima:
    |      type: object
    |      properties:
    |        A1Kielet:
    |          type: array
    |          description: Lista koulutuksen toteutuksen A1 kielistä. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kieli/1)
    |          items:
    |            type: string
    |            example:
    |              - kieli_EN#1
    |              - kieli_FI#1
    |        A2Kielet:
    |          type: array
    |          description: Lista koulutuksen toteutuksen A2 kielistä. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kieli/1)
    |          items:
    |            type: string
    |            example:
    |              - kieli_EN#1
    |              - kieli_FI#1
    |        B1Kielet:
    |          type: array
    |          description: Lista koulutuksen toteutuksen B1 kielistä. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kieli/1)
    |          items:
    |            type: string
    |            example:
    |              - kieli_EN#1
    |              - kieli_FI#1
    |        B2Kielet:
    |          type: array
    |          description: Lista koulutuksen toteutuksen B2 kielistä. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kieli/1)
    |          items:
    |            type: string
    |            example:
    |              - kieli_EN#1
    |              - kieli_FI#1
    |        B3Kielet:
    |          type: array
    |          description: Lista koulutuksen toteutuksen B3 kielistä. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kieli/1)
    |          items:
    |            type: string
    |            example:
    |              - kieli_EN#1
    |              - kieli_FI#1
    |        aidinkielet:
    |          type: array
    |          description: Lista koulutuksen toteutuksen äidinkielistä. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kieli/1)
    |          items:
    |            type: string
    |            example:
    |              - kieli_EN#1
    |              - kieli_FI#1
    |        muutKielet:
    |          type: array
    |          description: Lista koulutuksen toteutuksen muista kielistä. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kieli/1)
    |          items:
    |            type: string
    |            example:
    |              - kieli_EN#1
    |              - kieli_FI#1
    |"""
)
case class Kielivalikoima(
    A1Kielet: Seq[String] = Seq(),
    A2Kielet: Seq[String] = Seq(),
    B1Kielet: Seq[String] = Seq(),
    B2Kielet: Seq[String] = Seq(),
    B3Kielet: Seq[String] = Seq(),
    aidinkielet: Seq[String] = Seq(),
    muutKielet: Seq[String] = Seq()
)

@SwaggerModel(
  """    LukiolinjaTieto:
    |      type: object
    |      description: Toteutuksen yksittäisen lukiolinjatiedon kentät
    |      properties:
    |        koodiUri:
    |          type: string
    |          description: Lukiolinjatiedon koodiUri.
    |        kuvaus:
    |          type: object
    |          description: Lukiolinjatiedon kuvaus eri kielillä. Kielet on määritetty toteutuksen kielivalinnassa.
    |          $ref: '#/components/schemas/Kuvaus'
    |"""
)
case class LukiolinjaTieto(koodiUri: String, kuvaus: Kielistetty)

@SwaggerModel(
  """    LukiodiplomiTieto:
    |      type: object
    |      description: Toteutuksen yksittäisen lukiodiplomitiedon kentät
    |      properties:
    |        koodiUri:
    |          type: string
    |          description: Lukiodiplomin koodiUri. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/moduulikoodistolops2021/1).
    |        linkki:
    |          type: object
    |          description: Lukiodiplomin kielistetyt lisätietolinkit. Kielet on määritetty toteutuksen kielivalinnassa.
    |          $ref: '#/components/schemas/Linkki'
    |        linkinAltTeksti:
    |          type: object
    |          description: Lukiodiplomin kielistettyjen lisätietolinkkien alt-tekstit. Kielet on määritetty toteutuksen kielivalinnassa.
    |          $ref: '#/components/schemas/Teksti'
    |"""
)
case class LukiodiplomiTieto(koodiUri: String, linkki: Kielistetty, linkinAltTeksti: Kielistetty)

@SwaggerModel(
  """    LukioToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/ToteutusMetadata'
    |        - type: object
    |          properties:
    |            koulutustyyppi:
    |              type: string
    |              description: Toteutuksen metatiedon tyyppi
    |              example: lk
    |              enum:
    |                - lk
    |            kielivalikoima:
    |              type: object
    |              description: Koulutuksen kielivalikoima
    |              $ref: '#/components/schemas/Kielivalikoima'
    |            yleislinja:
    |              type: boolean,
    |              description: Onko lukio-toteutuksella yleislinja?
    |            painotukset:
    |              type: array
    |              description: Lukio-toteutuksen painotukset. Taulukon alkioiden koodiUri-kentät viittaavat [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/lukiopainotukset/1).
    |              items:
    |                type: object
    |                $ref: '#/components/schemas/LukiolinjaTieto'
    |            erityisetKoulutustehtavat:
    |              type: array
    |              description: Lukio-toteutuksen erityiset koulutustehtävät. Taulukon alkioiden koodiUri-kentät viittaavat [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/lukiolinjaterityinenkoulutustehtava/1).
    |              items:
    |                type: object
    |                $ref: '#/components/schemas/LukiolinjaTieto'
    |            diplomit:
    |              type: array
    |              description: Lukio-toteutuksen diplomit
    |              items:
    |                type: object
    |                $ref: '#/components/schemas/LukiodiplomiTieto'
    |"""
)
case class LukioToteutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    kielivalikoima: Option[Kielivalikoima],
    yleislinja: Boolean,
    painotukset: Seq[LukiolinjaTieto],
    erityisetKoulutustehtavat: Seq[LukiolinjaTieto],
    diplomit: Seq[LukiodiplomiTieto]
) extends ToteutusMetadata

@SwaggerModel("""    AikuistenPerusopetusToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/TutkintoonJohtamatonToteutusMetadata'
    |        - type: object
    |          properties:
    |            koulutustyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: aikuisten-perusopetus
    |              enum:
    |                - aikuisten-perusopetus
    |""")
case class AikuistenPerusopetusToteutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int]
) extends TutkintoonJohtamatonToteutusMetadata

@SwaggerModel("""    ErikoislaakariToteutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/ToteutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: erikoislaakari
    |              enum:
    |                - erikoislaakari
    |""")
case class ErikoislaakariToteutusMetadata(
    tyyppi: Koulutustyyppi = Erikoislaakari,
    kuvaus: Kielistetty = Map(),
    opetus: Option[Opetus] = None,
    asiasanat: List[Keyword] = List(),
    ammattinimikkeet: List[Keyword] = List(),
    yhteyshenkilot: Seq[Yhteyshenkilo] = Seq()
) extends ToteutusMetadata

@SwaggerModel(
  """    KkOpintokokonaisuusToteutusMetadata:
  |      allOf:
  |        - $ref: '#/components/schemas/TutkintoonJohtamatonToteutusMetadata'
  |        - type: object
  |          properties:
  |            tyyppi:
  |              type: string
  |              description: Koulutuksen metatiedon tyyppi
  |              example: kk-opintokokonaisuus
  |              enum:
  |                - kk-opintokokonaisuus
  |            opintojenLaajuusyksikkoKoodiUri:
  |              type: string
  |              description: "Opintojen laajuusyksikko. Viittaa koodistoon [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/opintojenlaajuusyksikko/1)"
  |              example: opintojenlaajuusyksikko_6#1
  |            opintojenLaajuusNumero:
  |              type: integer
  |              description: Opintojen laajuus tai kesto numeroarvona
  |              example: 10
  |"""
)
case class KkOpintokokonaisuusToteutusMetadata(
    tyyppi: Koulutustyyppi = KkOpintokokonaisuus,
    kuvaus: Kielistetty,
    opintojenLaajuusyksikkoKoodiUri: Option[String],
    opintojenLaajuusNumero: Option[Double],
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int]
) extends TutkintoonJohtamatonToteutusMetadata

@SwaggerModel(
  """    ErikoistumiskoulutusToteutusMetadata:
  |      allOf:
  |        - $ref: '#/components/schemas/TutkintoonJohtamatonToteutusMetadata'
  |        - type: object
  |          properties:
  |            tyyppi:
  |              type: string
  |              description: Koulutuksen metatiedon tyyppi
  |              example: erikoistumiskoulutus
  |              enum:
  |                - erikoistumiskoulutus
  |"""
)
case class ErikoistumiskoulutusToteutusMetadata(
    tyyppi: Koulutustyyppi = Erikoistumiskoulutus,
    kuvaus: Kielistetty,
    opetus: Option[Opetus],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int]
) extends TutkintoonJohtamatonToteutusMetadata
