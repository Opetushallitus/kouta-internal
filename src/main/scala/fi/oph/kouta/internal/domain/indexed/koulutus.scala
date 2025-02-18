package fi.oph.kouta.internal.domain.indexed

import fi.oph.kouta.domain._
import fi.oph.kouta.internal.domain._
import fi.oph.kouta.internal.domain.enums.{Julkaisutila, Kieli}
import fi.oph.kouta.internal.domain.oid._
import fi.oph.kouta.logging.Logging

import java.time.LocalDateTime
import java.util.UUID

case class KoulutusIndexed(
    oid: KoulutusOid,
    johtaaTutkintoon: Boolean,
    koulutustyyppi: Option[Koulutustyyppi],
    koulutukset: Seq[KoodiUri],
    tila: Julkaisutila,
    tarjoajat: Option[List[Organisaatio]],
    nimi: Kielistetty,
    metadata: Option[KoulutusMetadataIndexed],
    julkinen: Boolean,
    sorakuvausId: Option[UUID],
    muokkaaja: Muokkaaja,
    organisaatio: Option[Organisaatio],
    kielivalinta: Seq[Kieli],
    modified: Option[LocalDateTime],
    externalId: Option[String],
    koulutuskoodienAlatJaAsteet: Seq[KoulutusKoodienAlatJaAsteet],
    haut: Seq[HakuOid]
) extends WithTila
    with Logging {
  def toKoulutus: Koulutus = {
    try {
      Koulutus(
        oid = oid,
        johtaaTutkintoon = johtaaTutkintoon,
        koulutustyyppi = koulutustyyppi,
        koulutusKoodiUrit = koulutukset.map(_.koodiUri),
        tila = tila,
        tarjoajat = tarjoajat.toList.flatten.map(_.oid),
        nimi = nimi,
        metadata = metadata.map(_.toKoulutusMetadata),
        julkinen = julkinen,
        sorakuvausId = sorakuvausId,
        muokkaaja = muokkaaja.oid,
        organisaatioOid = organisaatio.get.oid,
        kielivalinta = kielivalinta,
        modified = modified,
        externalId = externalId
      )
    } catch {
      case e: Exception =>
        val msg: String = s"Failed to create Koulutus ($oid)"
        logger.error(msg, e)
        throw new RuntimeException(msg, e)
    }
  }

  def toOdwKoulutus: OdwKoulutus = {
    try {
      OdwKoulutus(
        oid = oid,
        johtaaTutkintoon = johtaaTutkintoon,
        koulutustyyppi = koulutustyyppi,
        koulutusKoodiUrit = koulutukset.map(_.koodiUri),
        tila = tila,
        tarjoajat = tarjoajat.toList.flatten.map(_.oid),
        nimi = nimi,
        metadata = metadata.map(_.toKoulutusMetadata),
        julkinen = julkinen,
        sorakuvausId = sorakuvausId,
        muokkaaja = muokkaaja.oid,
        organisaatioOid = organisaatio.get.oid,
        kielivalinta = kielivalinta,
        modified = modified,
        externalId = externalId,
        koulutuskoodienAlatJaAsteet = koulutuskoodienAlatJaAsteet
      )
    } catch {
      case e: Exception =>
        val msg: String = s"Failed to create OdwKoulutus ($oid)"
        logger.error(msg, e)
        throw new RuntimeException(msg, e)
    }
  }
}

sealed trait KoulutusMetadataIndexed {
  val tyyppi: Koulutustyyppi
  val kuvaus: Kielistetty
  val lisatiedot: Seq[LisatietoIndexed]

  def toKoulutusMetadata: KoulutusMetadata
}

case class AmmatillinenKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed]
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: AmmatillinenKoulutusMetadata =
    AmmatillinenKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto)
    )
}

case class AmmatillinenTutkinnonOsaKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    tutkinnonOsat: Seq[TutkinnonOsaIndexed]
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: AmmatillinenTutkinnonOsaKoulutusMetadata =
    AmmatillinenTutkinnonOsaKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      tutkinnonOsat = tutkinnonOsat.map(_.toTutkinnonOsa)
    )
}

case class TutkinnonOsaIndexed(
    ePerusteId: Option[Long],
    koulutus: Option[KoodiUri],
    tutkinnonosaId: Option[Long],
    tutkinnonosaViite: Option[Long]
) {
  def toTutkinnonOsa: TutkinnonOsa = TutkinnonOsa(
    ePerusteId = ePerusteId,
    koulutusKoodiUri = koulutus.map(_.koodiUri),
    tutkinnonosaId = tutkinnonosaId,
    tutkinnonosaViite = tutkinnonosaViite
  )
}

case class AmmatillinenOsaamisalaKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    osaamisala: Option[KoodiUri]
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: AmmatillinenOsaamisalaKoulutusMetadata =
    AmmatillinenOsaamisalaKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      osaamisalaKoodiUri = osaamisala.map(_.koodiUri)
    )
}

trait KorkeakoulutusKoulutusMetadataIndexed extends KoulutusMetadataIndexed {
  val tutkintonimike: Seq[KoodiUri]
  val opintojenLaajuusyksikko: Option[KoodiUri]
  val opintojenLaajuusNumero: Option[Double]
  val koulutusala: Seq[KoodiUri]
}

case class YliopistoKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    koulutusala: Seq[KoodiUri],
    tutkintonimike: Seq[KoodiUri],
    opintojenLaajuusyksikko: Option[KoodiUri] = None,
    opintojenLaajuusNumero: Option[Double] = None
) extends KorkeakoulutusKoulutusMetadataIndexed {
  override def toKoulutusMetadata: YliopistoKoulutusMetadata = YliopistoKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
    tutkintonimikeKoodiUrit = tutkintonimike.map(_.koodiUri),
    opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
    opintojenLaajuusNumero = opintojenLaajuusNumero
  )
}

case class AmmattikorkeakouluKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    koulutusala: Seq[KoodiUri],
    tutkintonimike: Seq[KoodiUri],
    opintojenLaajuusyksikko: Option[KoodiUri] = None,
    opintojenLaajuusNumero: Option[Double] = None
) extends KorkeakoulutusKoulutusMetadataIndexed {
  override def toKoulutusMetadata: AmmattikorkeakouluKoulutusMetadata = AmmattikorkeakouluKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
    tutkintonimikeKoodiUrit = tutkintonimike.map(_.koodiUri),
    opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
    opintojenLaajuusNumero = opintojenLaajuusNumero
  )
}

case class AmmOpeErityisopeJaOpoKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = AmmOpeErityisopeJaOpo,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    koulutusala: Seq[KoodiUri],
    tutkintonimike: Seq[KoodiUri],
    opintojenLaajuusyksikko: Option[KoodiUri] = None,
    opintojenLaajuusNumero: Option[Double] = None
) extends KorkeakoulutusKoulutusMetadataIndexed {
  override def toKoulutusMetadata: AmmOpeErityisopeJaOpoKoulutusMetadata = AmmOpeErityisopeJaOpoKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
    tutkintonimikeKoodiUrit = tutkintonimike.map(_.koodiUri),
    opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
    opintojenLaajuusNumero = opintojenLaajuusNumero
  )
}

case class OpePedagOpinnotKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = OpePedagOpinnot,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    koulutusala: Seq[KoodiUri],
    tutkintonimike: Seq[KoodiUri],
    opintojenLaajuusyksikko: Option[KoodiUri] = None,
    opintojenLaajuusNumero: Option[Double] = None
) extends KorkeakoulutusKoulutusMetadataIndexed {
  override def toKoulutusMetadata: OpePedagOpinnotKoulutusMetadata = OpePedagOpinnotKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
    tutkintonimikeKoodiUrit = tutkintonimike.map(_.koodiUri),
    opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
    opintojenLaajuusNumero = opintojenLaajuusNumero
  )
}

case class KkOpintojaksoKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = KkOpintojakso,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    koulutusala: Seq[KoodiUri],
    tutkintonimike: Seq[KoodiUri],
    opintojenLaajuusyksikko: Option[KoodiUri] = None,
    opintojenLaajuusNumeroMin: Option[Double] = None,
    opintojenLaajuusNumeroMax: Option[Double] = None
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: KkOpintojaksoKoulutusMetadata = KkOpintojaksoKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
    opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
    opintojenLaajuusNumeroMin = opintojenLaajuusNumeroMin,
    opintojenLaajuusNumeroMax = opintojenLaajuusNumeroMax
  )
}

case class LukioKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = Lk,
    kuvaus: Kielistetty = Map.empty,
    lisatiedot: Seq[LisatietoIndexed] = Seq.empty,
    opintojenLaajuus: Option[KoodiUri],
    koulutusala: Seq[KoodiUri],
    opintojenLaajuusyksikko: Option[KoodiUri] = None,
    opintojenLaajuusNumero: Option[Double] = None
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: LukioKoulutusMetadata = LukioKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
    opintojenLaajuusNumero = opintojenLaajuusNumero,
    koulutusalaKoodiUrit = koulutusala.map(_.koodiUri)
  )
}

