package fi.oph.kouta.internal.domain.indexed

import java.time.LocalDateTime
import java.util.UUID
import fi.oph.kouta.internal.domain.enums.{Hakulomaketyyppi, Julkaisutila, Kieli, LiitteenToimitustapa}
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid, ToteutusOid}
import fi.oph.kouta.internal.domain.{Ajanjakso, Hakukohde, Kielistetty, Liite, LiitteenToimitusosoite, PainotettuArvosana, Sora, WithTila, YhdenPaikanSaanto}
import fi.vm.sade.utils.slf4j.Logging

case class HakukohdeToteutusIndexed(oid: ToteutusOid, tarjoajat: List[Organisaatio])

case class AloituspaikatIndexed(lukumaara: Option[Int], ensikertalaisille: Option[Int])

case class OppiaineIndexed(
  koodiUri: Option[String]
)

case class PainottevaOppiaineKoodiIndexed(
  oppiaine: Option[OppiaineIndexed]
)

case class PainotettuArvosanaIndexed(
    painokerroin: Option[Double],
    koodit: Option[PainottevaOppiaineKoodiIndexed]
) {
  def toPainotettuArvosana: PainotettuArvosana = {
    PainotettuArvosana(
       koodiUri = koodit.flatMap(_.oppiaine).flatMap(_.koodiUri),
       painokerroin = painokerroin
    )
  }
}

case class HakukohteenLinjaIndexed(
    alinHyvaksyttyKeskiarvo: Option[Double],
    painotetutArvosanat: List[PainotettuArvosanaIndexed]
)

case class HakukohdeMetadataIndexed(
    kaytetaanHaunAlkamiskautta: Option[Boolean],
    aloituspaikat: Option[AloituspaikatIndexed],
    uudenOpiskelijanUrl: Option[Kielistetty],
    koulutuksenAlkamiskausi: Option[KoulutuksenAlkamiskausi],
    hakukohteenLinja: Option[HakukohteenLinjaIndexed]
)

