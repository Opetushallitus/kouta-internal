package fi.oph.kouta.internal.elasticsearch

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

class HakukohdeClient(val index: String, val client: ElasticClient)
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

  def search(hakuOid: Option[HakuOid],
             tarjoajaOids: Option[Set[OrganisaatioOid]],
             hakukohdeKoodi: Option[KoodiUri],
             q: Option[String],
             oikeusHakukohteeseenFn: OrganisaatioOid => Option[Boolean],
             hakukohderyhmanHakukohdeOids: Option[Set[HakukohdeOid]]
            ): Future[Seq[Hakukohde]] = {
    tarjoajaOids.foreach(oids =>
      if (oids.isEmpty)
        throw new IllegalArgumentException(s"Missing valid query parameters.")
    )

    val hakuQuery = hakuOid.map(oid => must(termsQuery("hakuOid", oid.toString)))
    val hakukohdeKoodiQuery = hakukohdeKoodi.map(k =>
      should(
        wildcardQuery("hakukohde.koodiUri.keyword", k.koodiUri + "#*"),
        termsQuery("hakukohde.koodiUri.keyword", k.koodiUri)
      ).minimumShouldMatch(1)
    )
    val tarjoajaQuery = tarjoajaOids.flatMap(oids =>
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

    val hakukohderyhmanHakukohteetQuery =
      hakukohderyhmanHakukohdeOids.map(oids => must(termsQuery("oid", oids.map(oid => oid.toString))))

    val queries = (tarjoajaQuery, hakukohderyhmanHakukohteetQuery)

    val query = queries match {
      case (None, None) => Some(must(hakuQuery))
      case (_, _) =>
        Some(
          must(
            hakuQuery ++ Some(should(queries._1 ++ queries._2).minimumShouldMatch(1))
          )
        )
    }

    val qQuery = q.map(q => {
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

    searchItems[HakukohdeIndexed](Some(must(query ++ hakukohdeKoodiQuery ++ qQuery)))
      .map(_.map(_.toHakukohde(oikeusHakukohteeseenFn)))
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

object HakukohdeClient extends HakukohdeClient("hakukohde-kouta", ElasticsearchClient.client)
