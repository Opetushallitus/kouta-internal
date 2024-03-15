package fi.oph.kouta.internal.integration

import co.elastic.clients.elasticsearch
import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.query_dsl.{
  ExistsQuery,
  QueryBuilders,
  TermQuery,
  TermsQuery,
  TermsQueryField,
  WildcardQuery
}
import co.elastic.clients.elasticsearch.core.SearchRequest
import fi.oph.kouta.domain.Alkamiskausityyppi
import fi.oph.kouta.internal.KoutaConfigurationFactory
import fi.oph.kouta.internal.client.OrganisaatioClient
import fi.oph.kouta.internal.domain.Kielistetty
import fi.oph.kouta.internal.domain.enums.{Julkaisutila, Kieli}
import fi.oph.kouta.internal.domain.indexed.KoodiUri
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid}
import fi.oph.kouta.internal.elasticsearch.ElasticsearchHealth
import fi.vm.sade.utils.slf4j.Logging

import java.util
import scala.collection.JavaConverters._

object JavaElasticsearchTesting extends Logging {

  def main(args: Array[String]): Unit = {

//    testAlkamiskausiTyyppi()

//testToKiellistettyMap();
    //testNewHakuQuery()
    testNewHakuKohdeQuery()
  }

  def testNewHakuKohdeQuery(): Unit = {

    //val hakuOid =  Some(HakuOid("1.2.246.562.29.00000000000000038404"))
    val hakuOid = None
    val tarjoajaOids = Some(
      Set(
        OrganisaatioOid("1.2.246.562.10.00000000001"),
        OrganisaatioOid("1.2.246.562.10.45798950973")
      )
    )
//val tarjoajaOids = Some(Set.empty[OrganisaatioOid])

//    val hakukohderyhmanHakukohdeOids = Some(
//      Set(
//        HakukohdeOid("1.2.246.562.28.00000000001"),
//        //OrganisaatioOid("1.2.246.562.10.45798950973")
//      )
//    )

    val hakukohdeKoodi = Option(KoodiUri("jannen testi"))

    val hakukohderyhmanHakukohdeOids = Some(
      Set(
        HakukohdeOid("1.2.246.562.10.00000000001")
      )
    )
    val includeHakukohdeOids = false
    val q                    = Some("Autoalan perustutkinto")
    //val q = None
    val withRootOikeus = false //tarjoajaOids.exists(_.contains(rootOrganisaatioOid))
    //val oikeusHakukohteeseenFn = false
//    OrganisaatioClient.asyncGetAllChildOidsFlat(tarjoajaOids).flatMap(oidsWithChilds => {
    createNewHakukohdeQuery(
      hakuOid,
      tarjoajaOids,
      hakukohdeKoodi,
      q,
      createOikeusFn(withRootOikeus, None),
      hakukohderyhmanHakukohdeOids
    )
    //  }
    //  )

  }

