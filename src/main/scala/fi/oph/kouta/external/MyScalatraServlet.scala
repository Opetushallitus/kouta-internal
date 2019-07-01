package fi.oph.kouta.external

import org.scalatra._

class MyScalatraServlet extends ScalatraServlet {

  get("/") {
    "Hello"
  }

}
