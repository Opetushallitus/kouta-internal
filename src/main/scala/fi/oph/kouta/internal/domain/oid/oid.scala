package fi.oph.kouta.internal.domain.oid

import fi.oph.kouta.internal.security.OidTooShortException

import java.util.regex.Pattern

sealed trait Oid {
  val OidPattern = Pattern.compile("""^[\d][\d\.]+[\d]$""")
  val s: String

  override def toString: String = s

  def isValid(): Boolean = OidPattern.matcher(s).matches()
  def validateLength():Any = {
   if (s.length < 35) throw OidTooShortException(s"Oid $s is too short!")
  }
}

case class GenericOid(s: String) extends Oid

case class KoulutusOid(s: String) extends Oid {
  override val OidPattern: Pattern = Pattern.compile("""^1\.2\.246\.562\.13.+[\d]$""")
}

case class ToteutusOid(s: String) extends Oid {
  override val OidPattern: Pattern = Pattern.compile("""^1\.2\.246\.562\.17.+[\d]$""")
}

case class HakukohdeOid(s: String) extends Oid {
  override val OidPattern: Pattern = Pattern.compile("""^1\.2\.246\.562\.20.+[\d]$""")
}

case class HakuOid(s: String) extends Oid {
  override val OidPattern: Pattern = Pattern.compile("""^1\.2\.246\.562\.29.+[\d]$""")
}

case class OrganisaatioOid(s: String) extends Oid
case class UserOid(s: String)         extends Oid
