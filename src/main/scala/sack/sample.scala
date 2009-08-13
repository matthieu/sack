package sack

import sack._

// Simplest app
//

object ChickenApp extends SackApp {
  def build = (env: Req) => (200, Map("Content-Type"->"text/plain"), List("In Every Pot"))    
}

// Slightly more complex example, an bare app and a handler
//

object TurtleApp extends SackApp {
  def build = Turtle
}

object Turtle extends SackHandler {
  def apply(env: Req): Resp =
    (200, Map("Content-Type"->"text/plain"), List("All The Way Down"))
}

// An app with two middlewares implementations and a handler
//

object PigApp extends SackApp {
    def build(): SackHandler = Paragraphize(Bolderize(Pig))
}

object Pig extends SackHandler {
  def apply(env: Req): Resp =
    (200, Map("Content-Type"->"text/html"), List("Happy As A Pig In Mud"))
}

// Companion object not necessary but it makes middlewares stacking nicer
object Bolderize {
  def apply(h: SackHandler) = new Bolderize(h)
}
class Bolderize(handler: SackHandler) extends SackHandler {
  def apply(env: Req): Resp = {
    val res = handler.apply(env)
    val bld = (res._1, res._2, List("<b>" + res._3(0) + "</b>"))
    bld
  }    
}

object Paragraphize {
  def apply(h: SackHandler) = new Paragraphize(h)
}
class Paragraphize(handler: SackHandler) extends SackHandler {
  def apply(env: Req): Resp = {
    val res = handler.apply(env)
    (res._1, res._2, List("<p>" + res._3(0) + "</p>"))
  }    
}
