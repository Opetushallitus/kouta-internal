package fi.oph.kouta.internal.domain

import fi.oph.kouta.domain.{AmmMuu, Koulutustyyppi}
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
    |            tyyppi:
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

@SwaggerModel("""    AmmatillinenTutkinnonOsaKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: amm-tutkinnon-osa
    |              enum:
    |                - amm-tutkinnon-osa
    |            tutkinnonOsat:
    |              type: array
    |              description: Tutkinnon osat
    |              items:
    |                type: object
    |                $ref: '#/components/schemas/TutkinnonOsa'
    |""")
case class AmmatillinenTutkinnonOsaKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    tutkinnonOsat: Seq[TutkinnonOsa]
) extends KoulutusMetadata

@SwaggerModel(
  """    TutkinnonOsa:
    |      type: object
    |      properties:
    |        ePerusteId:
    |          type: number
    |          description: Tutkinnon osan käyttämän ePerusteen id.
    |          example: 4804100
    |        koulutusKoodiUri:
    |          type: string
    |          description: Koulutuksen koodi URI. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/koulutus/11)
    |          example: koulutus_371101#1
    |        tutkinnonosaId:
    |          type: number
    |          description: Tutkinnon osan id ePerusteissa
    |          example: 12345
    |        tutkinnonosaViite:
    |          type: number
    |          description: Tutkinnon osan viite
    |          example: 2449201
    |"""
)
case class TutkinnonOsa(
    ePerusteId: Option[Long],
    koulutusKoodiUri: Option[String],
    tutkinnonosaId: Option[Long],
    tutkinnonosaViite: Option[Long]
)

@SwaggerModel(
  """    AmmatillinenOsaamisalaKoulutusMetadata:
      |      allOf:
      |        - $ref: '#/components/schemas/KoulutusMetadata'
      |        - type: object
      |          properties:
      |            tyyppi:
      |              type: string
      |              description: Koulutuksen metatiedon tyyppi
      |              example: amm-osaamisala
      |              enum:
      |                - amm-osaamisala
      |            tyyppi:
      |              type: string
      |              description: Koulutuksen metatiedon tyyppi
      |              example: amm-osaamisala
      |              enum:
      |                - amm-osaamisala
      |            osaamisalaKoodiUri:
      |              type: string
      |              description: Osaamisala. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/osaamisala/1)
      |              example: osaamisala_10#1
      |"""
)
case class AmmatillinenOsaamisalaKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    osaamisalaKoodiUri: Option[String]
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
    |            tyyppi:
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
    |            tyyppi:
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

@SwaggerModel("""    AmmOpeErityisopeJaOpoKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KorkeakouluMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: amm-ope-erityisope-ja-opo
    |              enum:
    |                - amm-ope-erityisope-ja-opo
    |""")
case class AmmOpeErityisopeJaOpoKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    koulutusalaKoodiUrit: Seq[String],
    tutkintonimikeKoodiUrit: Seq[String],
    opintojenLaajuusKoodiUri: Option[String],
    kuvauksenNimi: Kielistetty
) extends KorkeakoulutusKoulutusMetadata

@SwaggerModel(
  """    KkOpintojaksoKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: kk-opintojakso
    |              enum:
    |                - kk-opintojakso
    |            koulutusalaKoodiUrit:
    |              type: array
    |              description: Lista koulutusaloja. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kansallinenkoulutusluokitus2016koulutusalataso1/1)
    |              items:
    |                type: string
    |                example:
    |                  - kansallinenkoulutusluokitus2016koulutusalataso1_001#1
    |            opintojenLaajuusyksikkoKoodiUri:
    |              type: string
    |              description: "Opintojen laajuusyksikko. Viittaa koodistoon [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/opintojenlaajuusyksikko/1)"
    |              example: opintojenlaajuusyksikko_6#1
    |            opintojenLaajuusnumero:
    |              type: double
    |              description: Opintojen laajuus tai kesto numeroarvona
    |              example: 10
    |"""
)
case class KkOpintojaksoKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    koulutusalaKoodiUrit: Seq[String],
    opintojenLaajuusnumero: Option[Double],
    opintojenLaajuusyksikkoKoodiUri: Option[String],
    kuvauksenNimi: Kielistetty
) extends KoulutusMetadata

@SwaggerModel(
  """    LukioKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: lk
    |              enum:
    |                - lk
    |            opintojenLaajuusKoodiUri:
    |              type: string
    |              description: "Tutkinnon laajuus. Viittaa koodistoon [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/opintojenlaajuus/1)"
    |              example: opintojenlaajuus_40#1
    |            koulutusalaKoodiUrit:
    |              type: array
    |              description: Lista koulutusaloja. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kansallinenkoulutusluokitus2016koulutusalataso1/1)
    |              items:
    |                type: string
    |                example:
    |                  - kansallinenkoulutusluokitus2016koulutusalataso1_001#1
    |"""
)
case class LukioKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    opintojenLaajuusKoodiUri: Option[String],
    koulutusalaKoodiUrit: Seq[String]
) extends KoulutusMetadata

