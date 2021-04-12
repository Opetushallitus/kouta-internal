package fi.oph.kouta.internal.domain.indexed

import fi.oph.kouta.domain.{Hakutermi, Koulutustyyppi, Maksullisuustyyppi}
import fi.oph.kouta.internal.domain._
import fi.oph.kouta.internal.domain.enums.{Hakulomaketyyppi, Julkaisutila, Kieli}
import fi.oph.kouta.internal.domain.oid.{KoulutusOid, ToteutusOid}
import fi.vm.sade.utils.slf4j.Logging

import java.time.LocalDateTime

case class ToteutusIndexed(
    oid: ToteutusOid,
    koulutusOid: KoulutusOid,
    tila: Julkaisutila,
    tarjoajat: List[Organisaatio],
    nimi: Kielistetty,
    metadata: Option[ToteutusMetadataIndexed],
    muokkaaja: Muokkaaja,
    organisaatio: Option[Organisaatio],
    kielivalinta: Seq[Kieli],
    modified: Option[LocalDateTime]
) extends WithTila
    with Logging {
  def toToteutus: Toteutus = {
    try {
      Toteutus(
        oid = oid,
        koulutusOid = koulutusOid,
        tila = tila,
        tarjoajat = tarjoajat.map(_.oid),
        nimi = nimi,
        metadata = metadata.map(_.toToteutusMetadata),
        muokkaaja = muokkaaja.oid,
        organisaatioOid = organisaatio.get.oid,
        kielivalinta = kielivalinta,
        modified = modified
      )
    } catch {
      case e: Exception =>
        val msg: String = s"Failed to create Toteutus ($oid)"
        logger.error(msg, e)
        throw new RuntimeException(msg, e)
    }
  }
}

sealed trait ToteutusMetadataIndexed {
  val tyyppi: Koulutustyyppi
  val kuvaus: Kielistetty
  val opetus: Option[OpetusIndexed]
  val asiasanat: List[Keyword]
  val ammattinimikkeet: List[Keyword]
  val yhteyshenkilot: Seq[Yhteyshenkilo]

  def toToteutusMetadata: ToteutusMetadata
}

case class AmmatillinenToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    osaamisalat: List[AmmatillinenOsaamisalaIndexed],
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo]
) extends ToteutusMetadataIndexed {
  override def toToteutusMetadata: AmmatillinenToteutusMetadata = {
    AmmatillinenToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      osaamisalat = osaamisalat.map(_.toOsaamisala),
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot
    )
  }
}

sealed trait TutkintoonJohtamatonToteutusMetadataIndexed extends ToteutusMetadataIndexed {
  val hakutermi: Option[Hakutermi]
  val hakulomaketyyppi: Option[Hakulomaketyyppi]
  val hakulomakeLinkki: Kielistetty
  val lisatietoaHakeutumisesta: Kielistetty
  val lisatietoaValintaperusteista: Kielistetty
  val hakuaika: Option[Ajanjakso]
  val aloituspaikat: Option[Int]
}

case class AmmatillinenTutkinnonOsaToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    osaamisalat: List[AmmatillinenOsaamisalaIndexed],
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int]
) extends TutkintoonJohtamatonToteutusMetadataIndexed {
  override def toToteutusMetadata: AmmatillinenTutkinnonOsaToteutusMetadata = {
    AmmatillinenTutkinnonOsaToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      hakutermi = hakutermi,
      hakulomaketyyppi = hakulomaketyyppi,
      hakulomakeLinkki = hakulomakeLinkki,
      lisatietoaHakeutumisesta = lisatietoaHakeutumisesta,
      lisatietoaValintaperusteista = lisatietoaValintaperusteista,
      hakuaika = hakuaika,
      aloituspaikat = aloituspaikat
    )
  }
}

case class AmmatillinenOsaamisalaToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    osaamisalat: List[AmmatillinenOsaamisalaIndexed],
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int]
) extends TutkintoonJohtamatonToteutusMetadataIndexed {
  override def toToteutusMetadata: AmmatillinenOsaamisalaToteutusMetadata = {
    AmmatillinenOsaamisalaToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      hakutermi = hakutermi,
      hakulomaketyyppi = hakulomaketyyppi,
      hakulomakeLinkki = hakulomakeLinkki,
      lisatietoaHakeutumisesta = lisatietoaHakeutumisesta,
      lisatietoaValintaperusteista = lisatietoaValintaperusteista,
      hakuaika = hakuaika,
      aloituspaikat = aloituspaikat
    )
  }
}

sealed trait KorkeakouluToteutusMetadataIndexed extends ToteutusMetadataIndexed {
  val alemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisalaIndexed]
  val ylemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisalaIndexed]
}