  private def createOikeusFn(
      withRootOikeus: Boolean,
      tarjoajaOids: Option[Set[OrganisaatioOid]]
  ): OrganisaatioOid => Option[Boolean] = { (tarjoaja: OrganisaatioOid) =>
    {
      if (withRootOikeus) {
        Some(true)
      } else {
        tarjoajaOids.map(oids => oids.contains(tarjoaja))
      }
    }
  }
  def createNewHakukohdeQuery(
      hakuOid: Option[HakuOid],
      tarjoajaOids: Option[Set[OrganisaatioOid]],
      hakukohdeKoodi: Option[KoodiUri],
      q: Option[String],
      oikeusHakukohteeseenFn: OrganisaatioOid => Option[Boolean],
      hakukohderyhmanHakukohdeOids: Option[Set[HakukohdeOid]]
  ): Unit = {

    val hakuOidQuery = hakuOid.map(oid =>
      QueryBuilders.bool
        .must(
          TermsQuery
            .of(m =>
              m.field("hakuOid.keyword")
                .terms(
                  new TermsQueryField.Builder()
                    .value(List(FieldValue.of(oid.toString())).asJava)
                    .build()
                )
            )
            ._toQuery()
        )
        .build()
        ._toQuery()
    )

    val tarjoajaQueryNew = None
//  val tarjoajaQueryNew =
//    if (tarjoajaOids.get.isEmpty) None else
//    tarjoajaOids.map(oids =>
//
//      QueryBuilders.bool.must(
//        QueryBuilders.bool.should(
//          oids.map(oid =>
//            QueryBuilders.bool
//              .should(
//              TermsQuery
//                .of(m => m.field("jarjestyspaikka.oid")
//                  .terms(new TermsQueryField.Builder()
//                    .value(List(FieldValue.of(oid.toString)).asJava)
//                    .build()))._toQuery(),
//              QueryBuilders.bool
//
//                .must(
//                  TermsQuery
//                    .of(m => m.field("toteutus.tarjoajat.oid")
//                      .terms(new TermsQueryField.Builder()
//                        .value(List(FieldValue.of(oid.toString())).asJava)
//                        .build()))._toQuery())
//                .mustNot(ExistsQuery.of(m => m.field("jarjestyspaikka"))._toQuery())
//
//                .build()._toQuery()
//            )//.minimumShouldMatch("1") // TODO PALAUTA TÄMÄ
//              .build()._toQuery()
//          ).toList.asJava
//        ).build()._toQuery()
//      ).build()._toQuery()
//  )

    val qQueryNew =
      q.map(q => {
        val wildcardQ = "*" + q + "*"
        QueryBuilders.bool
          .should(
            // TermQuery.of(m => m.field("nimi.fi").value(wildcardQ))._toQuery(),
            WildcardQuery.of(m => m.field("nimi.fi.keyword").value(wildcardQ))._toQuery(),
            WildcardQuery.of(m => m.field("nimi.sv.keyword").value(wildcardQ))._toQuery(),
            WildcardQuery.of(m => m.field("nimi.en.keyword").value(wildcardQ))._toQuery(),
            WildcardQuery.of(m => m.field("jarjestyspaikka.nimi.fi.keyword").value(wildcardQ))._toQuery(),
            WildcardQuery.of(m => m.field("jarjestyspaikka.nimi.sv.keyword").value(wildcardQ))._toQuery(),
            WildcardQuery.of(m => m.field("jarjestyspaikka.nimi.en.keyword").value(wildcardQ))._toQuery(),
            WildcardQuery.of(m => m.field("toteutus.tarjoajat.nimi.fi.keyword").value(wildcardQ))._toQuery(),
            WildcardQuery.of(m => m.field("toteutus.tarjoajat.nimi.sv.keyword").value(wildcardQ))._toQuery(),
            WildcardQuery.of(m => m.field("toteutus.tarjoajat.nimi.en.keyword").value(wildcardQ))._toQuery()
          )
          .build
          ._toQuery()
      })

    /*
val hakukohderyhmanHakukohteetQueryOld =
  hakukohderyhmanHakukohdeOids.map(oids => must(termsQuery("oid", oids.map(oid => oid.toString))))
     */

    val hakukohderyhmanHakukohteetQuery =
      hakukohderyhmanHakukohdeOids
        .map(oid =>
          QueryBuilders.bool
            .must(
              TermsQuery
                .of(m =>
                  m.field("oid")
                    .terms(
                      new TermsQueryField.Builder()
                        .value(oid.toList.map(m => FieldValue.of(m.toString)).asJava)
                        .build()
                    )
                )
                ._toQuery()
            )
            .build()
            ._toQuery()
        )
    //  searchItems[HakukohdeIndexed](Some(must(queryOld ++ hakukohdeKoodiQueryOld++ qQueryOld)))
    val hakukohdeKoodiQuery =
      hakukohdeKoodi.map(oid =>
        QueryBuilders.bool
          .should(
            WildcardQuery.of(m => m.field("hakukohde.koodiUri.keyword").value(oid.koodiUri + "#*"))._toQuery(),
            TermsQuery
              .of(m =>
                m.field("hakukohde.koodiUri.keyword")
                  .terms(
                    new TermsQueryField.Builder()
                      .value(List(FieldValue.of(oid.koodiUri)).asJava)
                      .build()
                  )
              )
              ._toQuery()
          )
          .minimumShouldMatch("1")
          .build()
          ._toQuery()
      )

    val tilaQuery =
      Option.apply(
        QueryBuilders.bool
          .mustNot(
            TermsQuery
              .of(m =>
                m.field("tila.keyword")
                  .terms(
                    new TermsQueryField.Builder()
                      .value(List(FieldValue.of("tallennettu")).asJava)
                      .build()
                  )
              )
              ._toQuery()
          )
          .build()
          ._toQuery()
      )

    val queries = (tarjoajaQueryNew, hakukohderyhmanHakukohteetQuery)
    //val queries = (None, None)

    val query = queries match {
      case (None, None) => Some(QueryBuilders.bool.must(hakuOidQuery.get).build()._toQuery())
      //case (None, None) => Some(QueryBuilders.bool.must(List(hakuOidQuery).flatten.asJava)))
      case (_, _) =>
        Some(
          QueryBuilders.bool
            .must(
              List(
                hakuOidQuery,
                Option(
                  QueryBuilders.bool
                    .minimumShouldMatch("1")
                    .should(
                      List(
                        queries._1,
                        queries._2
                      ).flatten.asJava
                    )
                    .build()
                    ._toQuery()
                )
              ).flatten.asJava
            )
            .build()
            ._toQuery()
        )
    }

    val finalQuery =
      QueryBuilders.bool
        .must(
          List(query, hakukohdeKoodiQuery, qQueryNew).flatten.asJava
        )
        .build()
        ._toQuery()
    var searchRequestBuilder2 =
      new SearchRequest.Builder()
        .query(
          QueryBuilders.bool
            .must(
              finalQuery
            )
            .build()
            ._toQuery()
        )
        .size(500)

    val searchRequest2 = searchRequestBuilder2.build()

    val newHakukohdeQuery = searchRequest2.query().toString
    val origHakukohdeQuery = {
      "{\"query\":{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"terms\":{\"hakuOid.keyword\":[\"1.2.246.562.29.00000000000000038404\"]}}]}},{\"bool\":{\"should\":[{\"bool\":{\"must\":[{\"terms\":{\"oid\":[\"1.2.246.562.10.00000000001\"]}}]}}]}}]}},{\"bool\":{\"should\":[{\"wildcard\":{\"hakukohde.koodiUri.keyword\":{\"value\":\"jannen testi#*\"}}},{\"terms\":{\"hakukohde.koodiUri.keyword\":[\"jannen testi\"]}}]}},{\"bool\":{\"should\":[{\"wildcard\":{\"nimi.fi.keyword\":{\"value\":\"*Autoalan perustutkinto*\"}}},{\"wildcard\":{\"nimi.sv.keyword\":{\"value\":\"*Autoalan perustutkinto*\"}}},{\"wildcard\":{\"nimi.en.keyword\":{\"value\":\"*Autoalan perustutkinto*\"}}},{\"wildcard\":{\"jarjestyspaikka.nimi.fi.keyword\":{\"value\":\"*Autoalan perustutkinto*\"}}},{\"wildcard\":{\"jarjestyspaikka.nimi.sv.keyword\":{\"value\":\"*Autoalan perustutkinto*\"}}},{\"wildcard\":{\"jarjestyspaikka.nimi.en.keyword\":{\"value\":\"*Autoalan perustutkinto*\"}}},{\"wildcard\":{\"toteutus.tarjoajat.nimi.fi.keyword\":{\"value\":\"*Autoalan perustutkinto*\"}}},{\"wildcard\":{\"toteutus.tarjoajat.nimi.sv.keyword\":{\"value\":\"*Autoalan perustutkinto*\"}}},{\"wildcard\":{\"toteutus.tarjoajat.nimi.en.keyword\":{\"value\":\"*Autoalan perustutkinto*\"}}}]}}]}}]}}}"
      //"{\"query\":{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"terms\":{\"hakuOid.keyword\":[\"1.2.246.562.29.00000000000000038404\"]}}]}},{\"bool\":{\"should\":[{\"bool\":{\"must\":[{\"terms\":{\"oid\":[\"1.2.246.562.10.00000000001\"]}}]}}]}}]}},{\"bool\":{\"should\":[{\"wildcard\":{\"hakukohde.koodiUri.keyword\":{\"value\":\"jannen testi#*\"}}},{\"terms\":{\"hakukohde.koodiUri.keyword\":[\"jannen testi\"]}}]}}]}}]}}}"
//        "{\"query\":{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"terms\":{\"hakuOid.keyword\":[\"1.2.246.562.29.00000000000000038404\"]}}]}},{\"bool\":{\"should\":[{\"bool\":{\"must\":[{\"terms\":{\"oid\":[\"1.2.246.562.10.00000000001\"]}}]}}],\"minimum_should_match\":\"1\"}}]}},{\"bool\":{\"should\":[{\"wildcard\":{\"hakukohde.koodiUri.keyword\":{\"value\":\"jannen testi#*\"}}},{\"terms\":{\"hakukohde.koodiUri.keyword\":[\"jannen testi\"]}}],\"minimum_should_match\":\"1\"}}]}}]}}}"

    }

    logger.info("origHakukohdeQuery        = " + origHakukohdeQuery)
    logger.info("newHakukohdeQuery           = " + newHakukohdeQuery)
  }

