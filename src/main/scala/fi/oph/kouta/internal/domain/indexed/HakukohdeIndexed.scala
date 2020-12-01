package fi.oph.kouta.internal.domain.indexed

import java.time.LocalDateTime
import java.util.UUID

import fi.oph.kouta.internal.domain.enums.{Hakulomaketyyppi, Julkaisutila, Kieli, LiitteenToimitustapa}
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid, ToteutusOid}
import fi.oph.kouta.internal.domain.{
  Ajanjakso,
  Hakukohde,
  Kielistetty,
  Liite,
  LiitteenToimitusosoite,
  WithTila,
  YhdenPaikanSaanto
}
import fi.vm.sade.utils.slf4j.Logging

case class HakukohdeToteutusIndexed(oid: ToteutusOid, tarjoajat: List[Organisaatio])

case class HakukohdeIndexed(
    oid: HakukohdeOid,
    toteutus: HakukohdeToteutusIndexed,
    hakuOid: HakuOid,
    tila: Julkaisutila,
    nimi: Kielistetty,
    alkamiskausi: Option[KoodiUri],
    alkamisvuosi: Option[String],
    kaytetaanHaunAlkamiskautta: Option[Boolean],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeAtaruId: Option[UUID],
    hakulomakeKuvaus: Kielistetty,
    hakulomakeLinkki: Kielistetty,
    kaytetaanHaunHakulomaketta: Option[Boolean],
    aloituspaikat: Option[Int],
    minAloituspaikat: Option[Int],
    maxAloituspaikat: Option[Int],
    ensikertalaisenAloituspaikat: Option[Int],
    minEnsikertalaisenAloituspaikat: Option[Int],
    maxEnsikertalaisenAloituspaikat: Option[Int],
    pohjakoulutusvaatimus: Seq[KoodiUri],
    muuPohjakoulutusvaatimus: Kielistetty,
    toinenAsteOnkoKaksoistutkinto: Option[Boolean],
    kaytetaanHaunAikataulua: Option[Boolean],
    valintaperuste: Option[UuidObject],
    yhdenPaikanSaanto: YhdenPaikanSaanto,
    liitteetOnkoSamaToimitusaika: Option[Boolean],
    liitteetOnkoSamaToimitusosoite: Option[Boolean],
    liitteidenToimitusaika: Option[LocalDateTime],
    liitteidenToimitustapa: Option[LiitteenToimitustapa],
    liitteidenToimitusosoite: Option[LiitteenToimitusosoite],
    liitteet: List[Liite],
    valintakokeet: List[ValintakoeIndexed],
    hakuajat: List[Ajanjakso],
    muokkaaja: Muokkaaja,
    jarjestyspaikka: Option[Organisaatio],
    organisaatio: Option[Organisaatio],
    kielivalinta: Seq[Kieli],
    modified: Option[LocalDateTime]
) extends WithTila
    with Logging {
  def toHakukohde: Hakukohde = {
    try {
      Hakukohde(
        oid = oid,
        toteutusOid = toteutus.oid,
        hakuOid = hakuOid,
        tila = tila,
        nimi = nimi,
        alkamiskausiKoodiUri = alkamiskausi.map(_.koodiUri),
        alkamisvuosi = alkamisvuosi,
        kaytetaanHaunAlkamiskautta = kaytetaanHaunAlkamiskautta,
        hakulomaketyyppi = hakulomaketyyppi,
        hakulomakeAtaruId = hakulomakeAtaruId,
        hakulomakeKuvaus = hakulomakeKuvaus,
        hakulomakeLinkki = hakulomakeLinkki,
        kaytetaanHaunHakulomaketta = kaytetaanHaunHakulomaketta,
        aloituspaikat = aloituspaikat,
        minAloituspaikat = minAloituspaikat,
        maxAloituspaikat = maxAloituspaikat,
        ensikertalaisenAloituspaikat = ensikertalaisenAloituspaikat,
        minEnsikertalaisenAloituspaikat = minEnsikertalaisenAloituspaikat,
        maxEnsikertalaisenAloituspaikat = maxEnsikertalaisenAloituspaikat,
        pohjakoulutusvaatimusKoodiUrit = pohjakoulutusvaatimus.map(_.koodiUri),
        muuPohjakoulutusvaatimus = muuPohjakoulutusvaatimus,
        toinenAsteOnkoKaksoistutkinto = toinenAsteOnkoKaksoistutkinto,
        kaytetaanHaunAikataulua = kaytetaanHaunAikataulua,
        valintaperusteId = valintaperuste.map(_.id),
        yhdenPaikanSaanto = yhdenPaikanSaanto,
        liitteetOnkoSamaToimitusaika = liitteetOnkoSamaToimitusaika,
        liitteetOnkoSamaToimitusosoite = liitteetOnkoSamaToimitusosoite,
        liitteidenToimitusaika = liitteidenToimitusaika,
        liitteidenToimitustapa = liitteidenToimitustapa,
        liitteidenToimitusosoite = liitteidenToimitusosoite,
        liitteet = liitteet,
        valintakokeet = valintakokeet.map(_.toValintakoe),
        hakuajat = hakuajat,
        muokkaaja = muokkaaja.oid,
        tarjoajat = jarjestyspaikka.map(o => List(o.oid)).getOrElse(toteutus.tarjoajat.map(_.oid)),
        organisaatioOid = organisaatio.get.oid,
        kielivalinta = kielivalinta,
        modified = modified
      )
    } catch {
      case e: Exception => {
        val msg: String = s"Failed to create Hakukohde (${oid})"
        logger.error(msg, e)
        throw new RuntimeException(msg, e)
      }
    }
  }
}
