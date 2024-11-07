package fi.oph.kouta.internal.domain.indexed

import java.time.LocalDateTime
import java.util.UUID
import fi.oph.kouta.internal.domain.enums.{Hakulomaketyyppi, Julkaisutila, Kieli, LiitteenToimitustapa}
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid, ToteutusOid}
import fi.oph.kouta.internal.domain.{
  Ajanjakso,
  Hakukohde,
  Kielistetty,
  Liite,
  LiitteenToimitusosoite,
  LukiolinjaTieto,
  OdwKkTasot,
  PaateltyAlkamiskausi,
  PainotettuArvosana,
  Sora,
  WithTila,
  YhdenPaikanSaanto
}
import fi.oph.kouta.logging.Logging

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

case class OdwKkTasotIndexed(
    alempiKkAste: Boolean,
    ylempiKkAste: Boolean,
    kkTutkinnonTaso: Int,
    kkTutkinnonTasoSykli: Int
) {
  def toOdwKkTasot: OdwKkTasot = {
    OdwKkTasot(
      alempiKkAste = alempiKkAste,
      ylempiKkAste = ylempiKkAste,
      kkTutkinnonTaso = kkTutkinnonTaso,
      kkTutkinnonTasoSykli = kkTutkinnonTasoSykli
    )
  }
}

case class HakukohteenLinjaIndexed(
    alinHyvaksyttyKeskiarvo: Option[Double],
    painotetutArvosanat: List[PainotettuArvosanaIndexed],
    painotetutArvosanatOppiaineittain: List[PainotettuArvosanaIndexed],
    linja: Option[KoodiUri]
)

case class HakukohdeMetadataIndexed(
    kaytetaanHaunAlkamiskautta: Option[Boolean],
    aloituspaikat: Option[AloituspaikatIndexed],
    uudenOpiskelijanUrl: Option[Kielistetty],
    koulutuksenAlkamiskausi: Option[KoulutuksenAlkamiskausi],
    hakukohteenLinja: Option[HakukohteenLinjaIndexed]
)

case class LukioTieto(isLukio: Boolean = true, linja: Option[KoodiUri])

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
    valintaperuste: Option[ValintaperusteIndexed],
    yhdenPaikanSaanto: YhdenPaikanSaanto,
    koulutustyyppikoodi: Option[String],
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
    externalId: Option[String],
    hakukohde: Option[KoodiUri],
    paateltyAlkamiskausi: Option[PaateltyAlkamiskausi],
    odwKkTasot: Option[OdwKkTasotIndexed],
    jarjestyspaikkaHierarkiaNimi: Option[Kielistetty],
    opetuskieliKoodiUrit: List[String],
    johtaaTutkintoon: Option[Boolean]
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
        painotetutArvosanat = metadata
          .flatMap(_.hakukohteenLinja.flatMap(linja => Option.apply(linja.painotetutArvosanatOppiaineittain)))
          .getOrElse(List.empty)
          .map(_.toPainotettuArvosana),
        pohjakoulutusvaatimusKoodiUrit = pohjakoulutusvaatimus.map(_.koodiUri),
        muuPohjakoulutusvaatimus = muuPohjakoulutusvaatimus,
        toinenAsteOnkoKaksoistutkinto = toinenAsteOnkoKaksoistutkinto,
        kaytetaanHaunAikataulua = kaytetaanHaunAikataulua,
        valintaperusteId = valintaperuste.flatMap(_.id),
        valintaperusteValintakokeet =
          valintaperuste.flatMap(vp => Option.apply(vp.valintakokeet)).getOrElse(List.empty).map(_.toValintakoe),
        yhdenPaikanSaanto = yhdenPaikanSaanto,
        koulutustyyppikoodi = koulutustyyppikoodi,
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
        uudenOpiskelijanUrl = metadata.flatMap(_.uudenOpiskelijanUrl),
        hakukohde = hakukohde,
        lukioTieto = metadata.flatMap(m => m.hakukohteenLinja.map(l => LukioTieto(linja = l.linja))),
        paateltyAlkamiskausi = paateltyAlkamiskausi,
        odwKkTasot = odwKkTasot.map(_.toOdwKkTasot),
        jarjestyspaikkaHierarkiaNimi = jarjestyspaikkaHierarkiaNimi,
        opetuskieliKoodiUrit = opetuskieliKoodiUrit,
        johtaaTutkintoon = Option.apply(johtaaTutkintoon)
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
