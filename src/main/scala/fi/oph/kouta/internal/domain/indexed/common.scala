package fi.oph.kouta.internal.domain.indexed

import com.fasterxml.jackson.annotation.{JsonCreator, JsonProperty}

import java.util.UUID
import fi.oph.kouta.internal.domain.{Kielistetty, Lisatieto, Valintakoe, Valintakoetilaisuus}
import fi.oph.kouta.internal.domain.oid.{OrganisaatioOid, UserOid}

case class Muokkaaja(oid: UserOid)

case class Organisaatio(oid: OrganisaatioOid, nimi: Kielistetty)

case class KoodiUri(koodiUri: String)

case class UuidObject(id: UUID)

case class LisatietoIndexed(otsikko: KoodiUri, teksti: Kielistetty) {
  def toLisatieto = Lisatieto(otsikkoKoodiUri = otsikko.koodiUri, teksti)
}

case class ValintakoeMetadataIndexed(vahimmaispisteet: Option[Double])

case class ValintakoeIndexed(
    id: Option[UUID],
    tyyppi: Option[KoodiUri],
    tilaisuudet: Option[List[Valintakoetilaisuus]],
    vahimmaispisteet: Option[Double],
    metadata: Option[ValintakoeMetadataIndexed]
) {
  def toValintakoe: Valintakoe = Valintakoe(
    id = id,
    tyyppi = tyyppi.map(_.koodiUri),
    tilaisuudet = tilaisuudet,
    vahimmaispisteet = metadata.flatMap(_.vahimmaispisteet)
  )
}

case class AikaJakso @JsonCreator() (
    @JsonProperty("alkaa") alkaa: String,
    @JsonProperty("formatoituAlkaa") formatoituAlkaa: Map[String, String],
    @JsonProperty("formatoituPaattyy") formatoituPaattyy: Map[String, String],
    @JsonProperty("paattyy") paattyy: String
)

case class KoulutuksenAlkamiskausiHakukohdeES @JsonCreator() (
    @JsonProperty("alkamiskausityyppi") alkamiskausityyppi: String,
    @JsonProperty("henkilokohtaisenSuunnitelmanLisatiedot") henkilokohtaisenSuunnitelmanLisatiedot: Map[String, String],
    @JsonProperty("koulutuksenAlkamispaivamaara") koulutuksenAlkamispaivamaara: String,
    @JsonProperty("koulutuksenPaattymispaivamaara") koulutuksenPaattymispaivamaara: String,
    @JsonProperty("koulutuksenAlkamiskausi") koulutuksenAlkamiskausi: KoulutuksenAlkamiskausiMapES,
    @JsonProperty("koulutuksenAlkamisvuosi") koulutuksenAlkamisvuosi: String
)

case class KoulutuksenAlkamiskausiMapES @JsonCreator() (
    @JsonProperty("koodiUri") koodiUri: String,
    @JsonProperty("nimi") nimi: Map[String, String] = Map()
)

case class ValintakoeES @JsonCreator() (
    @JsonProperty("id") id: String,
    @JsonProperty("tyyppi") tyyppi: Option[ValintakoeTyyppiES],
    @JsonProperty("nimi") nimi: Map[String, String] = Map(),
    @JsonProperty("metadata") metadata: Option[ValintaKoeMetadataES],
    @JsonProperty("tilaisuudet") tilaisuudet: List[ValintakoeTilaisuus] = List()
)
case class ValintakoeTyyppiES @JsonCreator() (
    @JsonProperty("koodiUri") koodiUri: String,
    @JsonProperty("nimi") nimi: Map[String, String]
)
case class ValintakoeTilaisuus @JsonCreator() (
    @JsonProperty("aika") aika: Option[AikaJakso],
    @JsonProperty("jarjestamispaikka") jarjestamispaikka: Map[String, String] = Map(),
    @JsonProperty("lisatietoja") lisatietoja: Map[String, String] = Map(),
    @JsonProperty("osoite") osoite: Option[OsoiteES]
)

case class ValintaKoeMetadataES @JsonCreator() (
    @JsonProperty("liittyyEnnakkovalmistautumista") liittyyEnnakkovalmistautumista: Option[Boolean],
    @JsonProperty("ohjeetEnnakkovalmistautumiseen") ohjeetEnnakkovalmistautumiseen: Map[String, String] = Map(),
    @JsonProperty("erityisjarjestelytMahdollisia") erityisjarjestelytMahdollisia: Option[Boolean],
    @JsonProperty("ohjeetErityisjarjestelyihin") ohjeetErityisjarjestelyihin: Map[String, String] = Map(),
    @JsonProperty("tietoja") tietoja: Map[String, String] = Map(),
    @JsonProperty("vahimmaispisteet") vahimmaispisteet: Option[Double]
)

case class OsoiteES @JsonCreator() (
    @JsonProperty("osoite") osoite: Map[String, String] = Map(),
    @JsonProperty("postinumeroKoodiUri") postinumeroKoodiUri: String,
    @JsonProperty("postitoimipaikka") postitoimipaikka: Map[String, String] = Map()
)
case class MuokkaajaES @JsonCreator() (@JsonProperty("nimi") nimi: String, @JsonProperty("oid") oid: String)
