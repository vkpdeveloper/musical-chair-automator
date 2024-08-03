package org.musical.chairs

import javazoom.jl.decoder.{Bitstream, BitstreamException, Decoder, JavaLayerException}
import javazoom.jl.player.{FactoryRegistry, Player}

import java.io.{BufferedInputStream, FileInputStream, InputStream}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object AudioController {
  def loadSong(filePath: String): Player = {
    val fileInputStream: InputStream = new FileInputStream(filePath)
    val bufferedInputStream: BufferedInputStream = new BufferedInputStream(fileInputStream)
    val player: Player = new Player(bufferedInputStream, FactoryRegistry.systemRegistry.createAudioDevice())

    return player;
  }

  def playForFrames(player: Player, frames: Int): Boolean = {
    return player.play(frames)
  }
}
