package fi.oph.kouta.internal.domain.indexed

import fi.oph.kouta.domain._
import fi.oph.kouta.internal.domain._
import fi.oph.kouta.internal.domain.enums.{Hakulomaketyyppi, Julkaisutila, Kieli}
import fi.oph.kouta.internal.domain.oid.{HakuOid, KoulutusOid, ToteutusOid}
import fi.oph.kouta.logging.Logging

import java.time.LocalDateTime

case class ToteutusIndexed(
    oid: ToteutusOid,
    koulutusOid: KoulutusOid,
    tila: Julkaisutila,
    tarjoajat: Option[List[Organisaatio]],
    nimi: Kielistetty,
    metadata: Option[ToteutusMetadataIndexed],
    muokkaaja: Muokkaaja,
    organisaatio: Option[Organisaatio],
    kielivalinta: Seq[Kieli],
    modified: Option[LocalDateTime],
    externalId: Option[String],
    haut: Seq[HakuOid]
) extends WithTila
    with Logging {
  def toToteutus: Toteutus = {
    try {
      Toteutus(
        oid = oid,
        koulutusOid = koulutusOid,
        tila = tila,
        tarjoajat = tarjoajat.toList.flatten.map(_.oid),
        nimi = nimi,
        metadata = metadata.map(_.toToteutusMetadata),
        muokkaaja = muokkaaja.oid,
        organisaatioOid = organisaatio.get.oid,
        kielivalinta = kielivalinta,
        modified = modified,
        externalId = externalId
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
  val isHakukohteetKaytossa: Option[Boolean]
  val hakutermi: Option[Hakutermi]
  val hakulomaketyyppi: Option[Hakulomaketyyppi]
  val hakulomakeLinkki: Kielistetty
  val lisatietoaHakeutumisesta: Kielistetty
  val lisatietoaValintaperusteista: Kielistetty
  val hakuaika: Option[Ajanjakso]
  val aloituspaikat: Option[Int]
  val aloituspaikkakuvaus: Kielistetty
}

case class AmmatillinenTutkinnonOsaToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    osaamisalat: List[AmmatillinenOsaamisalaIndexed],
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    isHakukohteetKaytossa: Option[Boolean],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int],
    aloituspaikkakuvaus: Kielistetty
) extends TutkintoonJohtamatonToteutusMetadataIndexed {
  override def toToteutusMetadata: AmmatillinenTutkinnonOsaToteutusMetadata = {
    AmmatillinenTutkinnonOsaToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      isHakukohteetKaytossa = isHakukohteetKaytossa,
      hakutermi = hakutermi,
      hakulomaketyyppi = hakulomaketyyppi,
      hakulomakeLinkki = hakulomakeLinkki,
      lisatietoaHakeutumisesta = lisatietoaHakeutumisesta,
      lisatietoaValintaperusteista = lisatietoaValintaperusteista,
      hakuaika = hakuaika,
      aloituspaikat = aloituspaikat,
      aloituspaikkakuvaus = aloituspaikkakuvaus
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
    isHakukohteetKaytossa: Option[Boolean],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int],
    aloituspaikkakuvaus: Kielistetty
) extends TutkintoonJohtamatonToteutusMetadataIndexed {
  override def toToteutusMetadata: AmmatillinenOsaamisalaToteutusMetadata = {
    AmmatillinenOsaamisalaToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      isHakukohteetKaytossa = isHakukohteetKaytossa,
      hakutermi = hakutermi,
      hakulomaketyyppi = hakulomaketyyppi,
      hakulomakeLinkki = hakulomakeLinkki,
      lisatietoaHakeutumisesta = lisatietoaHakeutumisesta,
      lisatietoaValintaperusteista = lisatietoaValintaperusteista,
      hakuaika = hakuaika,
      aloituspaikat = aloituspaikat,
      aloituspaikkakuvaus = aloituspaikkakuvaus
    )
  }
}

