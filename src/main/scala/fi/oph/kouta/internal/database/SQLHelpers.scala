package fi.oph.kouta.internal.database

import java.sql.JDBCType
import java.time.{Instant, LocalDateTime, OffsetDateTime, ZoneId}
import java.util.UUID

import fi.oph.kouta.internal.domain.Ajanjakso
import fi.oph.kouta.internal.domain.oid._
import fi.oph.kouta.internal.util.KoutaJsonFormats
import fi.vm.sade.utils.slf4j.Logging
import slick.jdbc.{PositionedParameters, SetParameter}

trait SQLHelpers extends KoutaJsonFormats with Logging {
  implicit object SetUUID extends SetParameter[UUID] {
    def apply(v: UUID, pp: PositionedParameters) {
      pp.setObject(v, JDBCType.BINARY.getVendorTypeNumber)
    }
  }
}