case class YliopistoToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    alemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisalaIndexed],
    ylemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisalaIndexed]
) extends KorkeakouluToteutusMetadataIndexed {
  override def toToteutusMetadata: YliopistoToteutusMetadata = {
    YliopistoToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      alemmanKorkeakoulututkinnonOsaamisalat = alemmanKorkeakoulututkinnonOsaamisalat.map(_.toOsaamisala),
      ylemmanKorkeakoulututkinnonOsaamisalat = ylemmanKorkeakoulututkinnonOsaamisalat.map(_.toOsaamisala)
    )
  }
}

case class AmmattikorkeakouluToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    alemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisalaIndexed],
    ylemmanKorkeakoulututkinnonOsaamisalat: Seq[KorkeakouluOsaamisalaIndexed]
) extends KorkeakouluToteutusMetadataIndexed {
  override def toToteutusMetadata: AmmattikorkeakouluToteutusMetadata = {
    AmmattikorkeakouluToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      alemmanKorkeakoulututkinnonOsaamisalat = alemmanKorkeakoulututkinnonOsaamisalat.map(_.toOsaamisala),
      ylemmanKorkeakoulututkinnonOsaamisalat = ylemmanKorkeakoulututkinnonOsaamisalat.map(_.toOsaamisala)
    )
  }
}

sealed trait OsaamisalaIndexed {
  val linkki: Kielistetty
  val otsikko: Kielistetty

  def toOsaamisala: Osaamisala
}

case class AmmatillinenOsaamisalaIndexed(koodi: KoodiUri, linkki: Kielistetty, otsikko: Kielistetty)
    extends OsaamisalaIndexed {
  override def toOsaamisala: AmmatillinenOsaamisala =
    AmmatillinenOsaamisala(koodi = koodi.koodiUri, linkki = linkki, otsikko = otsikko)
}

case class KorkeakouluOsaamisalaIndexed(
    nimi: Kielistetty,
    kuvaus: Kielistetty,
    linkki: Kielistetty,
    otsikko: Kielistetty
) extends OsaamisalaIndexed {
  override def toOsaamisala: KorkeakouluOsaamisala =
    KorkeakouluOsaamisala(nimi = nimi, kuvaus = kuvaus, linkki = linkki, otsikko = otsikko)
}

case class OpetusIndexed(
    opetuskieli: Seq[KoodiUri],
    opetuskieletKuvaus: Kielistetty,
    opetusaika: Seq[KoodiUri],
    opetusaikaKuvaus: Kielistetty,
    opetustapa: Seq[KoodiUri],
    opetustapaKuvaus: Kielistetty,
    @deprecated("Tämän korvaa maksullisuustyyppi") onkoMaksullinen: Option[Boolean],
    maksullisuustyyppi: Option[Maksullisuustyyppi],
    maksullisuusKuvaus: Kielistetty,
    maksunMaara: Option[Double],
    koulutuksenTarkkaAlkamisaika: Option[Boolean],
    koulutuksenAlkamiskausi: Option[KoodiUri],
    koulutuksenAlkamisvuosi: Option[Int],
    lisatiedot: Seq[LisatietoIndexed],
    onkoStipendia: Option[Boolean],
    stipendinMaara: Option[Double],
    stipendinKuvaus: Kielistetty
) {
  def toOpetus: Opetus = Opetus(
    opetuskieliKoodiUrit = opetuskieli.map(_.koodiUri),
    opetuskieletKuvaus = opetuskieletKuvaus,
    opetusaikaKoodiUrit = opetusaika.map(_.koodiUri),
    opetusaikaKuvaus = opetusaikaKuvaus,
    opetustapaKoodiUrit = opetustapa.map(_.koodiUri),
    opetustapaKuvaus = opetustapaKuvaus,
    onkoMaksullinen = onkoMaksullinen.getOrElse(false),
    maksullisuustyyppi = maksullisuustyyppi,
    maksullisuusKuvaus = maksullisuusKuvaus,
    maksunMaara = maksunMaara,
    koulutuksenTarkkaAlkamisaika = koulutuksenTarkkaAlkamisaika.getOrElse(false),
    alkamiskausiKoodiUri = koulutuksenAlkamiskausi.map(_.koodiUri),
    alkamisvuosi = koulutuksenAlkamisvuosi.map(_.toString),
    lisatiedot = lisatiedot.map(_.toLisatieto),
    onkoStipendia = onkoStipendia.getOrElse(false),
    stipendinMaara = stipendinMaara,
    stipendinKuvaus = stipendinKuvaus
  )
}
