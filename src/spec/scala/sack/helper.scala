package sack

import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.HttpClient

import sack._

trait HttpClientHelper extends SackTypes {
  def GET(url: String): Resp = {
    val client = new HttpClient();
    val method = new GetMethod(url);

    try {
      val statusCode = client.executeMethod(method);
      val headers = for {
        // This API sucks so much it's not even funny
        h <- method.getResponseHeaders
        e <- h.getElements
      } yield (h.getName -> e.getName)

      // Builds the response tuple
      (statusCode, Map(headers: _*), List(method.getResponseBodyAsString()));
    } catch {
      case ex: Exception => ex.printStackTrace(); (400, Map(), List(ex.toString()))
    } finally {
      method.releaseConnection();
    }
  }
}
