package fi.oph.kouta.internal.elasticsearch

case class TeapotException(msg: String, error: Throwable) extends RuntimeException
