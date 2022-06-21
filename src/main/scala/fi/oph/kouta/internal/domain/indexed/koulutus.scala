package fi.oph.kouta.internal.domain.indexed

import fi.oph.kouta.domain.{AmmOpeErityisopeJaOpo, KkOpintojakso, Koulutustyyppi, Lk}
import fi.oph.kouta.internal.domain._
import fi.oph.kouta.internal.domain.enums.{Julkaisutila, Kieli}
import fi.oph.kouta.internal.domain.oid._
import fi.vm.sade.utils.slf4j.Logging

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
    externalId: Option[String]
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
  val kuvauksenNimi: Kielistetty
  val tutkintonimike: Seq[KoodiUri]
  val opintojenLaajuus: Option[KoodiUri]
  val koulutusala: Seq[KoodiUri]
}

case class YliopistoKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    koulutusala: Seq[KoodiUri],
    tutkintonimike: Seq[KoodiUri],
    opintojenLaajuus: Option[KoodiUri],
    kuvauksenNimi: Kielistetty
) extends KorkeakoulutusKoulutusMetadataIndexed {
  override def toKoulutusMetadata: YliopistoKoulutusMetadata = YliopistoKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
    tutkintonimikeKoodiUrit = tutkintonimike.map(_.koodiUri),
    opintojenLaajuusKoodiUri = opintojenLaajuus.map(_.koodiUri),
    kuvauksenNimi = kuvauksenNimi
  )
}

case class AmmattikorkeakouluKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    koulutusala: Seq[KoodiUri],
    tutkintonimike: Seq[KoodiUri],
    opintojenLaajuus: Option[KoodiUri],
    kuvauksenNimi: Kielistetty
) extends KorkeakoulutusKoulutusMetadataIndexed {
  override def toKoulutusMetadata: AmmattikorkeakouluKoulutusMetadata = AmmattikorkeakouluKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
    tutkintonimikeKoodiUrit = tutkintonimike.map(_.koodiUri),
    opintojenLaajuusKoodiUri = opintojenLaajuus.map(_.koodiUri),
    kuvauksenNimi = kuvauksenNimi
  )
}

case class AmmOpeErityisopeJaOpoKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = AmmOpeErityisopeJaOpo,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    koulutusala: Seq[KoodiUri],
    tutkintonimike: Seq[KoodiUri],
    opintojenLaajuus: Option[KoodiUri],
    kuvauksenNimi: Kielistetty
) extends KorkeakoulutusKoulutusMetadataIndexed {
  override def toKoulutusMetadata: AmmOpeErityisopeJaOpoKoulutusMetadata = AmmOpeErityisopeJaOpoKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
    tutkintonimikeKoodiUrit = tutkintonimike.map(_.koodiUri),
    opintojenLaajuusKoodiUri = opintojenLaajuus.map(_.koodiUri),
    kuvauksenNimi = kuvauksenNimi
  )
}

case class KkOpintojaksoKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = KkOpintojakso,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    koulutusala: Seq[KoodiUri],
    tutkintonimike: Seq[KoodiUri],
    opintojenLaajuusyksikko: Option[KoodiUri] = None,
    opintojenLaajuusNumero: Option[Double] = None,
    kuvauksenNimi: Kielistetty
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: KkOpintojaksoKoulutusMetadata = KkOpintojaksoKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    koulutusalaKoodiUrit = koulutusala.map(_.koodiUri),
    kuvauksenNimi = kuvauksenNimi,
    opintojenLaajuusyksikkoKoodiUri = opintojenLaajuusyksikko.map(_.koodiUri),
    opintojenLaajuusNumero = opintojenLaajuusNumero
  )
}

case class LukioKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi = Lk,
    kuvaus: Kielistetty = Map.empty,
    lisatiedot: Seq[LisatietoIndexed] = Seq.empty,
    opintojenLaajuus: Option[KoodiUri],
    koulutusala: Seq[KoodiUri]
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: LukioKoulutusMetadata = LukioKoulutusMetadata(
    tyyppi = tyyppi,
    kuvaus = kuvaus,
    lisatiedot = lisatiedot.map(_.toLisatieto),
    opintojenLaajuusKoodiUri = opintojenLaajuus.map(_.koodiUri),
    koulutusalaKoodiUrit = koulutusala.map(_.koodiUri)
  )
}

case class TuvaKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    linkkiEPerusteisiin: Kielistetty,
    opintojenLaajuus: Option[KoodiUri] = None
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: TuvaKoulutusMetadata =
    TuvaKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      linkkiEPerusteisiin = linkkiEPerusteisiin,
      opintojenLaajuusKoodiUri = opintojenLaajuus.map(_.koodiUri)
    )
}

case class TelmaKoulutusMetadataIndexed(
    tyyppi: Koulutustyyppi,
    kuvaus: Kielistetty,
    lisatiedot: Seq[LisatietoIndexed],
    linkkiEPerusteisiin: Kielistetty,
    opintojenLaajuus: Option[KoodiUri] = None
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: TelmaKoulutusMetadata =
    TelmaKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      linkkiEPerusteisiin = linkkiEPerusteisiin,
      opintojenLaajuusKoodiUri = opintojenLaajuus.map(_.koodiUri)
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
    opintojenLaajuus: Option[KoodiUri] = None
) extends KoulutusMetadataIndexed {
  override def toKoulutusMetadata: VapaaSivistystyoKoulutusMetadata =
    VapaaSivistystyoKoulutusMetadata(
      tyyppi = tyyppi,
      kuvaus = kuvaus,
      lisatiedot = lisatiedot.map(_.toLisatieto),
      linkkiEPerusteisiin = linkkiEPerusteisiin,
      opintojenLaajuusKoodiUri = opintojenLaajuus.map(_.koodiUri)
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
