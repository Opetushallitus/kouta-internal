package fi.oph.kouta.internal.util

import java.util.Optional

object MiscUtils {
  def toScalaOption[A](maybeA: Optional[A]): Option[A] =
  if(maybeA.isEmpty) None else Some(maybeA.get)
}
