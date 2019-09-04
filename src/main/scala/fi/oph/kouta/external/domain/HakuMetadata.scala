package fi.oph.kouta.external.domain

import fi.oph.kouta.external.swagger.SwaggerModel

@SwaggerModel(
  """    HakuMetadata:
    |      type: object
    |      properties:
    |        yhteyshenkilo:
    |          type: object
    |          description: Haun yhteyshenkilön tiedot
    |          allOf:
    |            - $ref: '#/components/schemas/Yhteyshenkilo'
    |        tulevaisuudenAikataulu:
    |          type: array
    |          description: Oppijalle Opintopolussa näytettävät haun mahdolliset tulevat hakuajat
    |          items:
    |            $ref: '#/components/schemas/Ajanjakso'
    |""")
case class HakuMetadata(yhteyshenkilo: Option[Yhteyshenkilo], tulevaisuudenAikataulu: Seq[Ajanjakso])
