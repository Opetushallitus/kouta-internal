package fi.oph.kouta.internal.elasticsearch

import com.sksamuel.elastic4s.ElasticError

case class ElasticSearchException(error: ElasticError) extends RuntimeException
