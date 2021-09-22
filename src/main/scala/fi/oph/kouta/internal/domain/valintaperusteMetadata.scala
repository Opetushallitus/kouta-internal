package fi.oph.kouta.internal.domain

import fi.oph.kouta.domain.{Koulutustyyppi, Amm, Amk, Yo, Tuva, VapaaSivistystyoOpistovuosi, VapaaSivistystyoMuu}
import fi.oph.kouta.internal.swagger.SwaggerModel

@SwaggerModel("""    ValintaperusteMetadata:
    |      type: object
    |      properties:
    |        koulutustyyppi:
    |          type: string
    |          description: Valintaperustekuvauksen metatiedon tyyppi
    |          example: yo
    |          enum:
    |            - yo
    |        valintatavat:
    |          type: array
    |          description: Lista valintaperustekuvauksen valintatavoista
    |          items:
    |            $ref: '#/components/schemas/Valintatapa'
    |""")
sealed trait ValintaperusteMetadata {
  def koulutustyyppi: Koulutustyyppi

  def valintatavat: Seq[Valintatapa]
}

@SwaggerModel(
  """    KorkeakoulutusValintaperusteMetadata:
    |      type: object
    |      properties:
    |        valintatavat:
    |          type: array
    |          description: Lista valintaperustekuvauksen valintatavoista
    |          items:
    |            $ref: '#/components/schemas/KorkeakoulutusValintatapa'
    |        kuvaus:
    |          type: object
    |          description: Valintaperustekuvauksen kuvausteksti eri kielillä. Kielet on määritetty koulutuksen kielivalinnassa.
    |          allOf:
    |            - $ref: '#/components/schemas/Kuvaus'
    |"""
)
sealed trait KorkeakoulutusValintaperusteMetadata extends ValintaperusteMetadata {
  def valintatavat: Seq[KorkeakoulutusValintatapa]

  def kuvaus: Kielistetty
}

@SwaggerModel("""    AmmatillinenValintaperusteMetadata:
    |      type: object
    |      allOf:
    |        - $ref: '#/components/schemas/ValintaperusteMetadata'
    |      properties:
    |        valintatavat:
    |          type: array
    |          description: Lista valintaperustekuvauksen valintatavoista
    |          items:
    |            $ref: '#/components/schemas/AmmatillinenValintatapa'
    |        koulutustyyppi:
    |          type: string
    |          description: Valintaperustekuvauksen metatiedon tyyppi
    |          example: amm
    |          enum:
    |            - amm
    |""")
case class AmmatillinenValintaperusteMetadata(
    koulutustyyppi: Koulutustyyppi = Amm,
    valintatavat: Seq[AmmatillinenValintatapa]
) extends ValintaperusteMetadata

@SwaggerModel("""    TuvaValintaperusteMetadata:
                |      type: object
                |      allOf:
                |        - $ref: '#/components/schemas/ValintaperusteMetadata'
                |      properties:
                |        valintatavat:
                |          type: array
                |          description: Lista valintaperustekuvauksen valintatavoista
                |          items:
                |            $ref: '#/components/schemas/AmmatillinenValintatapa'
                |        koulutustyyppi:
                |          type: string
                |          description: Valintaperustekuvauksen metatiedon tyyppi
                |          example: tuva
                |          enum:
                |            - tuva
                |""")
case class TuvaValintaperusteMetadata(
    koulutustyyppi: Koulutustyyppi = Tuva,
    valintatavat: Seq[AmmatillinenValintatapa]
) extends ValintaperusteMetadata

@SwaggerModel("""    VapaaSivistystyoValintaperusteMetadata:
                |      type: object
                |      allOf:
                |        - $ref: '#/components/schemas/ValintaperusteMetadata'
                |      properties:
                |        valintatavat:
                |          type: array
                |          description: Lista valintaperustekuvauksen valintatavoista
                |          items:
                |            $ref: '#/components/schemas/AmmatillinenValintatapa'
                |        koulutustyyppi:
                |          type: string
                |          description: Valintaperustekuvauksen metatiedon tyyppi
                |          example: vapaa-sivistystyo-opistovuosi
                |          enum:
                |            - vapaa-sivistystyo-opistovuosi
                |            - vapaa-sivistystyo-muu
                |""")
case class VapaaSivistystyoValintaperusteMetadata(
    koulutustyyppi: Koulutustyyppi,
    valintatavat: Seq[AmmatillinenValintatapa]
) extends ValintaperusteMetadata

@SwaggerModel("""    YliopistoValintaperusteMetadata:
    |      type: object
    |      allOf:
    |        - $ref: '#/components/schemas/KorkeakoulutusValintaperusteMetadata'
    |      properties:
    |        valintatavat:
    |          type: array
    |          description: Lista valintaperustekuvauksen valintatavoista
    |          items:
    |            $ref: '#/components/schemas/YliopistoValintatapa'
    |        koulutustyyppi:
    |          type: string
    |          description: Valintaperustekuvauksen metatiedon tyyppi
    |          example: yo
    |          enum:
    |            - yo
    |""")
case class YliopistoValintaperusteMetadata(
    koulutustyyppi: Koulutustyyppi = Yo,
    valintatavat: Seq[YliopistoValintatapa],
    kuvaus: Kielistetty = Map()
) extends KorkeakoulutusValintaperusteMetadata

@SwaggerModel("""    AmmattikorkeakouluValintaperusteMetadata:
    |      type: object
    |      allOf:
    |        - $ref: '#/components/schemas/KorkeakoulutusValintaperusteMetadata'
    |      properties:
    |        valintatavat:
    |          type: array
    |          description: Lista valintaperustekuvauksen valintatavoista
    |          items:
    |            $ref: '#/components/schemas/AmmattikorkeakouluValintatapa'
    |        koulutustyyppi:
    |          type: string
    |          description: Valintaperustekuvauksen metatiedon tyyppi
    |          example: amk
    |          enum:
    |            - amk
    |""")
case class AmmattikorkeakouluValintaperusteMetadata(
    koulutustyyppi: Koulutustyyppi = Amk,
    valintatavat: Seq[AmmattikorkeakouluValintatapa],
    kuvaus: Kielistetty = Map()
) extends KorkeakoulutusValintaperusteMetadata
