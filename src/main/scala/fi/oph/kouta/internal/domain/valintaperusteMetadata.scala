package fi.oph.kouta.internal.domain

import fi.oph.kouta.domain.{
  AikuistenPerusopetus,
  Amk,
  Amm,
  AmmMuu,
  AmmOpeErityisopeJaOpo,
  KkOpintojakso,
  Koulutustyyppi,
  Lk,
  Telma,
  Tuva,
  VapaaSivistystyoMuu,
  VapaaSivistystyoOpistovuosi,
  Yo
}
import fi.oph.kouta.internal.swagger.SwaggerModel

@SwaggerModel("""    ValintaperusteMetadata:
    |      type: object
    |      properties:
    |        tyyppi:
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
  def tyyppi: Koulutustyyppi
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
    |        tyyppi:
    |          type: string
    |          description: Valintaperustekuvauksen metatiedon tyyppi
    |          example: amm
    |          enum:
    |            - amm
    |""")
case class AmmatillinenValintaperusteMetadata(
    tyyppi: Koulutustyyppi = Amm,
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
                |        tyyppi:
                |          type: string
                |          description: Valintaperustekuvauksen metatiedon tyyppi
                |          example: tuva
                |          enum:
                |            - tuva
                |""")
case class TuvaValintaperusteMetadata(
    tyyppi: Koulutustyyppi = Tuva,
    valintatavat: Seq[AmmatillinenValintatapa]
) extends ValintaperusteMetadata

@SwaggerModel("""    TelmaValintaperusteMetadata:
                |      type: object
                |      allOf:
                |        - $ref: '#/components/schemas/ValintaperusteMetadata'
                |      properties:
                |        valintatavat:
                |          type: array
                |          description: Lista valintaperustekuvauksen valintatavoista
                |          items:
                |            $ref: '#/components/schemas/AmmatillinenValintatapa'
                |        tyyppi:
                |          type: string
                |          description: Valintaperustekuvauksen metatiedon tyyppi
                |          example: telma
                |          enum:
                |            - telma
                |""")
case class TelmaValintaperusteMetadata(
    tyyppi: Koulutustyyppi = Telma,
    valintatavat: Seq[AmmatillinenValintatapa]
) extends ValintaperusteMetadata

@SwaggerModel("""    AmmatillinenMuuValintaperusteMetadata:
                |      type: object
                |      allOf:
                |        - $ref: '#/components/schemas/ValintaperusteMetadata'
                |      properties:
                |        valintatavat:
                |          type: array
                |          description: Lista valintaperustekuvauksen valintatavoista
                |          items:
                |            $ref: '#/components/schemas/AmmatillinenValintatapa'
                |        tyyppi:
                |          type: string
                |          description: Valintaperustekuvauksen metatiedon tyyppi
                |          example: amm-muu
                |          enum:
                |            - amm-muu
                |""")
case class AmmatillinenMuuValintaperusteMetadata(
    tyyppi: Koulutustyyppi = AmmMuu,
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
                |        tyyppi:
                |          type: string
                |          description: Valintaperustekuvauksen metatiedon tyyppi
                |          example: vapaa-sivistystyo-opistovuosi
                |          enum:
                |            - vapaa-sivistystyo-opistovuosi
                |            - vapaa-sivistystyo-muu
                |""")
case class VapaaSivistystyoValintaperusteMetadata(
    tyyppi: Koulutustyyppi,
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
    |        tyyppi:
    |          type: string
    |          description: Valintaperustekuvauksen metatiedon tyyppi
    |          example: yo
    |          enum:
    |            - yo
    |""")
case class YliopistoValintaperusteMetadata(
    tyyppi: Koulutustyyppi = Yo,
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
    |        tyyppi:
    |          type: string
    |          description: Valintaperustekuvauksen metatiedon tyyppi
    |          example: amk
    |          enum:
    |            - amk
    |""")
case class AmmattikorkeakouluValintaperusteMetadata(
    tyyppi: Koulutustyyppi = Amk,
    valintatavat: Seq[AmmattikorkeakouluValintatapa],
    kuvaus: Kielistetty = Map()
) extends KorkeakoulutusValintaperusteMetadata

@SwaggerModel("""    AmmOpeErityisopeJaOpoValintaperusteMetadata:
                |      type: object
                |      allOf:
                |        - $ref: '#/components/schemas/KorkeakoulutusValintaperusteMetadata'
                |      properties:
                |        valintatavat:
                |          type: array
                |          description: Lista valintaperustekuvauksen valintatavoista
                |          items:
                |            $ref: '#/components/schemas/AmmOpeErityisopeJaOpoValintatapa'
                |        tyyppi:
                |          type: string
                |          description: Valintaperustekuvauksen metatiedon tyyppi
                |          example: amm-ope-erityisope-ja-opo
                |          enum:
                |            - amm-ope-erityisope-ja-opo
                |""")
case class AmmOpeErityisopeJaOpoValintaperusteMetadata(
    tyyppi: Koulutustyyppi = AmmOpeErityisopeJaOpo,
    valintatavat: Seq[AmmOpeErityisopeJaOpoValintatapa],
    kuvaus: Kielistetty = Map()
) extends KorkeakoulutusValintaperusteMetadata

@SwaggerModel("""    KkOpintojaksoValintaperusteMetadata:
                |      type: object
                |      allOf:
                |        - $ref: '#/components/schemas/KkOpintojaksoValintaperusteMetadata'
                |      properties:
                |        valintatavat:
                |          type: array
                |          description: Lista valintaperustekuvauksen valintatavoista
                |          items:
                |            $ref: '#/components/schemas/KorkeakoulutusValintatapa'
                |        tyyppi:
                |          type: string
                |          description: Valintaperustekuvauksen metatiedon tyyppi
                |          example: kk-opintojakso
                |          enum:
                |            - kk-opintojakso
                |""")
case class KkOpintojaksoValintaperusteMetadata(
    tyyppi: Koulutustyyppi = KkOpintojakso,
    valintatavat: Seq[KkOpintojaksoValintatapa],
    kuvaus: Kielistetty = Map()
) extends KorkeakoulutusValintaperusteMetadata

@SwaggerModel("""    LukioValintaperusteMetadata:
    |      type: object
    |      allOf:
    |        - $ref: '#/components/schemas/ValintaperusteMetadata'
    |      properties:
    |        tyyppi:
    |          type: string
    |          description: Valintaperustekuvauksen metatiedon tyyppi
    |          example: lk
    |          enum:
    |            - lk
    |""")
case class LukioValintaperusteMetadata(
    tyyppi: Koulutustyyppi = Lk,
    valintatavat: Seq[LukioValintatapa],
    kuvaus: Kielistetty = Map()
) extends ValintaperusteMetadata

@SwaggerModel("""    AikuistenPerusopetusValintaperusteMetadata:
                |      type: object
                |      allOf:
                |        - $ref: '#/components/schemas/ValintaperusteMetadata'
                |      properties:
                |        valintatavat:
                |          type: array
                |          description: Lista valintaperustekuvauksen valintatavoista
                |          items:
                |            $ref: '#/components/schemas/AmmatillinenValintatapa'
                |        tyyppi:
                |          type: string
                |          description: Valintaperustekuvauksen metatiedon tyyppi
                |          example: aikuisten-perusopetus
                |          enum:
                |            - aikuisten-perusopetus
                |""")
case class AikuistenPerusopetusValintaperusteMetadata(
    tyyppi: Koulutustyyppi = AikuistenPerusopetus,
    valintatavat: Seq[AmmatillinenValintatapa]
) extends ValintaperusteMetadata
