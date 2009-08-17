package sack

object Cascade extends SackTypes {
  def apply(hs: (Req=>Resp)*) = new Cascade(hs.toList)
  def apply(fall: List[Int], hs: (Req=>Resp)*) = new Cascade(hs.toList, fall)
}
class Cascade(handlers: List[(SackTypes#Req=>SackTypes#Resp)], fall: List[Int]) extends SackHandler {
  require(handlers.length > 0)

  def this(handlers: List[(SackTypes#Req=>SackTypes#Resp)]) = this(handlers, List(404))

  def apply(env: Req) = cascade(env, handlers)
    
  def cascade(env:Req, hs: List[(Req=>Resp)]): Resp = 
    success(hs.head.apply(env)) match {
      case Some(resp) => resp
      case None       => if (hs.length == 1) (404, null, null) else cascade(env, hs.tail)
    }

  def success(resp: Resp) = if (fall.contains(resp._1)) None else Some(resp)
}

object Multi extends SackTypes {
  def apply(hs: (String,Req=>Resp)*) = new Multi(hs.toList)
}
class Multi(handlers: List[(String,SackTypes#Req=>SackTypes#Resp)]) extends SackHandler {
  require(handlers.length > 0)
  require(handlers.forall(p => p._1.startsWith("/")))

  val hs: List[(String,Req=>Resp)] = 
    handlers map(p => (if (p._1.endsWith("/")) p._1.take(p._1.length - 1) else p._1, p._2))

  def apply(env: Req) = findMatch(env, hs) match {
    case Some(h) => {
      env("PATH_INFO") = env("PATH_INFO").asInstanceOf[String].drop(h._1.length)
      env("SCRIPT_NAME") = env("SCRIPT_NAME").asInstanceOf[String] + h._1
      h._2.apply(env)
    }
    case None    => (404, null, null)
  }

  def findMatch(env: Req, hs: List[(String,Req=>Resp)]): Option[(String,Req=>Resp)] = {
    val pi = env("PATH_INFO").asInstanceOf[String]
    hs find(p => pi.startsWith(p._1) && (pi.length == p._1.length || pi(p._1.length) == '/'))
  }
}
