package fi.oph.kouta.external.domain

case class HakuMetadata(yhteyshenkilo: Option[Yhteyshenkilo], tulevaisuudenAikataulu: Seq[Ajanjakso])
