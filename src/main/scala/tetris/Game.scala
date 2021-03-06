package tetris

import org.scalajs.dom.CanvasRenderingContext2D
import tetris.datas._

case class Game(bounds: Point, resetGame: () => Unit) {

  var gameCtx = GameContext.initialValue(bounds)

  def hasCollisions(offset: Point): Boolean = {
    val lines = for {
      point <- gameCtx.currentPiece.iterator(gameCtx.piecePos)
      newPt = point + offset
      if !gameCtx.within(newPt) || gameCtx.getCell(newPt).color != Color.Black
    } yield ()
    lines.nonEmpty
  }

  def moveDown(): Unit =
    if (hasCollisions(Point(0, 1))) {
      for {
        point <- gameCtx.currentPiece.iterator(gameCtx.piecePos)
      } gameCtx.getCell(point).color = gameCtx.currentPiece.color

      gameCtx = gameCtx.genNextPiece()
      gameCtx = gameCtx.resetToStartPoint()
      if (hasCollisions(Point(0, 0))) {
        gameCtx = gameCtx.updateSuccess("The board has filled up!")
        resetGame()
      }
    } else
      gameCtx = gameCtx.moveCurrentPieceToDown()

  def update(keys: Set[Int]): Unit = {
    if (keys(InputKeys.KEY_LEFT) && !hasCollisions(Point(-1, 0)))
      gameCtx = gameCtx.moveCurrentPieceToLeft()
    if (keys(InputKeys.KEY_RIGHT) && !hasCollisions(Point(1, 0)))
      gameCtx = gameCtx.moveCurrentPieceToRight()
    if (keys(InputKeys.KEY_SPACE) && !gameCtx.prevKeys(InputKeys.KEY_SPACE)) {
      gameCtx = gameCtx.rotatePiece()
      if (hasCollisions(Point(0, 0))) gameCtx = gameCtx.rotatePiece1Lap()
    }
    if (keys(InputKeys.KEY_DOWN)) moveDown()

    gameCtx = gameCtx.updateKeyInputs(keys)

    // TODO: move count is mutable
    if (gameCtx.moveCount > 0) {
      gameCtx.decrementMoveCount()
    } else {
      gameCtx.setDefaultMoveCount()
      moveDown()
    }

    var remaining = for {
      i <- (gameCtx.grid.height - 1 to 0 by -1).toList
      if !gameCtx.getRow(i).hasBlock
    } yield i

    for (i <- gameCtx.grid.height - 1 to 0 by -1) remaining match {
      case first :: rest =>
        remaining = rest
        for ((oldS, newS) <- gameCtx.getRow(i).zip(gameCtx.getRow(first))) {
          oldS.color = newS.color
        }
      case _ =>
        gameCtx = gameCtx.incrementLinesCleard().clearRow(i)
    }
  }

  implicit class PimpedContext(val ctx: CanvasRenderingContext2D) {
    def strokePath(points: Point*) = {
      ctx.beginPath()
      ctx.moveTo(points.last.x, points.last.y)
      for (p <- points) {
        ctx.lineTo(p.x, p.y)
      }
      ctx.stroke()
    }
  }

  def draw(implicit ctx: CanvasRenderingContext2D): Unit = {
    fillBackground(ctx)
    drawGameStatus(ctx)
    drawNextBlock(ctx)
    draw(gameCtx.currentPiece, gameCtx.piecePos, external = false)
    draw(gameCtx.nextPiece, Point(18, 9), external = true)
    drawVerticalLines(ctx)
  }

  private def draw(piece: Piece, pos: Point, external: Boolean)(implicit
      ctx: CanvasRenderingContext2D
  ): Unit =
    for {
      point <- piece.iterator(pos)
      if gameCtx.within(point) || external
    } fillBlock(point, piece.color)

  private def fillBlock(point: Point, color: Color)(implicit
      ctx: CanvasRenderingContext2D
  ): Unit =
    fillBlock(point.x.toInt, point.y.toInt, color)

  private def fillBlock(i: Int, j: Int, color: Color)(implicit
      ctx: CanvasRenderingContext2D
  ): Unit = {
    val blockWidth = gameCtx.blockWidth

    ctx.fillStyle = color.replace(255, 128).value
    ctx.fillRect(gameCtx.leftBorder + i * blockWidth, 0 + j * blockWidth, blockWidth, blockWidth)
    ctx.strokeStyle = color.value
    ctx.strokeRect(gameCtx.leftBorder + i * blockWidth, 0 + j * blockWidth, blockWidth, blockWidth)
  }

  private def fillBackground(implicit ctx: CanvasRenderingContext2D): Unit = {
    ctx.fillStyle = Color.Black.value
    ctx.fillRect(0, 0, bounds.x, bounds.y)
  }

  private def drawGameStatus(implicit ctx: CanvasRenderingContext2D): Unit = {
    val blockWidth = gameCtx.blockWidth

    ctx.textAlign = "left"
    ctx.fillStyle = Color.White.value
    ctx.fillText(
      "Lines Cleared: " + gameCtx.linesCleared,
      gameCtx.leftBorder * 1.3 + gameCtx.grid.width * blockWidth,
      100
    )
  }

  private def drawNextBlock(implicit ctx: CanvasRenderingContext2D): Unit = {
    ctx.fillText(
      "Next Block",
      gameCtx.leftBorder * 1.35 + gameCtx.grid.width * gameCtx.blockWidth,
      150
    )
    for {
      i <- 0 until gameCtx.grid.width
      j <- 0 until gameCtx.grid.height
    } fillBlock(i, j, gameCtx.getCell(i, j).color)
  }

  private def drawVerticalLines(ctx: CanvasRenderingContext2D): Unit = {
    ctx.strokeStyle = Color.White.value
    ctx.strokePath(
      Point(gameCtx.leftBorder, 0),
      Point(gameCtx.leftBorder, bounds.y)
    )
    ctx.strokePath(
      Point(bounds.x - gameCtx.leftBorder, 0),
      Point(bounds.x - gameCtx.leftBorder, bounds.y)
    )
  }
}
