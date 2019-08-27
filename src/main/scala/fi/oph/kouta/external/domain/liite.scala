package fi.oph.kouta.external.domain

import java.time.LocalDateTime
import java.util.UUID

import fi.oph.kouta.external.domain.enums.LiitteenToimitustapa

case class Liite(
    id: Option[UUID],
    tyyppi: Option[String],
    nimi: Kielistetty,
    kuvaus: Kielistetty,
    toimitusaika: Option[LocalDateTime],
    toimitustapa: Option[LiitteenToimitustapa],
    toimitusosoite: Option[LiitteenToimitusosoite]
)

case class LiitteenToimitusosoite(osoite: Osoite, sahkoposti: Option[String])
