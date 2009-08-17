package sack

object Cascade {
  def apply(hs: SackHandler*) = new Cascade(hs.toList)
  def apply(fall: List[Int], hs: SackHandler*) = new Cascade(hs.toList, fall)
}
class Cascade(handlers: List[SackHandler], fall: List[Int]) extends SackHandler {
  require(handlers.length > 0)

  def this(handlers: List[SackHandler]) = this(handlers, List(404))

  def apply(env: Req): Resp = cascade(env, handlers)
    
  def cascade(env:Req, hs: List[SackHandler]): Resp = 
    success(hs.head.apply(env)) match {
      case Some(resp) => resp
      case None       => if (hs.length == 1) (404, null, null) else cascade(env, hs.tail)
    }

  def success(resp: Resp) = if (fall.contains(resp._1)) None else Some(resp)
}