  def createTermsQuery(fieldName: String, value: String): Unit = {
    //val hakuOidQuery = hakuOid.map(oid =>
    //  QueryBuilders.bool.must(
    TermsQuery
      .of(m =>
        m.field("hakuOid.keyword")
          .terms(
            new TermsQueryField.Builder()
              .value(List(FieldValue.of(value.toString())).asJava)
              .build()
          )
      )
      ._toQuery()
    //).build()._toQuery()
    //)
  }
  def testNewHakuQuery(): Unit = {

    //    val ataruId: Option[String] = Option("66b7b709-1ed0-49cc-bbef-e5b0420a81c9")
    val ataruId = None

    val tarjoajaOids = Some(
      Set(OrganisaatioOid("1.2.246.562.10.64170598482"), OrganisaatioOid("1.2.246.562.10.45798950973"))
    )
    val vuosi: Option[Int]   = Option(2023)
    val includeHakukohdeOids = false
    val newQuery             = createNewHakuQuery(ataruId, tarjoajaOids, vuosi, includeHakukohdeOids)

  }

  def createNewHakuQuery(
      ataruId: Option[String],
      tarjoajaOids: Option[Set[OrganisaatioOid]],
      vuosi: Option[Int],
      includeHakukohdeOids: Boolean
  ): Unit = {

    val ataruIdQuery = ataruId.map(t =>
      (TermsQuery
        .of(m =>
          m.field("hakulomakeAtaruId.keyword")
            .terms(
              new TermsQueryField.Builder()
                .value(ataruId.toList.map(m => FieldValue.of(m)).asJava)
                .build()
            )
        )
        ._toQuery())
    )

    val alkamisvuosiQuery = vuosi.map(t =>
      TermsQuery
        .of(m =>
          m.field("metadata.koulutuksenAlkamiskausi.koulutuksenAlkamisvuosi")
            .terms(
              new TermsQueryField.Builder()
                .value(vuosi.toList.map(m => FieldValue.of(m)).asJava)
                .build()
            )
        )
        ._toQuery()
    )

    //val hakuvuosiQueryOld = vuosi.map(termsQuery("hakuvuosi", _))
    val hakuvuosiQuery = vuosi.map(t =>
      TermsQuery
        .of(m =>
          m.field("hakuvuosi")
            .terms(
              new TermsQueryField.Builder()
                .value(vuosi.toList.map(m => FieldValue.of(m)).asJava)
                .build()
            )
        )
        ._toQuery()
    )
    //    val tarjoajaQueryOld = tarjoajaOids.map(oids =>
    //      should(
    //        oids.map(oid =>
    //          should(
    //            termsQuery("hakukohteet.jarjestyspaikka.oid", oid.toString),
    //            termsQuery("hakukohteet.toteutus.tarjoajat.oid", oid.toString)
    //          )
    //        )
    //      )
    //    )

    val tarjoajaQuery =
      tarjoajaOids.map(oids =>
        QueryBuilders.bool
          .should(
            oids
              .map(oid =>
                QueryBuilders.bool
                  .should(
                    TermsQuery
                      .of(m =>
                        m.field("hakukohteet.jarjestyspaikka.oid")
                          .terms(
                            new TermsQueryField.Builder()
                              .value(List(FieldValue.of(oid.toString())).asJava)
                              .build()
                          )
                      )
                      ._toQuery(),
                    TermsQuery
                      .of(m =>
                        m.field("hakukohteet.toteutus.tarjoajat.oid")
                          .terms(
                            new TermsQueryField.Builder()
                              .value(List(FieldValue.of(oid.toString())).asJava)
                              .build()
                          )
                      )
                      ._toQuery()
                  )
                  .build()
                  ._toQuery()
              )
              .toList
              .asJava
          )
          .build()
          ._toQuery()
      )

    val tilaQuery =
      Option.apply(
        QueryBuilders.bool
          .mustNot(
            TermsQuery
              .of(m =>
                m.field("tila.keyword")
                  .terms(
                    new TermsQueryField.Builder()
                      .value(List(FieldValue.of("tallennettu")).asJava)
                      .build()
                  )
              )
              ._toQuery()
          )
          .build()
          ._toQuery()
      )

    val queryList    = List(ataruIdQuery, tarjoajaQuery, alkamisvuosiQuery, hakuvuosiQuery).flatten.asJava
    val queryInitial = QueryBuilders.bool.must(queryList).build._toQuery()
    val queryList2   = List(tilaQuery, Some(queryInitial)).flatten.asJava

    val query2 = QueryBuilders.bool.must(queryList2).build._toQuery()

    var searchRequestBuilder2 =
      new SearchRequest.Builder()
        .query(query2)
        .size(500)
    //.sort(sortOpt).pit(pointInTimeReference)

    val searchRequest2 = searchRequestBuilder2.build()

//    val origHakuQuery =
//      "{\"bool\":{\"must\":[{\"bool\":{\"must_not\":[{\"terms\":{\"tila.keyword\":[\"tallennettu\"]}}]}},{\"bool\":{\"must\":[{\"terms\":{\"hakulomakeAtaruId.keyword\":[\"66b7b709-1ed0-49cc-bbef-e5b0420a81c9\"]}},{\"terms\":{\"metadata.koulutuksenAlkamiskausi.koulutuksenAlkamisvuosi\":[2022]}},{\"terms\":{\"hakuvuosi\":[2022]}}]}}]}}"
    val origHakuQuery =
      "{\"bool\":{\"must\":[{\"bool\":{\"must_not\":[{\"terms\":{\"tila.keyword\":[\"tallennettu\"]}}]}},{\"bool\":{\"must\":[{\"bool\":{\"should\":[{\"bool\":{\"should\":[{\"terms\":{\"hakukohteet.jarjestyspaikka.oid\":[\"1.2.246.562.10.64170598482\"]}},{\"terms\":{\"hakukohteet.toteutus.tarjoajat.oid\":[\"1.2.246.562.10.64170598482\"]}}]}},{\"bool\":{\"should\":[{\"terms\":{\"hakukohteet.jarjestyspaikka.oid\":[\"1.2.246.562.10.45798950973\"]}},{\"terms\":{\"hakukohteet.toteutus.tarjoajat.oid\":[\"1.2.246.562.10.45798950973\"]}}]}}]}},{\"terms\":{\"metadata.koulutuksenAlkamiskausi.koulutuksenAlkamisvuosi\":[2023]}},{\"terms\":{\"hakuvuosi\":[2023]}}]}}]}},\"size\":500}"

    logger.info("origHakuQuery        = " + origHakuQuery)
    logger.info("newQuery2 = " + searchRequest2.query().toString)
//    logger.info("newQueryNotWorking  = " +     searchRequestNotWorking.query().toString)

  }

  def testAlkamiskausiTyyppi(): Unit = {
    val tyyppiString = Option("alkamiskausi ja -vuosi")

    def tyyppi = Alkamiskausityyppi.withName(tyyppiString.get)

    logger.info("tyyppi = " + tyyppi)

  }

  def testToKiellistettyMap(): Unit = {
    val k  = Kieli.all;
    val k0 = k.apply(0)
    logger.info("k = " + k)
    logger.info("k0 = " + k0)
    val map = Map("fi" -> "test", "sv" -> "ruotsi")
    logger.info("KielistettyMap = " + toKielistettyMap(map))

    val map2 = Map("fi" -> "testiä testiä")
    logger.info("KielistettyMap2 = " + toKielistettyMap(map2))

  }

  def toKielistettyMap(map: Map[String, String]): Kielistetty = {
    Map(
      Kieli.En -> map.get("en"),
      Kieli.Fi -> map.get("fi"),
      Kieli.Sv -> map.get("sv")
    ).collect { case (k, Some(v)) => (k, v) }
  }

}
