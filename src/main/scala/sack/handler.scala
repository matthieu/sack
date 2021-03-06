package sack

import org.mortbay.jetty.Server
import org.mortbay.jetty.Request
import org.mortbay.jetty.handler.AbstractHandler

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

trait SackTypes {
  type Req  = Map[String,Any]
  type Resp = Tuple3[Int,Map[String,String],List[String]]
}

trait SackApp extends SackTypes {
  val port = 5414
  private var server: Server = null

  def main(args: Array[String]) {
    System.setProperty("org.mortbay.log.class", "sack.QuietJettyLogger");
    server = new Server(port)
    server.addHandler(new JettyHandler(build()));
    server.start();
  }

  def build(): Req => Resp

  def stop() {
    server.stop
    server = null
  }
}

trait SackHandler extends (SackTypes#Req => SackTypes#Resp) with SackTypes {
  def apply(env: Req): Resp
}

class JettyHandler(h: (SackTypes#Req => SackTypes#Resp)) extends AbstractHandler with SackTypes {

  def handle(target: String, request: HttpServletRequest, response: HttpServletResponse, dispatch: Int) {
    val base_request = request match {
      case r: Request => r
      case _ => org.mortbay.jetty.HttpConnection.getCurrentConnection().getRequest()
    }
    base_request.setHandled(true);

    val res = h.apply(buildEnv(base_request))

    response.setStatus(res._1);
    if (res._2 != null) for ((k,v)<-res._2) response.setHeader(k, v)
    if (res._3 != null) response.getWriter().print(res._3(0));
  }

  def buildEnv(r: Request) = Map("REQUEST_METHOD"->r.getMethod, "SCRIPT_NAME"->r.getServletPath,
    "PATH_INFO"->r.getPathInfo, "QUERY_STRING"->r.getQueryString, "SERVER_NAME"->r.getServerName,
    "SERVER_PORT"->r.getServerPort) ++ buildHttpHeaders(r) ++ buildSackEnv(r)

  def buildHttpHeaders(r: Request): Iterator[(String,String)] =
    for {
      h <- new RichEnumeration(r.getHeaderNames)
      v = r.getHeader(h.asInstanceOf[String])
      hs = h.asInstanceOf[String]
      vs = v.asInstanceOf[String]
    } yield ("HTTP_"+hs -> vs)

  def buildSackEnv(r:Request) = Map("sack.version"->List(1,0), "sack.url_scheme"->r.getScheme,
    "sack.input"->r.getReader, "sack.errors"->new java.io.OutputStreamWriter(System.err),
    "sack.multithread"->true, "sack.multiprocess"->false, "sack.run_once"->false)

  class RichEnumeration[T](enumeration:java.util.Enumeration[T]) extends Iterator[T] {
    def hasNext:Boolean =  enumeration.hasMoreElements()
    def next:T = enumeration.nextElement()
  }

}

class QuietJettyLogger extends org.mortbay.log.Logger {
  def setDebugEnabled(e: Boolean) = null
  def isDebugEnabled() = false
  def debug(msg: String, arg0: Object, arg1: Object) = null
  def debug(msg: String, t: Throwable) = null
  def getLogger(name: String) = this
  def info(msg: String, arg0: Object, arg1: Object) = null
  def warn(msg: String, t: Throwable) {
    println("WARN: " + msg)
    t.printStackTrace();
  }
  def warn(msg: String, arg0: Object, arg1: Object) {
    println("WARN: " + msg)
  }

}
