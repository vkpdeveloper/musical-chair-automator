package org.musical.chairs

import sttp.client4.*
import sttp.model.Uri
import sttp.client4.httpclient.HttpClientSyncBackend
import exceptions.{DownloadException, URIDecodingException}

import upickle.default.*

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global

// For External Command Execution
import scala.sys.process._

case class YouTubePlaylist(
                            val items: Seq[Item],
                          )derives ReadWriter

case class Item(
                 val id: String,
                 val snippet: Snippet,
               )derives ReadWriter

case class Snippet(
                    val publishedAt: String,
                    val channelId: String,
                    val title: String,
                    val description: String,
                    val channelTitle: String,
                    val playlistId: String,
                    val position: Long,
                    val resourceId: ResourceID,
                    val videoOwnerChannelTitle: String,
                    val videoOwnerChannelId: String
                  )derives ReadWriter

case class ResourceID(
                       val kind: String,
                       val videoId: String
                     )derives ReadWriter

object YouTubeClient {
  implicit val backend: WebSocketSyncBackend = HttpClientSyncBackend()
  private val API_KEY: String = "AIzaSyD9SgDKb8qHU8aAdD42Ol4-MVvauYnmbh4"

  private val silentLogger = ProcessLogger(_ => (), _ => ())

  def getPlaylistItems(id: String, maxResults: Int = 2): List[Item] = {
    val url = s"https://www.googleapis.com/youtube/v3/playlistItems?part=id,snippet&maxResults=$maxResults&playlistId=$id&key=$API_KEY"
    val requestUrl = Uri.parse(url) match {
      case Right(uri: Uri) => uri
      case Left(error) => throw new URIDecodingException("failed to parse the url", url)
    }
    val resp = basicRequest.get(requestUrl).response(asString).send(backend)
    if (!resp.is200) throw new Exception("failed to fetch the data via YouTube API")
    val respBody = resp.body match {
      case Right(b) => b
      case Left(e) => throw new Exception("Not able to fetch the exact body")
    }
    val parsedBody = upickle.default.read[YouTubePlaylist](respBody)
    return parsedBody.items.toList
  }

  def downloadSong(id: String): Future[String] = {
    val promise = Promise[String]()
    Future {
      val command = s"yt-dlp --audio-format mp3 -x -N 10 --path ./songs/ -o $id.mp3 --audio-quality 128K --q https://www.youtube.com/watch?v=$id"
      val exitCode = command ! silentLogger
      if (exitCode != 0) {
        promise.failure(new DownloadException(id, "failed to download the song"))
      } else {
        promise.success(id)
      }
    }
    return promise.future
  }
}
