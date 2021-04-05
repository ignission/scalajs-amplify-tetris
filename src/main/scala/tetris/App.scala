package tetris

import org.scalajs.dom
import org.scalajs.dom.html
import tetris.datas.{Color, Point}

import scala.collection.mutable
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

object App {

  private val canvas    = dom.document.getElementById("tetris").asInstanceOf[html.Canvas]
  private val canvasCtx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
  private val bounds    = Point(canvas.width, canvas.height)
  private val keys      = mutable.Set.empty[Int]

  private var game                    = Game(bounds, () => resetGame())
  private var active                  = false
  private var firstFrame              = false
  private var message: Option[String] = None

  canvas.onkeydown = { (e: dom.KeyboardEvent) =>
    keys.add(e.keyCode.toInt)
    if (Seq(32, 37, 38, 39, 40).contains(e.keyCode.toInt)) e.preventDefault()
    message = None
  }
  canvas.onkeyup = { (e: dom.KeyboardEvent) =>
    keys.remove(e.keyCode.toInt)
    if (Seq(32, 37, 38, 39, 40).contains(e.keyCode.toInt)) e.preventDefault()
  }

  canvas.onfocus = { _ => active = true }
  canvas.onblur = { _ => active = false }

  def resetGame(): Unit = {
    message = game.result
    println("MESSAGE " + message)
    game = Game(bounds, () => resetGame())
  }

  def update(): Unit = {
    if (!firstFrame) {
      game.draw(canvasCtx)
      firstFrame = true
    }
    if (active && message.isEmpty) {
      game.draw(canvasCtx)
      game.update(keys.toSet)
    } else if (message.isDefined) {
      canvasCtx.fillStyle = Color.Black.value
      canvasCtx.fillRect(0, 0, bounds.x, bounds.y)
      canvasCtx.fillStyle = Color.White.value
      canvasCtx.font = "20pt Arial"
      canvasCtx.textAlign = "center"
      canvasCtx.fillText(message.get, bounds.x / 2, bounds.y / 2)
      canvasCtx.font = "14pt Arial"
      canvasCtx.fillText("Press any key to continue", bounds.x / 2, bounds.y / 2 + 30)
    }
  }

  @JSExportTopLevel("main")
  def main(): Unit = {

    canvasCtx.font = "12pt Arial"
    canvasCtx.textAlign = "center"

    js.timers.setInterval(50) {
      update()
    }
  }
}
