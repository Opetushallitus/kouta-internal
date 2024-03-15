package fi.oph.kouta.internal.elasticsearch

import co.elastic.clients.elasticsearch
import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.query_dsl.{ExistsQuery, QueryBuilders, TermQuery, TermsQuery, TermsQueryField, WildcardQuery}
import co.elastic.clients.elasticsearch.core.SearchRequest
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import com.sksamuel.elastic4s.requests.searches.queries.Query
import com.sksamuel.elastic4s.{ElasticClient, ElasticDateMath}
import fi.oph.kouta.internal.domain.Hakukohde
import fi.oph.kouta.internal.domain.enums.Julkaisutila
import fi.oph.kouta.internal.domain.indexed.{HakuJavaClient, HakukohdeIndexed, HakukohdeJavaClient, KoodiUri}
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid}
import fi.oph.kouta.internal.util.{ElasticCache, KoutaJsonFormats}
import fi.vm.sade.utils.slf4j.Logging

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util
import java.util.NoSuchElementException
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.collection.JavaConverters._

class HakukohdeClient(val index: String, val client: ElasticClient, val clientJava: elasticsearch.ElasticsearchClient)
    extends KoutaJsonFormats
    with Logging
    with ElasticsearchClient {

  private val hakukohdeCache = ElasticCache[HakukohdeOid, HakukohdeIndexed]()

  def getHakukohde(oid: HakukohdeOid): Future[Hakukohde] =
    findByOidsIndexed(Set(oid)).flatMap {
      case r if r.nonEmpty =>
        Future.successful(r.head.toHakukohde)
      case _ =>
        Future.failed(new NoSuchElementException(s"Hakukohde not found from Elastic! Didn't find id $oid"))
    }

  def search(
      hakuOid: Option[HakuOid],
      tarjoajaOids: Option[Set[OrganisaatioOid]],
      hakukohdeKoodi: Option[KoodiUri],
      q: Option[String],
      oikeusHakukohteeseenFn: OrganisaatioOid => Option[Boolean],
      hakukohderyhmanHakukohdeOids: Option[Set[HakukohdeOid]]
  ): Future[Seq[Hakukohde]] = {

    /*
    // Lokaalia testausta varten
    val hakukohdeKoodi = Option(KoodiUri("jannen testi"))
    val tarjoajaOids = Some(
      Set(
        OrganisaatioOid("1.2.246.562.10.00000000001"),
        OrganisaatioOid("1.2.246.562.10.45798950973")
      )
    )*/
    /*
    val hakukohderyhmanHakukohdeOids = Some(
      Set(
        HakukohdeOid("1.2.246.562.10.00000000001")
      ))
     */
    tarjoajaOids.foreach(oids =>
      if (oids.isEmpty)
        throw new IllegalArgumentException(s"Missing valid query parameters.")
    )

    // Uusi OK!
    val hakuQueryOld = hakuOid.map(oid => must(termsQuery("hakuOid", oid.toString)))
    val hakuQueryNew = hakuOid.map(oid =>
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

    // Uusi OK!
    val tarjoajaQueryOld = tarjoajaOids.flatMap(oids =>
      if (oids.isEmpty) None
      else
        Some(
          must(
            should(
              oids.map(oid =>
                should(
                  termsQuery("jarjestyspaikka.oid", oid.toString),
                  not(existsQuery("jarjestyspaikka")).must(termsQuery("toteutus.tarjoajat.oid", oid.toString))
                ).minimumShouldMatch(1)
              )
            )
          )
        )
    )

    val tarjoajaQueryNew =
      if (tarjoajaOids == None || tarjoajaOids.get.isEmpty) None
      else
        tarjoajaOids.map(oids =>
          QueryBuilders.bool
            .must(
              QueryBuilders.bool
                .should(
                  oids.map(oid =>
                      QueryBuilders.bool
                        .should(
                          TermsQuery
                            .of(m =>
                              m.field("jarjestyspaikka.oid")
                                .terms(
                                  new TermsQueryField.Builder()
                                    .value(List(FieldValue.of(oid.toString)).asJava)
                                    .build()
                                ))._toQuery(),
                          QueryBuilders.bool
                            .must(
                              TermsQuery
                                .of(m =>
                                  m.field("toteutus.tarjoajat.oid")
                                    .terms(
                                      new TermsQueryField.Builder()
                                        .value(List(FieldValue.of(oid.toString())).asJava)
                                        .build()
                                    ))._toQuery())
                            .mustNot(ExistsQuery.of(m => m.field("jarjestyspaikka"))._toQuery())
                            .build()._toQuery()
                        )
                        .minimumShouldMatch("1")
                        .build()._toQuery()
                    ).toList.asJava
                ).build()._toQuery()
            ).build()._toQuery())

    val hakukohdeKoodiQueryOld = hakukohdeKoodi.map(k =>
      should(
        wildcardQuery("hakukohde.koodiUri.keyword", k.koodiUri + "#*"),
        termsQuery("hakukohde.koodiUri.keyword", k.koodiUri)
      ).minimumShouldMatch(1)
    )
    val hakukohdeKoodiQueryNew =
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

    val qQueryOld = q.map(q => {
      val wildcardQ = "*" + q + "*"
      should(
        wildcardQuery("nimi.fi.keyword", wildcardQ),
        wildcardQuery("nimi.sv.keyword", wildcardQ),
        wildcardQuery("nimi.en.keyword", wildcardQ),
        wildcardQuery("jarjestyspaikka.nimi.fi.keyword", wildcardQ),
        wildcardQuery("jarjestyspaikka.nimi.sv.keyword", wildcardQ),
        wildcardQuery("jarjestyspaikka.nimi.en.keyword", wildcardQ),
        wildcardQuery("toteutus.tarjoajat.nimi.fi.keyword", wildcardQ),
        wildcardQuery("toteutus.tarjoajat.nimi.sv.keyword", wildcardQ),
        wildcardQuery("toteutus.tarjoajat.nimi.en.keyword", wildcardQ)
      ).minimumShouldMatch(1)
    })

    val qQueryNew =
      q.map(q => {
        val wildcardQ = "*" + q + "*"
        QueryBuilders.bool
          .minimumShouldMatch("1")
          .should(
            WildcardQuery.of(m => m.field("nimi.fi.keyword").value(wildcardQ))._toQuery(),
            WildcardQuery.of(m => m.field("nimi.sv.keyword").value(wildcardQ))._toQuery(),
            WildcardQuery.of(m => m.field("nimi.en.keyword").value(wildcardQ))._toQuery(),
            WildcardQuery.of(m => m.field("jarjestyspaikka.nimi.fi.keyword").value(wildcardQ))._toQuery(),
            WildcardQuery.of(m => m.field("jarjestyspaikka.nimi.sv.keyword").value(wildcardQ))._toQuery(),
            WildcardQuery.of(m => m.field("jarjestyspaikka.nimi.en.keyword").value(wildcardQ))._toQuery(),
            WildcardQuery.of(m => m.field("toteutus.tarjoajat.nimi.fi.keyword").value(wildcardQ))._toQuery(),
            WildcardQuery.of(m => m.field("toteutus.tarjoajat.nimi.sv.keyword").value(wildcardQ))._toQuery(),
            WildcardQuery.of(m => m.field("toteutus.tarjoajat.nimi.en.keyword").value(wildcardQ))._toQuery()
          ).build._toQuery()
      })

    val hakukohderyhmanHakukohteetQueryOld =
      hakukohderyhmanHakukohdeOids.map(oids => must(termsQuery("oid", oids.map(oid => oid.toString))))

    val hakukohderyhmanHakukohteetQueryNew =
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
                    ))._toQuery()
            ).build()._toQuery())

    val queriesOld = (tarjoajaQueryOld, hakukohderyhmanHakukohteetQueryOld)
    val queryOld = queriesOld match {
      case (None, None) => Some(must(hakuQueryOld))
      case (_, _) =>
        Some(
          must(
            hakuQueryOld ++ Some(
              should(queriesOld._1 ++ queriesOld._2)
                .minimumShouldMatch(1)
            )
          )
        )
    }
    val queriesNew = (tarjoajaQueryNew, hakukohderyhmanHakukohteetQueryNew)
    val queryNew = queriesNew match {
      case (None, None) =>
        if (hakuQueryNew == None) None else Some(QueryBuilders.bool.must(hakuQueryNew.get).build()._toQuery())
      //   case (None, None) => Some(QueryBuilders.bool.must(hakuQueryNew.map(a => a.get)).build()._toQuery())
      case (_, _) =>
        Some(
          QueryBuilders.bool
            .must(
              List(
                hakuQueryNew,
                Option(
                  QueryBuilders.bool
                    .minimumShouldMatch("1")
                    .should(List(queriesNew._1, queriesNew._2).flatten.asJava)
                    .build()
                    ._toQuery()
                )
              ).flatten.asJava
            )
            .build()
            ._toQuery()
        )
    }

    val queryFinal =
      QueryBuilders.bool
        .must(
          List(queryNew, hakukohdeKoodiQueryNew, qQueryNew).flatten.asJava
        )
        .build()
        ._toQuery()

    val tilaQuery =
      //Option.apply(
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
              )._toQuery()).build()._toQuery()
    //)

    val queryList = List(tilaQuery, queryFinal).asJava

    val newresult = Future(searchItemsNew[HakukohdeJavaClient](queryList).map(_.toResult()))
      .map(_.map(_.toHakukohde(oikeusHakukohteeseenFn)))

    val oldResult = searchItems[HakukohdeIndexed](Some(must(queryOld ++ hakukohdeKoodiQueryOld ++ qQueryOld)))
      .map(_.map(_.toHakukohde(oikeusHakukohteeseenFn)))
    oldResult
    newresult
  }

  def hakukohdeOidsByJulkaisutila(
      julkaisuTilat: Option[Seq[Julkaisutila]],
      modifiedDateStartFrom: Option[LocalDate],
      offset: Int,
      limit: Option[Int]
  ): Future[Seq[HakukohdeOid]] = {
    var allQueries: List[Query] = List()
    if (julkaisuTilat.isDefined) {
      allQueries ++= julkaisuTilat.map(tilat =>
        should(
          tilat.map(tila =>
            should(
              termsQuery("tila.keyword", tila.name)
            )
          )
        )
      )
    }
    if (modifiedDateStartFrom.isDefined) {
      allQueries ++= Some(
        rangeQuery("modified").gt(ElasticDateMath(modifiedDateStartFrom.get.format(DateTimeFormatter.ISO_LOCAL_DATE)))
      )
    }

    searchItemBulks[HakukohdeIndexed](
      if (allQueries.isEmpty) None
      else
        Some(must(allQueries)),
      offset,
      limit
    ).map(_.map(_.oid))
  }

  def findByOids(
      hakukohteetOids: Set[HakukohdeOid],
      oikeusHakukohteeseenFn: OrganisaatioOid => Option[Boolean]
  ): Future[Seq[Hakukohde]] =
    findByOidsIndexed(hakukohteetOids).map(_.map(_.toHakukohde(oikeusHakukohteeseenFn)))

  private def findByOidsForReal(hakukohteetOids: Set[HakukohdeOid]): Future[Map[HakukohdeOid, HakukohdeIndexed]] = {
    val hakukohteetQuery = should(termsQuery("oid", hakukohteetOids.map(_.toString)))
    searchItemBulks[HakukohdeIndexed](Some(must(hakukohteetQuery)), 0, None)
      .map(h => h.map(hh => hh.oid -> hh).toMap)
  }
  private def findByOidsIndexed(hakukohteetOids: Set[HakukohdeOid]): Future[Seq[HakukohdeIndexed]] =
    hakukohdeCache.getMany(hakukohteetOids, missing => findByOidsForReal(missing.toSet))

  def findByOids(hakukohteetOids: Set[HakukohdeOid]): Future[Seq[Hakukohde]] =
    findByOidsIndexed(hakukohteetOids).map(h => h.map(_.toHakukohde))
}

object HakukohdeClient
    extends HakukohdeClient("hakukohde-kouta", ElasticsearchClient.client, ElasticsearchClient.clientJava)
