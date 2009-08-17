package sack

import org.specs._

import sack._

object HandlerSpecs extends Specification with HttpClientHelper {
  val url = "http://localhost:5414/"

  object OtherChickenApp extends SackApp {
    def build = (env: Req) => (200, Map("Content-Type"->"text/plain"), List("In Every Pot"))    
  }
  object EnvValidationApp extends SackApp {
    def build = (env: Req) => (200, Map("Content-Type"->"text/plain"), List(env.map(p=>p._1+"~"+p._2).mkString("|")))
  }
    
  "Static lambda handler" should {
    doFirst { OtherChickenApp.main(null) }

    "have a 200 response status" in {
      GET(url)._1 must_== 200
    }
    "have a text/plain content type" in {
      GET(url)._2("Content-Type") must_== "text/plain"
    }
    "return the expected text" in {
      GET(url)._3(0) must_== "In Every Pot"
    }

    doLast { OtherChickenApp.stop }
  }

  "Handler environment" should {
    doFirst { EnvValidationApp.main(null); }

    "provide the request method" in {
      env must havePair("REQUEST_METHOD"->"GET")
    }
    "provide a content type header" in {
      env must haveKey("HTTP_User-Agent")
    }
    "provide the URL scheme" in {
      env must havePair("sack.url_scheme"->"http")
    }

    def env = restoreEnv(GET(url)._3(0))
    def restoreEnv(b: String) = Map(b.split("\\|").map(splitAt("\\~")): _*)
    def splitAt(sep: String)(s: String): Tuple2[String,String] = {
      val a = s.split(sep)
      if (a.length == 2) (a(0),a(1)) else (a(0),"") 
    }

    doLast { EnvValidationApp.stop }
  }
}

