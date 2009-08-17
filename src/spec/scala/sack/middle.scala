package sack

import org.specs._

import sack._

object MiddleWareSpecs extends Specification with HttpClientHelper {
  val url = "http://localhost:5414/"
  
  object CascadeApp extends SackApp {
    def build = Cascade(new SinglePath("Foo"), new SinglePath("Bar"), new SinglePath("Baz"))
  }
  class SinglePath(name: String) extends SackHandler {
    def apply(env: Req) =
      if (env("PATH_INFO") == "/"+name.toLowerCase) (200, Map("Content-Type"->"text/plain"), List(name+" forever"))
      else (404, null, null)
  }

  "Cascade Handler" should {
    doFirst { CascadeApp.main(null) }

    "should stop at first response" in {
      GET(url+"foo")._3(0) must_== "Foo forever"
    }
    "should fallthrough a failing handler" in {
      GET(url+"bar")._3(0) must_== "Bar forever"
    }
    "should fallthrough failing handlers" in {
      GET(url+"baz")._3(0) must_== "Baz forever"
    }
    "should return a 404 when all fail" in {
      GET(url+"quux")._1 must_== 404
    }

    doLast { CascadeApp.stop }
  }
}