case class TuvaKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    linkkiEPerusteisiin: Kielistetty,
    opintojenLaajuusyksikko: Option[KoodiUri] = None,
    opintojenLaajuusNumero: Option[Double] = None
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: TuvaKoulutusMetadata =
    TuvaKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      linkkiEPerusteisiin = linkkiEPerusteisiin,
      opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
      opintojenLaajuusNumero = opintojenLaajuusNumero
    )
}

case class TelmaKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    linkkiEPerusteisiin: Kielistetty,
    opintojenLaajuusyksikko: Option[KoodiUri] = None,
    opintojenLaajuusNumero: Option[Double] = None
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: TelmaKoulutusMetadata =
    TelmaKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      linkkiEPerusteisiin = linkkiEPerusteisiin,
      opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
      opintojenLaajuusNumero = opintojenLaajuusNumero
    )
}

case class AmmatillinenMuuKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    koulutusala: Seq[KoodiUri],
    opintojenLaajuusyksikko: Option[KoodiUri] = None,
    opintojenLaajuusNumero: Option[Double] = None
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: AmmatillinenMuuKoulutusMetadata =
    AmmatillinenMuuKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
      opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
      opintojenLaajuusNumero = opintojenLaajuusNumero
    )
}

case class VapaaSivistystyoKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    linkkiEPerusteisiin: Kielistetty,
    opintojenLaajuusyksikko: Option[KoodiUri] = None,
    opintojenLaajuusNumero: Option[Double] = None
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: VapaaSivistystyoKoulutusMetadata =
    VapaaSivistystyoKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      linkkiEPerusteisiin = linkkiEPerusteisiin,
      opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
      opintojenLaajuusNumero = opintojenLaajuusNumero
    )
}

case class AikuistenPerusopetusKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    linkkiEPerusteisiin: Kielistetty,
    opintojenLaajuusyksikko: Option[KoodiUri] = None,
    opintojenLaajuusNumero: Option[Double] = None
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: AikuistenPerusopetusKoulutusMetadata =
    AikuistenPerusopetusKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      linkkiEPerusteisiin = linkkiEPerusteisiin,
      opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
      opintojenLaajuusNumero = opintojenLaajuusNumero
    )
}

case class ErikoislaakariKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    koulutusala: Seq[KoodiUri] = Seq.empty,
    tutkintonimike: Seq[KoodiUri] = Seq.empty
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: ErikoislaakariKoulutusMetadata =
    ErikoislaakariKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
      tutkintonimikeKoodiUrit = tutkintonimike.map(_.koodiUri)
    )
}

case class KkOpintokokonaisuusKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    koulutusala: Seq[KoodiUri],
    opintojenLaajuusNumeroMin: Option[Double],
    opintojenLaajuusNumeroMax: Option[Double],
    opintojenLaajuusyksikko: Option[KoodiUri]
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: KkOpintokokonaisuusKoulutusMetadata =
    KkOpintokokonaisuusKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
      opintojenLaajuusNumeroMin = opintojenLaajuusNumeroMin,
      opintojenLaajuusNumeroMax = opintojenLaajuusNumeroMax,
      opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri)
    )
}

case class ErikoistumisKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    erikoistumiskoulutus: Option[KoodiUri],
    koulutusala: Seq[KoodiUri],
    opintojenLaajuusNumeroMin: Option[Double],
    opintojenLaajuusNumeroMax: Option[Double],
    opintojenLaajuusyksikko: Option[KoodiUri]
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: ErikoistumiskoulutusMetadata =
    ErikoistumiskoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      erikoistumiskoulutusKoodiUri = erikoistumiskoulutus.map(_.koodiUri),
      koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
      opintojenLaajuusNumeroMin = opintojenLaajuusNumeroMin,
      opintojenLaajuusNumeroMax = opintojenLaajuusNumeroMax,
      opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri)
    )
}

case class TaiteenPerusopetusKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    linkkiEPerusteisiin: Kielistetty
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: TaiteenPerusopetusKoulutusMetadata =
    TaiteenPerusopetusKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      linkkiEPerusteisiin = linkkiEPerusteisiin
    )
}

case class MuuKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = Muu,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    koulutusala: Seq[KoodiUri] = Seq.empty,
    opintojenLaajuusyksikko: Option[KoodiUri],
    opintojenLaajuusNumeroMin: Option[Double],
    opintojenLaajuusNumeroMax: Option[Double]
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: MuuKoulutusMetadata =
    MuuKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
      opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
      opintojenLaajuusNumeroMin = opintojenLaajuusNumeroMin,
      opintojenLaajuusNumeroMax = opintojenLaajuusNumeroMax
    )
}
