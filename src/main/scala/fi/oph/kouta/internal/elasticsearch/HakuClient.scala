package fi.oph.kouta.internal.elasticsearch

import co.elastic.clients.elasticsearch
import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.query_dsl.{ExistsQuery, QueryBuilders, TermsQuery, TermsQueryField}

import com.sksamuel.elastic4s.{ElasticClient, ElasticDateMath}
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.json4s.ElasticJson4s.Implicits._
import com.sksamuel.elastic4s.requests.searches.queries.Query
import fi.oph.kouta.internal.domain.enums.Julkaisutila
import fi.oph.kouta.internal.domain.enums.Julkaisutila.Tallennettu
import fi.oph.kouta.internal.domain.indexed.{HakuIndexed, HakuJavaClient}
import fi.oph.kouta.internal.domain.oid.{HakuOid, OrganisaatioOid}
import fi.oph.kouta.internal.domain.{Haku, OdwHaku}
import fi.oph.kouta.internal.util.{ElasticCache, KoutaJsonFormats}
import fi.vm.sade.utils.slf4j.Logging

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.NoSuchElementException
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.collection.JavaConverters._

class HakuClient(val index: String, val client: ElasticClient, val clientJava: elasticsearch.ElasticsearchClient)
    extends KoutaJsonFormats
    with Logging
    with ElasticsearchClient {

  private val hakuCache = ElasticCache[HakuOid, HakuIndexed]()

  private def findHakuIndexedByOidsForReal(hakuOids: Set[HakuOid]): Future[Map[HakuOid, HakuIndexed]] = {
    val hakuQuery = should(termsQuery("oid", hakuOids.map(_.toString)))
    searchItemBulks[HakuIndexed](Some(must(hakuQuery)), 0, None)
      .map(h => h.map(hh => hh.oid -> hh).toMap)
  }

  def findHakuIndexedByOids(hakuOids: Set[HakuOid]): Future[Seq[HakuIndexed]] =
    hakuCache.getMany(hakuOids, missing => findHakuIndexedByOidsForReal(missing.toSet))

  def getHaku(oid: HakuOid): Future[Haku] =
    findHakuIndexedByOids(Set(oid)).flatMap {
      case r if r.nonEmpty =>
        Future.successful(r.head.toHaku())
      case _ =>
        Future.failed(new NoSuchElementException(s"Haku not found from Elastic! Didn't find id $oid"))
    }

  private def byTarjoajaAndTila(tarjoajaOids: Option[Set[OrganisaatioOid]], haku: HakuIndexed): Boolean =
    tarjoajaOids.fold(true)(oids =>
      haku.hakukohteet.exists(hakukohde => {
        hakukohde.tila match {
          case Tallennettu => false
          case _ =>
            hakukohde.jarjestyspaikka.fold(hakukohde.toteutus.tarjoajat.exists(t => oids.contains(t.oid)))(j =>
              oids.contains(j.oid)
            )
        }
      })
    )

  def search(
      ataruId: Option[String],
      tarjoajaOids: Option[Set[OrganisaatioOid]],
      vuosi: Option[Int],
      includeHakukohdeOids: Boolean
  ): Future[Seq[Haku]] = {
    val ataruIdQueryOld      = ataruId.map(termsQuery("hakulomakeAtaruId.keyword", _))
   val ataruIdQuery = ataruId.map(t =>
     (TermsQuery.of(m => m.field("hakulomakeAtaruId.keyword").terms(
       new TermsQueryField.Builder()
         .value(ataruId.toList.map(m => FieldValue.of(m)).asJava)
         .build()
     ))._toQuery()))

    val alkamisvuosiQueryOld = vuosi.map(termsQuery("metadata.koulutuksenAlkamiskausi.koulutuksenAlkamisvuosi", _))
    val alkamisvuosiQuery = vuosi.map(t =>
      TermsQuery.of(m => m.field("metadata.koulutuksenAlkamiskausi.koulutuksenAlkamisvuosi").terms(
        new TermsQueryField.Builder()
          .value(vuosi.toList.map(m => FieldValue.of(m)).asJava)
          .build()
      ))._toQuery())

    val hakuvuosiQueryOld    = vuosi.map(termsQuery("hakuvuosi", _))
    val hakuvuosiQuery = vuosi.map(t =>
      TermsQuery.of(m => m.field("hakuvuosi").terms(
        new TermsQueryField.Builder()
          .value(vuosi.toList.map(m => FieldValue.of(m)).asJava)
          .build()
      ))._toQuery())
    val tarjoajaQueryOld = tarjoajaOids.map(oids =>
      should(
        oids.map(oid =>
          should(
            termsQuery("hakukohteet.jarjestyspaikka.oid", oid.toString),
            termsQuery("hakukohteet.toteutus.tarjoajat.oid", oid.toString)
          )
        )
      )
    )
    val tarjoajaQuery =
      tarjoajaOids.map(oids =>
        QueryBuilders.bool.should(
          oids.map(oid =>
            QueryBuilders.bool.should(
              TermsQuery.of(m => m.field("hakukohteet.jarjestyspaikka.oid").terms(
                new TermsQueryField.Builder()
                  .value(List(FieldValue.of(oid.toString())).asJava)
                  .build()
              ))._toQuery(),
              TermsQuery.of(m => m.field("hakukohteet.toteutus.tarjoajat.oid").terms(
                new TermsQueryField.Builder()
                  .value(List(FieldValue.of(oid.toString())).asJava)
                  .build()
              ))._toQuery()
            ).build()._toQuery()).toList.asJava).build()._toQuery())

    val tilaQuery =
      Option.apply(
        QueryBuilders.bool.mustNot(
          TermsQuery.of(m => m.field("tila.keyword").terms(
            new TermsQueryField.Builder()
              .value(List(FieldValue.of("tallennettu")).asJava)
              .build()
          ))._toQuery()
        ).build()._toQuery())



    val queryList = List(
      ataruIdQuery,
      tarjoajaQuery,
      alkamisvuosiQuery,
      hakuvuosiQuery).flatten.asJava

    val queryListFinal = List(
      tilaQuery,
      Some(QueryBuilders.bool.must(queryList).build._toQuery())).flatten.asJava

    // Tämä toimii teknisesti, muttei ehkä oikealla tavalla
    //Future(searchItemsNew[HakuJavaClient](queryListFinal).map(_.toResult()).toSeq.map(_.toHaku()))



//    val query = ataruIdQueryOld ++ tarjoajaQueryOld ++ alkamisvuosiQueryOld ++ hakuvuosiQueryOld

    // Alkup haku
//  val origSearch =  searchItems[HakuIndexed](if (query.isEmpty) None else Some(must(query)))
//      .map(_.filter(byTarjoajaAndTila(tarjoajaOids, _)).map(_.toHaku(includeHakukohdeOids)))

    Future(searchItemsNew[HakuJavaClient](queryListFinal).map(_.toResult()))
      .map(_.filter(byTarjoajaAndTila(tarjoajaOids, _)).map(_.toHaku(includeHakukohdeOids)))
    //Future(
      //searchItemsNew[HakuJavaClient](queryListFinal).map(_.toResult()).toSeq.map(_.toHaku()))
  }

  def hakuOidsByJulkaisutila(
      julkaisuTilat: Option[Seq[Julkaisutila]],
      modifiedDateStartFrom: Option[LocalDate],
      offset: Int,
      limit: Option[Int]
  ): Future[Seq[HakuOid]] = {
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
    searchItemBulks[HakuIndexed](
      if (allQueries.isEmpty) None
      else
        Some(must(allQueries)),
      offset,
      limit
    ).map(_.map(_.oid))
  }

  def findByOids(hakuOids: Set[HakuOid]): Future[Seq[Haku]] =
    findHakuIndexedByOids(hakuOids).map(_.map(_.toHaku()))

  def findOdwHautByOids(hakuOids: Set[HakuOid]): Future[Seq[OdwHaku]] = {
    findHakuIndexedByOids(hakuOids).map(_.map(_.toOdwHaku))
  }

}

object HakuClient extends HakuClient("haku-kouta", ElasticsearchClient.client, ElasticsearchClient.clientJava)
