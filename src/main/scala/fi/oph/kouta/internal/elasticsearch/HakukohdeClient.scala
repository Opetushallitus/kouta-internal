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
import fi.oph.kouta.internal.domain.indexed.{HakukohdeIndexed, KoodiUri}
import fi.oph.kouta.internal.domain.oid.{HakuOid, HakukohdeOid, OrganisaatioOid}
import fi.oph.kouta.internal.util.{ElasticCache, KoutaJsonFormats}
import fi.vm.sade.utils.slf4j.Logging

import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
      hakukohdeKoodiOrig: Option[KoodiUri],
      q: Option[String],
      oikeusHakukohteeseenFn: OrganisaatioOid => Option[Boolean],
      hakukohderyhmanHakukohdeOidsOrig: Option[Set[HakukohdeOid]]
  ): Future[Seq[Hakukohde]] = {


    val hakukohdeKoodi = Option(KoodiUri("jannen testi"))

    /*val tarjoajaOids = Some(
      Set(
        OrganisaatioOid("1.2.246.562.10.00000000001"),
        OrganisaatioOid("1.2.246.562.10.45798950973")
      )
    )*/
    val hakukohderyhmanHakukohdeOids = Some(
      Set(
        HakukohdeOid("1.2.246.562.10.00000000001")
      ))

    tarjoajaOids.foreach(oids =>
      if (oids.isEmpty)
        throw new IllegalArgumentException(s"Missing valid query parameters.")
    )

    val hakuQueryOld = hakuOid.map(oid => must(termsQuery("hakuOid", oid.toString)))

    val hakuOidQuery = hakuOid.map(oid =>
      QueryBuilders.bool.must(
        TermsQuery.of(m => m.field("hakuOid.keyword").terms(
          new TermsQueryField.Builder()
            .value(List(FieldValue.of(oid.toString())).asJava)
            .build()))._toQuery()
      ).build()._toQuery())

    val hakukohdeKoodiQueryOld = hakukohdeKoodi.map(k =>
      should(
        wildcardQuery("hakukohde.koodiUri.keyword", k.koodiUri + "#*"),
        termsQuery("hakukohde.koodiUri.keyword", k.koodiUri)
      ).minimumShouldMatch(1)
    )

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
              )))))
    val hakukohderyhmanHakukohteetQueryOld =
      hakukohderyhmanHakukohdeOids.map(oids => must(termsQuery("oid", oids.map(oid => oid.toString))))

    val hakukohderyhmanHakukohteetQuery =
      hakukohderyhmanHakukohdeOids
        .map(oid =>
          QueryBuilders.bool.must(
            TermsQuery.of(m => m.field("oid").terms(
              new TermsQueryField.Builder()
                .value(oid.toList.map(m => FieldValue.of(m.toString)).asJava)
                .build()))._toQuery()
          ).build()._toQuery())

    val queriesOld = (tarjoajaQueryOld, hakukohderyhmanHakukohteetQueryOld)

    val queryOld = queriesOld match {
      case (None, None) => Some(must(hakuQueryOld))
      case (_, _) =>
        Some(
          must(
            hakuQueryOld ++ Some(should(queriesOld._1 ++ queriesOld._2).minimumShouldMatch(1))
          )
        )
    }
    val tarjoajaQueryNew =
      if (tarjoajaOids.get.isEmpty) None else
        tarjoajaOids.map(oids =>

          QueryBuilders.bool.must(
            QueryBuilders.bool.should(
              oids.map(oid =>
                QueryBuilders.bool
                  .should(
                    TermsQuery
                      .of(m => m.field("jarjestyspaikka.oid")
                        .terms(new TermsQueryField.Builder()
                          .value(List(FieldValue.of(oid.toString)).asJava)
                          .build()))._toQuery(),
                    QueryBuilders.bool

                      .must(
                        TermsQuery
                          .of(m => m.field("toteutus.tarjoajat.oid")
                            .terms(new TermsQueryField.Builder()
                              .value(List(FieldValue.of(oid.toString())).asJava)
                              .build()))._toQuery())
                      .mustNot(ExistsQuery.of(m => m.field("jarjestyspaikka"))._toQuery())

                      .build()._toQuery()
                  ).minimumShouldMatch("1")
                  .build()._toQuery()
              ).toList.asJava
            ).build()._toQuery()
          ).build()._toQuery()
        )
    val queries = (tarjoajaQueryNew, hakukohderyhmanHakukohteetQuery)

    val query = queries match {
      case (None, None) => Some(QueryBuilders.bool.must(hakuOidQuery.get))
      //case (None, None) => Some(QueryBuilders.bool.must(List(hakuOidQuery).flatten.asJava)))
      case (_, _) =>
        Some(
          QueryBuilders.bool.must(
            QueryBuilders.bool.must(
              List(
                hakuOidQuery,
                Option(QueryBuilders.bool
                  .minimumShouldMatch("1")
                  .should(
                    List(queries._1, queries._2).flatten.asJava).build()._toQuery())
              ).flatten.asJava
            ).build()._toQuery()))
    }

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
    //

    searchItems[HakukohdeIndexed](Some(must(queryOld ++ hakukohdeKoodiQueryOld++ qQueryOld)))
      .map(_.map(_.toHakukohde(oikeusHakukohteeseenFn)))
  }


  /*


  val qQueryNew =
    q.map(q => {
      val wildcardQ = "*" + q + "*"
      QueryBuilders.bool
        .should(
          TermQuery.of(m => m.field("nimi.fi").value(wildcardQ))._toQuery(),
          TermQuery.of(m => m.field("nimi.sv").value(wildcardQ))._toQuery(),
          TermQuery.of(m => m.field("nimi.en").value(wildcardQ))._toQuery(),
          TermQuery.of(m => m.field("jarjestyspaikka.nimi.fi").value(wildcardQ))._toQuery(),
          TermQuery.of(m => m.field("jarjestyspaikka.nimi.sv").value(wildcardQ))._toQuery(),
          TermQuery.of(m => m.field("jarjestyspaikka.nimi.en").value(wildcardQ))._toQuery(),
          TermQuery.of(m => m.field("toteutus.tarjoajat.nimi.fi").value(wildcardQ))._toQuery(),
          TermQuery.of(m => m.field("toteutus.tarjoajat.nimi.sv").value(wildcardQ))._toQuery(),
          TermQuery.of(m => m.field("toteutus.tarjoajat.nimi.en").value(wildcardQ))._toQuery()
        ).build._toQuery()
    })
  val hakukohderyhmanHakukohteetQueryNew = hakukohderyhmanHakukohdeOids.map(oid => TermQuery.of(m => m.field("oid.keyword").value(oid.toString))._toQuery())
*/

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

object HakukohdeClient extends HakukohdeClient("hakukohde-kouta", ElasticsearchClient.client, ElasticsearchClient.clientJava)