case class AmmatillinenMuuToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    isHakukohteetKaytossa: Option[Boolean],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int],
    aloituspaikkakuvaus: Kielistetty
) extends TutkintoonJohtamatonToteutusMetadataIndexed {
  override def toToteutusMetadata: AmmatillinenMuuToteutusMetadata = {
    AmmatillinenMuuToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      isHakukohteetKaytossa = isHakukohteetKaytossa,
      hakutermi = hakutermi,
      hakulomaketyyppi = hakulomaketyyppi,
      hakulomakeLinkki = hakulomakeLinkki,
      lisatietoaHakeutumisesta = lisatietoaHakeutumisesta,
      lisatietoaValintaperusteista = lisatietoaValintaperusteista,
      hakuaika = hakuaika,
      aloituspaikat = aloituspaikat,
      aloituspaikkakuvaus = aloituspaikkakuvaus
    )
  }
}

case class TuvaToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    jarjestetaanErityisopetuksena: Boolean
) extends ToteutusMetadataIndexed {
  override def toToteutusMetadata: TuvaToteutusMetadata = {
    TuvaToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      jarjestetaanErityisopetuksena = jarjestetaanErityisopetuksena
    )
  }
}

case class TelmaToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo]
) extends ToteutusMetadataIndexed {
  override def toToteutusMetadata: TelmaToteutusMetadata = {
    TelmaToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot
    )
  }
}

case class VapaaSivistystyoOpistovuosiToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo]
) extends ToteutusMetadataIndexed {
  override def toToteutusMetadata: VapaaSivistystyoOpistovuosiToteutusMetadata = {
    VapaaSivistystyoOpistovuosiToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot
    )
  }
}

case class VapaaSivistystyoMuuToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    isHakukohteetKaytossa: Option[Boolean],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int],
    aloituspaikkakuvaus: Kielistetty
) extends TutkintoonJohtamatonToteutusMetadataIndexed {
  override def toToteutusMetadata: VapaaSivistystyoMuuToteutusMetadata = {
    VapaaSivistystyoMuuToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      isHakukohteetKaytossa = isHakukohteetKaytossa,
      hakutermi = hakutermi,
      hakulomaketyyppi = hakulomaketyyppi,
      hakulomakeLinkki = hakulomakeLinkki,
      lisatietoaHakeutumisesta = lisatietoaHakeutumisesta,
      lisatietoaValintaperusteista = lisatietoaValintaperusteista,
      hakuaika = hakuaika,
      aloituspaikat = aloituspaikat,
      aloituspaikkakuvaus = aloituspaikkakuvaus
    )
  }
}

case class YliopistoToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo]
) extends ToteutusMetadataIndexed {
  override def toToteutusMetadata: YliopistoToteutusMetadata = {
    YliopistoToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot
    )
  }
}

case class AmmattikorkeakouluToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo]
) extends ToteutusMetadataIndexed {
  override def toToteutusMetadata: AmmattikorkeakouluToteutusMetadata = {
    AmmattikorkeakouluToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot
    )
  }
}

case class AmmOpeErityisopeJaOpoToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi = AmmOpeErityisopeJaOpo,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo]
) extends ToteutusMetadataIndexed {
  override def toToteutusMetadata: AmmOpeErityisopeJaOpoToteutusMetadata = {
    AmmOpeErityisopeJaOpoToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot
    )
  }
}

case class OpePedagOpinnotToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi = OpePedagOpinnot,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo]
) extends ToteutusMetadataIndexed {
  override def toToteutusMetadata: OpePedagOpinnotToteutusMetadata = {
    OpePedagOpinnotToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot
    )
  }
}

