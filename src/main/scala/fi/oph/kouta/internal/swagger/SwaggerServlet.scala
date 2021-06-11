package fi.oph.kouta.internal.swagger

import org.reflections.Reflections
import org.scalatra.ScalatraServlet

import scala.collection.JavaConverters._

class SwaggerServlet extends ScalatraServlet {

  get("/swagger.yaml") {
    response.setContentType("text/yaml")
    renderOpenapi3Yaml
  }

  protected lazy val renderOpenapi3Yaml: String = {
    val header =
      """
        |openapi: 3.0.0
        |info:
        |  title: kouta-internal
        |  description: >
        |    Uuden koulutustarjonnan Opintopolun sisäisille palveluille tarkoitetut rajapinnat.
        |
        |
        |    Ohjeita rajapintojen kutsujalle:
        |
        |
        |    * [Autentikaatio](https://wiki.eduuni.fi/x/L95cCw)
        |
        |    * [Kutsujan tunniste (caller-id)](https://wiki.eduuni.fi/x/0MeKCg)
        |
        |    * [CSRF-suojaus](https://wiki.eduuni.fi/x/EL_hCw)
        |
        |
        |    Helpoin tapa kirjautua sisään Swagger-ui:n käyttäjälle on avata
        |    [/kouta-internal/auth/login](/kouta-internal/auth/login) uuteen selainikkunaan.
        |    Jos näkyviin tulee `{"personOid":"1.2.246.562.24.xxxx"}` on kirjautuminen onnistunut. Jos näkyviin tulee
        |    opintopolun kirjautumisikkuna, kirjaudu sisään.
        |  termsOfService: https://opintopolku.fi/wp/fi/opintopolku/tietoa-palvelusta/
        |  contact:
        |    name: "Opetushallitus"
        |    email: verkkotoimitus_opintopolku@oph.fi
        |    url: "https://www.oph.fi/"
        |  license:
        |    name: "EUPL 1.1 or latest approved by the European Commission"
        |    url: "http://www.osor.eu/eupl/"
        |servers:
        |  - url: /kouta-internal/
        |paths:
        |""".stripMargin

    val paths = SwaggerPaths.paths.map { case (path, op) =>
      s"""  $path:
         |    parameters:
         |      - $$ref: '#/components/parameters/callerId'
         |""".stripMargin +
        op.mkString
    }.mkString

    val componentsHeader =
      s"""
         |components:
         |  parameters:
         |    callerId:
         |      in: header
         |      name: Caller-Id
         |      schema:
         |        type: string
         |        default: kouta-internal-swagger
         |      required: true
         |      description: Kutsujan [Caller ID](https://wiki.eduuni.fi/x/0MeKCg)
         |  schemas:
         |""".stripMargin

    Seq(header, paths, componentsHeader, getModelAnnotations).mkString
  }

  private def getModelAnnotations: String = {
    val reflections = new Reflections("fi.oph.kouta.internal")

    reflections
      .getTypesAnnotatedWith(classOf[SwaggerModel])
      .asScala
      .toSeq
      .sortBy(_.getSimpleName)
      .map(_.getAnnotation(classOf[SwaggerModel]))
      .filter(_ != null)
      .map(_.value.stripMargin)
      .mkString
  }
}
