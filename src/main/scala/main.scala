package org.musical.chairs

import exceptions.URIDecodingException
import AudioController.{loadSong, playForFrames}

import javazoom.jl.player.Player

import java.net.URI
import scala.io.StdIn
import scala.util.{Failure, Random, Success}
import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global

var loadedPlayers = scala.collection.mutable.Map[String, Player]()

def parsePlaylistURL(): String = {
  print("Enter the playlist url: ")
  val url = StdIn.readLine()
  val parsedUrl = URI.create(url)
  val urlQuery = parsedUrl.getQuery()
  if (urlQuery == null) {
    throw new URIDecodingException("failed to decode the playlist id from given url.", url)
  }
  val queries = urlQuery.split("&")
  if (queries.isEmpty) {
    throw new URIDecodingException("failed to decode the playlist id from given url.", url)
  }
  val playlistId = queries.head.split("=").last
  playlistId
}

def getRandomFrame(min: Int, max: Int): Int = {
  require(min <= max, "Min value must be less than or equal to max value")
  val randomSec = Random.nextInt((max - min) + 1) + min
  return randomSec * 40
}

def pickRandomSong[T](array: Array[T]): Option[T] = {
  if (array.isEmpty) {
    None // Return None if the array is empty
  } else {
    val randomIndex = Random.nextInt(array.length)
    Some(array(randomIndex)) // Return Some with the randomly picked item
  }
}

def startMusicalChair(songs: Array[Item], currentlyPlaying: Option[Item] = None, shouldChange: Boolean): Unit = {
  val currentSong = shouldChange match {
    case false => {
      currentlyPlaying
      match {
        case Some(s) => s
        case None => {
          pickRandomSong(songs) match {
            case Some(s) => s
            case None => throw Exception("failed to find the song")
          }
        }
      }
    }
    case true
    => pickRandomSong(songs) match {
      case Some(s) => s
      case None => throw Exception("failed to find the song")
    }
  }

  println(s"Playing: ${currentSong.snippet.title}")
  val player = loadedPlayers.contains(currentSong.snippet.resourceId.videoId) match {
    case true => loadedPlayers(currentSong.snippet.resourceId.videoId)
    case false => loadSong(s"./songs/${currentSong.snippet.resourceId.videoId}.mp3")
  }

  // Adding the player in the loaded players map
  loadedPlayers(currentSong.snippet.resourceId.videoId) = player;

  val randomFrames = getRandomFrame(25, 50)
  val isLastFramePlayer = playForFrames(player, randomFrames)

  if (isLastFramePlayer) {
    return startMusicalChair(songs, None, true)
  }

  Thread.sleep(3000)

  val newShouldChange = Random.nextInt(2) match {
    case 0 => false
    case 1 => true
  }

  return startMusicalChair(songs, Some(currentSong), newShouldChange)
}


@main
def main(): Unit = {
  try {
    val playlistId = parsePlaylistURL()
    val musicPlaylistItems = YouTubeClient.getPlaylistItems(playlistId, 20)
    val downloadFutures = musicPlaylistItems.map(song => YouTubeClient.downloadSong(song.snippet.resourceId.videoId))

    downloadFutures.foreach { f =>
      f.onComplete {
        case Success(id: String) => println(s"Downloaded song by id: $id")
        case Failure(exception) => println(s"failed to download a song")
      }
    }

    scala.concurrent.Await.ready(Future.sequence(downloadFutures), scala.concurrent.duration.Duration.Inf)

    startMusicalChair(musicPlaylistItems.toArray, None, false)
  } catch {
    case e: URIDecodingException => println(s"URL decoding failed -> message: ${e.getMessage}, url: ${e.getUrl}")
    case e: Exception => {
      throw e;
    }
  }
}