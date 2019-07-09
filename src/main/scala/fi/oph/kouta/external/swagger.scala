package fi.oph.kouta.external

import java.time.LocalDateTime
import java.util.UUID

import fi.oph.kouta.external.domain.enums.{Julkaisutila, Kieli, Koulutustyyppi}
import fi.oph.kouta.external.domain.{KorkeakoulutusKoulutusMetadata, Lisatieto}
import fi.oph.kouta.external.util.KoutaJsonFormats
import org.scalatra.ScalatraServlet
import org.scalatra.swagger.DataType.ValueDataType
import org.scalatra.swagger._

class SwaggerServlet(implicit val swagger: Swagger) extends ScalatraServlet with JacksonSwaggerBase

class KoutaExternalSwagger
    extends Swagger(
      Swagger.SpecVersion,
      "0.1-SNAPSHOT",
      ApiInfo(
        "kouta-external",
        "Koulutustarjonnan ulkoiset rajapinnat",
        "https://opintopolku.fi/wp/fi/opintopolku/tietoa-palvelusta/",
        "verkkotoimitus_opintopolku@oph.fi",
        "EUPL 1.1 or latest approved by the European Commission",
        "http://www.osor.eu/eupl/"
      )
    )

trait PrettySwaggerSupport extends SwaggerSupport with KoutaJsonFormats {

  private val exampleDateTime = ISO_LOCAL_DATE_TIME_FORMATTER.format(LocalDateTime.MIN)
  private val exampleUuid     = UUID.randomUUID().toString

  private def exampleOid(i: Int) = List(s"1.2.246.562.$i.123456")

  val modelName: String

  private def modelProperty(position: Int, values: List[String]) =
    ModelProperty(
      `type` = DataType.String,
      position = position,
      required = false,
      description = Some(values.mkString("/")),
      allowableValues = AllowableValues.apply(values)
    )

  private def modelPropertyList(position: Int, values: String) = ModelProperty(
    DataType.GenList(DataType.String),
    position,
    description = Some(values)
  )

  def prettifySwaggerModels(model: String = modelName): Unit = {
    removeRedundantModels()
    models.foreach { case (k, m) => models.update(k, m.copy(properties = prettifyEnumModels(m))) }
    prettifyKielistetty()
    models.foreach { case (k, m) => models.update(k, m.copy(properties = prettifyDataTypes(m))) }
  }

  private def removeRedundantModels() = {
    models.remove("Hakulomaketyyppi")
    models.remove("Julkaisutila")
    models.remove("Koulutustyyppi")
    models.remove("LiitteenToimitustapa")
    models.remove("Kieli")
    models.remove("UUID")
    models.remove("Pattern")
    models.remove("LocalDate")
    models.remove("LocalDateTime")
    models.remove("LocalTime")
    models.remove("HakuOid")
    models.remove("HakukohdeOid")
    models.remove("KoulutusOid")
    models.remove("ToteutusOid")
    models.remove("UserOid")
    models.remove("OrganisaatioOid")
    models.remove("GenericOid")
    models.remove("Oid")

    models.put("KorkeakoulutusKoulutusMetadata", Swagger.modelToSwagger[KorkeakoulutusKoulutusMetadata].get)
    models.put("Lisatieto", Swagger.modelToSwagger[Lisatieto].get)
  }

  private def prettifyEnumModels(model: Model): List[(String, ModelProperty)] = model.properties.map {
    //case ("hakulomaketyyppi", mp) => ("hakulomaketyyppi", modelProperty(mp.position, Hakulomaketyyppi.values().map(_.toString)))
    //case ("liitteidenToimitustapa", mp) => ("liitteidenToimitustapa", modelProperty(mp.position, LiitteenToimitustapa.values().map(_.toString)))
    //case ("toimitustapa", mp) => ("toimitustapa", modelProperty(mp.position, LiitteenToimitustapa.values().map(_.toString)))
    case ("id", mp)               => ("id", modelProperty(mp.position, List(exampleUuid)))
    case ("oid", mp)              => ("oid", modelProperty(mp.position, exampleOid(123)))
    case ("tila", mp)             => ("tila", modelProperty(mp.position, Julkaisutila.all.map(_.toString)))
    case ("kieli", mp)            => ("kieli", modelProperty(mp.position, Kieli.all.map(_.toString)))
    case ("tyyppi", mp)           => ("tyyppi", modelProperty(mp.position, Koulutustyyppi.all.map(_.toString)))
    case ("tarjoajat", mp)        => ("tarjoajat", modelPropertyList(mp.position, s"${exampleOid(10).head}"))
    case ("kielivalinta", mp)     => ("kielivalinta", modelPropertyList(mp.position, s"${Kieli.all.mkString(",")}"))
    case ("koulutustyyppi", mp)   => ("koulutustyyppi", modelProperty(mp.position, Koulutustyyppi.all.map(_.toString)))
    case ("valintaperusteet", mp) => ("valintaperusteet", modelPropertyList(mp.position, exampleUuid))
    case p                        => p
  }

  private def prettifyKielistetty() = {
    models.put(
      "Kielistetty",
      Model("Kielistetty", "Kielistetty", properties = Kieli.all.zipWithIndex.map {
        case (k, i) => (k.toString, modelProperty(i + 1, List(s"nimi ${k.toString}")))
      })
    )
    models.put(
      "Keyword",
      Model(
        "Keyword",
        "Keyword",
        properties =
          List(("kieli", modelProperty(0, Kieli.all.map(_.toString))), ("arvo", ModelProperty(DataType.String, 1)))
      )
    )
  }

  private def prettifyDataTypes(model: Model): List[(String, ModelProperty)] = model.properties.map {
    case (name, mp) if mp.`type`.name.equals("Instant")         => (name, modelProperty(mp.position, List(exampleDateTime)))
    case (name, mp) if mp.`type`.name.equals("UUID")            => (name, modelProperty(mp.position, List(exampleUuid)))
    case (name, mp) if mp.`type`.name.equals("KoulutusOid")     => (name, modelProperty(mp.position, exampleOid(13)))
    case (name, mp) if mp.`type`.name.equals("ToteutusOid")     => (name, modelProperty(mp.position, exampleOid(17)))
    case (name, mp) if mp.`type`.name.equals("HakuOid")         => (name, modelProperty(mp.position, exampleOid(29)))
    case (name, mp) if mp.`type`.name.equals("HakukohdeOid")    => (name, modelProperty(mp.position, exampleOid(20)))
    case (name, mp) if mp.`type`.name.equals("UserOid")         => (name, modelProperty(mp.position, exampleOid(17)))
    case (name, mp) if mp.`type`.name.equals("OrganisaatioOid") => (name, modelProperty(mp.position, exampleOid(10)))
    case (name, mp) if mp.`type`.name.equals("Oid")             => (name, modelProperty(mp.position, exampleOid(24)))
    case (name, mp) if mp.`type`.name.equals("Map") =>
      (name, ModelProperty(ValueDataType("Kielistetty", None, Some("fi.oph.kouta.domain.Kielistetty")), mp.position))
    case (name, mp) if mp.`type`.name.equals("KoulutusMetadata") =>
      val a = DataType.apply[KorkeakoulutusKoulutusMetadata]
      (name, ModelProperty(a, mp.position))
    case p => p
  }
}
