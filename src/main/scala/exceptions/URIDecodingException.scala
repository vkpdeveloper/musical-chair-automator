package org.musical.chairs
package exceptions

class URIDecodingException(message: String, url: String, cause: Throwable = null) extends Exception(message, cause) {

  @inline
  def getUrl: String = this.url
}