case class KkOpintojaksoToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi = KkOpintojakso,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    isHakukohteetKaytossa: Option[Boolean],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int],
    aloituspaikkakuvaus: Kielistetty
) extends ToteutusMetadataIndexed {
  override def toToteutusMetadata: KkOpintojaksoToteutusMetadata = {
    KkOpintojaksoToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      isHakukohteetKaytossa = isHakukohteetKaytossa,
      hakutermi = hakutermi,
      hakulomaketyyppi = hakulomaketyyppi,
      hakulomakeLinkki = hakulomakeLinkki,
      lisatietoaHakeutumisesta = lisatietoaHakeutumisesta,
      lisatietoaValintaperusteista = lisatietoaValintaperusteista,
      hakuaika = hakuaika,
      aloituspaikat = aloituspaikat,
      aloituspaikkakuvaus = aloituspaikkakuvaus
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

case class KoulutuksenAlkamiskausi(
    alkamiskausityyppi: Alkamiskausityyppi,
    koulutuksenAlkamiskausi: Option[KoodiUri],
    koulutuksenAlkamisvuosi: Option[String]
)

case class OpetusIndexed(
    opetuskieli: Seq[KoodiUri],
    opetuskieletKuvaus: Kielistetty,
    opetusaika: Seq[KoodiUri],
    opetusaikaKuvaus: Kielistetty,
    opetustapa: Seq[KoodiUri],
    opetustapaKuvaus: Kielistetty,
    maksullisuustyyppi: Option[Maksullisuustyyppi],
    maksullisuusKuvaus: Kielistetty,
    maksunMaara: Option[Double],
    koulutuksenAlkamiskausi: Option[KoulutuksenAlkamiskausi],
    lisatiedot: Seq[LisatietoIndexed]
) {
  def toOpetus: Opetus = Opetus(
    opetuskieliKoodiUrit = opetuskieli.map(_.koodiUri),
    opetuskieletKuvaus = opetuskieletKuvaus,
    opetusaikaKoodiUrit = opetusaika.map(_.koodiUri),
    opetusaikaKuvaus = opetusaikaKuvaus,
    opetustapaKoodiUrit = opetustapa.map(_.koodiUri),
    opetustapaKuvaus = opetustapaKuvaus,
    maksullisuustyyppi = maksullisuustyyppi,
    maksullisuusKuvaus = maksullisuusKuvaus,
    maksunMaara = maksunMaara,
    alkamiskausiKoodiUri = koulutuksenAlkamiskausi.flatMap(_.koulutuksenAlkamiskausi.map(_.koodiUri)),
    alkamisvuosi = koulutuksenAlkamiskausi.flatMap(_.koulutuksenAlkamisvuosi),
    lisatiedot = lisatiedot.map(_.toLisatieto)
  )
}

case class KielivalikoimaIndexed(
    A1Kielet: Seq[KoodiUri] = Seq(),
    A2Kielet: Seq[KoodiUri] = Seq(),
    B1Kielet: Seq[KoodiUri] = Seq(),
    B2Kielet: Seq[KoodiUri] = Seq(),
    B3Kielet: Seq[KoodiUri] = Seq(),
    aidinkielet: Seq[KoodiUri] = Seq(),
    muutKielet: Seq[KoodiUri] = Seq()
) {
  def toKielivalikoima: Kielivalikoima =
    Kielivalikoima(
      A1Kielet = A1Kielet.map(_.koodiUri),
      A2Kielet = A2Kielet.map(_.koodiUri),
      B1Kielet = B1Kielet.map(_.koodiUri),
      B2Kielet = B2Kielet.map(_.koodiUri),
      B3Kielet = B3Kielet.map(_.koodiUri),
      aidinkielet = aidinkielet.map(_.koodiUri),
      muutKielet = muutKielet.map(_.koodiUri)
    )
}

case class LukiolinjaTietoIndexed(koodi: KoodiUri, kuvaus: Kielistetty) {
  def toLukioLinjaTieto: LukiolinjaTieto = LukiolinjaTieto(koodi.koodiUri, kuvaus)
}

case class LukiodiplomiTietoIndexed(koodi: KoodiUri, linkki: Kielistetty, linkinAltTeksti: Kielistetty) {
  def toLukioDiplomiTieto: LukiodiplomiTieto = LukiodiplomiTieto(koodi.koodiUri, linkki, linkinAltTeksti)
}

case class LukioToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi = Lk,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    kielivalikoima: Option[KielivalikoimaIndexed],
    yleislinja: Boolean,
    painotukset: Seq[LukiolinjaTietoIndexed],
    erityisetKoulutustehtavat: Seq[LukiolinjaTietoIndexed],
    diplomit: Seq[LukiodiplomiTietoIndexed]
) extends ToteutusMetadataIndexed {
  def toToteutusMetadata: LukioToteutusMetadata =
    LukioToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      kielivalikoima = kielivalikoima.map(_.toKielivalikoima),
      yleislinja = yleislinja,
      painotukset = painotukset.map(_.toLukioLinjaTieto),
      erityisetKoulutustehtavat = erityisetKoulutustehtavat.map(_.toLukioLinjaTieto),
      diplomit = diplomit.map(_.toLukioDiplomiTieto)
    )
}