case class HakukohdeIndexed(
    oid: HakukohdeOid,
    toteutus: HakukohdeToteutusIndexed,
    hakuOid: HakuOid,
    tila: Julkaisutila,
    nimi: Kielistetty,
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeAtaruId: Option[UUID],
    hakulomakeKuvaus: Kielistetty,
    hakulomakeLinkki: Kielistetty,
    kaytetaanHaunHakulomaketta: Option[Boolean],
    pohjakoulutusvaatimus: Seq[KoodiUri],
    muuPohjakoulutusvaatimus: Kielistetty,
    toinenAsteOnkoKaksoistutkinto: Option[Boolean],
    kaytetaanHaunAikataulua: Option[Boolean],
    valintaperuste: Option[UuidObject],
    yhdenPaikanSaanto: YhdenPaikanSaanto,
    koulutustyyppikoodi: Option[String],
    onkoHarkinnanvarainenKoulutus: Option[Boolean],
    salliikoHakukohdeHarkinnanvaraisuudenKysymisen: Option[Boolean],
    voikoHakukohteessaOllaHarkinnanvaraisestiHakeneita: Option[Boolean],
    liitteetOnkoSamaToimitusaika: Option[Boolean],
    liitteetOnkoSamaToimitusosoite: Option[Boolean],
    liitteidenToimitusaika: Option[LocalDateTime],
    liitteidenToimitustapa: Option[LiitteenToimitustapa],
    liitteidenToimitusosoite: Option[LiitteenToimitusosoite],
    liitteet: List[Liite],
    sora: Option[Sora],
    valintakokeet: List[ValintakoeIndexed],
    hakuajat: List[Ajanjakso],
    muokkaaja: Muokkaaja,
    jarjestyspaikka: Option[Organisaatio],
    organisaatio: Option[Organisaatio],
    kielivalinta: Seq[Kieli],
    modified: Option[LocalDateTime],
    metadata: Option[HakukohdeMetadataIndexed],
    jarjestaaUrheilijanAmmKoulutusta: Option[Boolean],
    externalId: Option[String]
) extends WithTila
    with Logging {
  def toHakukohde(oikeusHakukohteeseenFn: OrganisaatioOid => Option[Boolean]): Hakukohde = {
    try {
      val tarjoaja = jarjestyspaikka.map(o => o.oid)
      Hakukohde(
        oid = oid,
        toteutusOid = toteutus.oid,
        hakuOid = hakuOid,
        tila = tila,
        nimi = nimi,
        alkamiskausiKoodiUri = metadata.flatMap(m =>
          m.koulutuksenAlkamiskausi
            .flatMap(_.koulutuksenAlkamiskausi.map(_.koodiUri))
        ),
        alkamisvuosi = metadata.flatMap(m => m.koulutuksenAlkamiskausi.flatMap(_.koulutuksenAlkamisvuosi)),
        kaytetaanHaunAlkamiskautta = metadata.flatMap(_.kaytetaanHaunAlkamiskautta),
        hakulomaketyyppi = hakulomaketyyppi,
        hakulomakeAtaruId = hakulomakeAtaruId,
        hakulomakeKuvaus = hakulomakeKuvaus,
        hakulomakeLinkki = hakulomakeLinkki,
        kaytetaanHaunHakulomaketta = kaytetaanHaunHakulomaketta,
        aloituspaikat = metadata.flatMap(_.aloituspaikat.flatMap(_.lukumaara)),
        ensikertalaisenAloituspaikat = metadata.flatMap(_.aloituspaikat.flatMap(_.ensikertalaisille)),
        alinHyvaksyttyKeskiarvo = metadata.flatMap(_.hakukohteenLinja.flatMap(_.alinHyvaksyttyKeskiarvo)),
        painotetutArvosanat = metadata.flatMap(_.hakukohteenLinja).flatMap(_.painotetutArvosanat).map(_.toPainotettuArvosana).toList,
        pohjakoulutusvaatimusKoodiUrit = pohjakoulutusvaatimus.map(_.koodiUri),
        muuPohjakoulutusvaatimus = muuPohjakoulutusvaatimus,
        toinenAsteOnkoKaksoistutkinto = toinenAsteOnkoKaksoistutkinto,
        kaytetaanHaunAikataulua = kaytetaanHaunAikataulua,
        valintaperusteId = valintaperuste.map(_.id),
        yhdenPaikanSaanto = yhdenPaikanSaanto,
        koulutustyyppikoodi = koulutustyyppikoodi,
        onkoHarkinnanvarainenKoulutus = onkoHarkinnanvarainenKoulutus,
        salliikoHakukohdeHarkinnanvaraisuudenKysymisen = salliikoHakukohdeHarkinnanvaraisuudenKysymisen,
        voikoHakukohteessaOllaHarkinnanvaraisestiHakeneita = voikoHakukohteessaOllaHarkinnanvaraisestiHakeneita,
        liitteetOnkoSamaToimitusaika = liitteetOnkoSamaToimitusaika,
        liitteetOnkoSamaToimitusosoite = liitteetOnkoSamaToimitusosoite,
        liitteidenToimitusaika = liitteidenToimitusaika,
        liitteidenToimitustapa = liitteidenToimitustapa,
        liitteidenToimitusosoite = liitteidenToimitusosoite,
        liitteet = liitteet,
        sora = sora,
        valintakokeet = valintakokeet.map(_.toValintakoe),
        hakuajat = hakuajat,
        muokkaaja = muokkaaja.oid,
        tarjoaja = tarjoaja,
        organisaatioOid = organisaatio.get.oid,
        organisaatioNimi = organisaatio.get.nimi,
        kielivalinta = kielivalinta,
        modified = modified,
        oikeusHakukohteeseen = tarjoaja.flatMap(t => oikeusHakukohteeseenFn(t)),
        jarjestaaUrheilijanAmmKoulutusta = jarjestaaUrheilijanAmmKoulutusta,
        externalId = externalId,
        uudenOpiskelijanUrl = metadata.flatMap(_.uudenOpiskelijanUrl)
      )
    } catch {
      case e: Exception => {
        val msg: String = s"Failed to create Hakukohde (${oid})"
        logger.error(msg, e)
        throw new RuntimeException(msg, e)
      }
    }
  }

  def toHakukohde: Hakukohde = {
    toHakukohde(_ => None)
  }
}
