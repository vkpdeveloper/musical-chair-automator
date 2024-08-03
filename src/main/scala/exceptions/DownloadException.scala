package org.musical.chairs
package exceptions

class DownloadException(id: String, message: String, cause: Throwable = null) extends Exception(message, cause)