case class AikuistenPerusopetusToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    isHakukohteetKaytossa: Option[Boolean],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int],
    aloituspaikkakuvaus: Kielistetty
) extends TutkintoonJohtamatonToteutusMetadataIndexed {
  override def toToteutusMetadata: AikuistenPerusopetusToteutusMetadata = {
    AikuistenPerusopetusToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      isHakukohteetKaytossa = isHakukohteetKaytossa,
      hakutermi = hakutermi,
      hakulomaketyyppi = hakulomaketyyppi,
      hakulomakeLinkki = hakulomakeLinkki,
      lisatietoaHakeutumisesta = lisatietoaHakeutumisesta,
      lisatietoaValintaperusteista = lisatietoaValintaperusteista,
      hakuaika = hakuaika,
      aloituspaikat = aloituspaikat,
      aloituspaikkakuvaus = aloituspaikkakuvaus
    )
  }
}

case class ErikoislaakariToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo]
) extends ToteutusMetadataIndexed {
  override def toToteutusMetadata: ErikoislaakariToteutusMetadata = {
    ErikoislaakariToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot
    )
  }
}

case class KkOpintokokonaisuusToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi = KkOpintokokonaisuus,
    kuvaus: Kielistetty,
    opintojenLaajuusyksikko: Option[KoodiUri],
    opintojenLaajuusNumero: Option[Double],
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    isHakukohteetKaytossa: Option[Boolean],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int],
    aloituspaikkakuvaus: Kielistetty
) extends ToteutusMetadataIndexed {
  override def toToteutusMetadata: KkOpintokokonaisuusToteutusMetadata = {
    KkOpintokokonaisuusToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      isHakukohteetKaytossa = isHakukohteetKaytossa,
      hakutermi = hakutermi,
      hakulomaketyyppi = hakulomaketyyppi,
      hakulomakeLinkki = hakulomakeLinkki,
      lisatietoaHakeutumisesta = lisatietoaHakeutumisesta,
      lisatietoaValintaperusteista = lisatietoaValintaperusteista,
      hakuaika = hakuaika,
      aloituspaikat = aloituspaikat,
      aloituspaikkakuvaus = aloituspaikkakuvaus,
      opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
      opintojenLaajuusNumero = opintojenLaajuusNumero
    )
  }
}

case class ErikoistumiskoulutusToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    isHakukohteetKaytossa: Option[Boolean],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int],
    aloituspaikkakuvaus: Kielistetty
) extends TutkintoonJohtamatonToteutusMetadataIndexed {
  override def toToteutusMetadata: ErikoistumiskoulutusToteutusMetadata = {
    ErikoistumiskoulutusToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      isHakukohteetKaytossa = isHakukohteetKaytossa,
      hakutermi = hakutermi,
      hakulomaketyyppi = hakulomaketyyppi,
      hakulomakeLinkki = hakulomakeLinkki,
      lisatietoaHakeutumisesta = lisatietoaHakeutumisesta,
      lisatietoaValintaperusteista = lisatietoaValintaperusteista,
      hakuaika = hakuaika,
      aloituspaikat = aloituspaikat,
      aloituspaikkakuvaus = aloituspaikkakuvaus
    )
  }
}

case class TaiteenPerusopetusToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opintojenLaajuusyksikko: Option[KoodiUri],
    opintojenLaajuusNumeroMin: Option[Double],
    opintojenLaajuusNumeroMax: Option[Double],
    taiteenala: Seq[KoodiUri],
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    isHakukohteetKaytossa: Option[Boolean],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int],
    aloituspaikkakuvaus: Kielistetty
) extends TutkintoonJohtamatonToteutusMetadataIndexed {
  override def toToteutusMetadata: TaiteenPerusopetusToteutusMetadata =
    TaiteenPerusopetusToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
      opintojenLaajuusNumeroMin = opintojenLaajuusNumeroMin,
      opintojenLaajuusNumeroMax = opintojenLaajuusNumeroMax,
      taiteenalaKoodiUrit = taiteenala.map(_.koodiUri),
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      isHakukohteetKaytossa = isHakukohteetKaytossa,
      hakutermi = hakutermi,
      hakulomaketyyppi = hakulomaketyyppi,
      hakulomakeLinkki = hakulomakeLinkki,
      lisatietoaHakeutumisesta = lisatietoaHakeutumisesta,
      lisatietoaValintaperusteista = lisatietoaValintaperusteista,
      hakuaika = hakuaika,
      aloituspaikat = aloituspaikat,
      aloituspaikkakuvaus = aloituspaikkakuvaus
    )
}

case class MuuToteutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    opintojenLaajuusyksikko: Option[KoodiUri],
    opintojenLaajuusNumeroMin: Option[Double],
    opintojenLaajuusNumeroMax: Option[Double],
    taiteenala: Seq[KoodiUri],
    opetus: Option[OpetusIndexed],
    asiasanat: List[Keyword],
    ammattinimikkeet: List[Keyword],
    yhteyshenkilot: Seq[Yhteyshenkilo],
    isHakukohteetKaytossa: Option[Boolean],
    hakutermi: Option[Hakutermi],
    hakulomaketyyppi: Option[Hakulomaketyyppi],
    hakulomakeLinkki: Kielistetty,
    lisatietoaHakeutumisesta: Kielistetty,
    lisatietoaValintaperusteista: Kielistetty,
    hakuaika: Option[Ajanjakso],
    aloituspaikat: Option[Int],
    aloituspaikkakuvaus: Kielistetty
) extends TutkintoonJohtamatonToteutusMetadataIndexed {
  override def toToteutusMetadata: MuuToteutusMetadata =
    MuuToteutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
      opintojenLaajuusNumeroMin = opintojenLaajuusNumeroMin,
      opintojenLaajuusNumeroMax = opintojenLaajuusNumeroMax,
      opetus = opetus.map(_.toOpetus),
      asiasanat = asiasanat,
      ammattinimikkeet = ammattinimikkeet,
      yhteyshenkilot = yhteyshenkilot,
      isHakukohteetKaytossa = isHakukohteetKaytossa,
      hakutermi = hakutermi,
      hakulomaketyyppi = hakulomaketyyppi,
      hakulomakeLinkki = hakulomakeLinkki,
      lisatietoaHakeutumisesta = lisatietoaHakeutumisesta,
      lisatietoaValintaperusteista = lisatietoaValintaperusteista,
      hakuaika = hakuaika,
      aloituspaikat = aloituspaikat,
      aloituspaikkakuvaus = aloituspaikkakuvaus
    )
}