@SwaggerModel(
  """    TuvaKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: tuva
    |              enum:
    |                - tuva
    |            linkkiEPerusteisiin:
    |              type: object
    |              description: Linkit koulutuksen käyttämiin ePerusteisiin, eri kielisiin versioihin. Kielet on määritetty koulutuksen kielivalinnassa.
    |            opintojenLaajuusKoodiUri:
    |              type: string
    |              description: "Tutkinnon laajuus. Viittaa koodistoon [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/opintojenlaajuus/1)"
    |              example: opintojenlaajuus_38#1
    |"""
)
case class TuvaKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    linkkiEPerusteisiin: Kielistetty,
    opintojenLaajuusKoodiUri: Option[String] = None
) extends KoulutusMetadata

@SwaggerModel(
  """    TelmaKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: telma
    |              enum:
    |                - telma
    |            linkkiEPerusteisiin:
    |              type: object
    |              description: Linkit koulutuksen käyttämiin ePerusteisiin, eri kielisiin versioihin. Kielet on määritetty koulutuksen kielivalinnassa.
    |            opintojenLaajuusKoodiUri:
    |              type: string
    |              description: "Tutkinnon laajuus. Viittaa koodistoon [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/opintojenlaajuus/1)"
    |              example: opintojenlaajuus_38#1
    |"""
)
case class TelmaKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    linkkiEPerusteisiin: Kielistetty,
    opintojenLaajuusKoodiUri: Option[String] = None
) extends KoulutusMetadata

@SwaggerModel(
  """    AmmatillinenMuuKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: amm-muu
    |              enum:
    |                - amm-muu
    |            koulutusalaKoodiUrit:
    |              type: array
    |              description: Lista koulutusaloja. Viittaa [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/kansallinenkoulutusluokitus2016koulutusalataso1/1)
    |              items:
    |                type: string
    |                example:
    |                  - kansallinenkoulutusluokitus2016koulutusalataso1_001#1
    |            opintojenLaajuusyksikkoKoodiUri:
    |              type: string
    |              description: "Opintojen laajuusyksikko. Viittaa koodistoon [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/opintojenlaajuusyksikko/1)"
    |              example: opintojenlaajuusyksikko_6#1
    |            opintojenLaajuusnumero:
    |              type: double
    |              description: Opintojen laajuus tai kesto numeroarvona
    |              example: 10
    |"""
)
case class AmmatillinenMuuKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    koulutusalaKoodiUrit: Seq[String] = Seq(),
    opintojenLaajuusyksikkoKoodiUri: Option[String] = None,
    opintojenLaajuusNumero: Option[Double] = None
) extends KoulutusMetadata

@SwaggerModel(
  """    VapaaSivistystyoKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: vapaa-sivistystyo-muu
    |              enum:
    |                - vapaa-sivistystyo-opistovuosi
    |                - vapaa-sivistystyo-muu
    |            linkkiEPerusteisiin:
    |              type: object
    |              description: Linkit koulutuksen käyttämiin ePerusteisiin, eri kielisiin versioihin. Kielet on määritetty koulutuksen kielivalinnassa.
    |            opintojenLaajuusKoodiUri:
    |              type: string
    |              description: "Tutkinnon laajuus. Viittaa koodistoon [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/opintojenlaajuus/1)"
    |              example: opintojenlaajuus_60#1
    |"""
)
case class VapaaSivistystyoKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    linkkiEPerusteisiin: Kielistetty,
    opintojenLaajuusKoodiUri: Option[String] = None
) extends KoulutusMetadata

@SwaggerModel(
  """    AikuistenPerusopetusKoulutusMetadata:
    |      allOf:
    |        - $ref: '#/components/schemas/KoulutusMetadata'
    |        - type: object
    |          properties:
    |            tyyppi:
    |              type: string
    |              description: Koulutuksen metatiedon tyyppi
    |              example: aikuisten-perusopetus
    |              enum:
    |                - aikuisten-perusopetus
    |            linkkiEPerusteisiin:
    |              type: object
    |              description: Linkit koulutuksen käyttämiin ePerusteisiin, eri kielisiin versioihin. Kielet on määritetty koulutuksen kielivalinnassa.
    |            opintojenLaajuusyksikkoKoodiUri:
    |              type: string
    |              description: "Opintojen laajuusyksikko. Viittaa koodistoon [koodistoon](https://virkailija.testiopintopolku.fi/koodisto-ui/html/koodisto/opintojenlaajuusyksikko/1)"
    |              example: opintojenlaajuusyksikko_6#1
    |            opintojenLaajuusnumero:
    |              type: double
    |              description: Opintojen laajuus tai kesto numeroarvona
    |              example: 10
    |"""
)
case class AikuistenPerusopetusKoulutusMetadata(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[Lisatieto],
    linkkiEPerusteisiin: Kielistetty,
    opintojenLaajuusyksikkoKoodiUri: Option[String] = None,
    opintojenLaajuusNumero: Option[Double] = None
) extends KoulutusMetadata
