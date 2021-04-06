package tetris

import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import tetris.datas.{Color, InputKeys, Piece, Pieces, Point}

case class Cell(var color: Color = Color.Black)

case class Row(cells: IndexedSeq[Cell]) {

  def hasBlock: Boolean =
    cells.forall(_.color != Color.Black)

  def cell(i: Int): Cell =
    cells(i)

  def zip(that: Row): IndexedSeq[(Cell, Cell)] =
    cells.zip(that.cells)

  def clear(): Row =
    Row(cells.map(_ => Cell()))
}

object Row {
  def gen(num: Int): Row =
    Row(IndexedSeq.fill(num)(Cell()))
}

case class Grid(rows: IndexedSeq[Row]) {
  def row(i: Int): Row =
    rows(i)

  def cell(x: Int, y: Int): Cell =
    row(y).cell(x)

  def clearRow(i: Int): Grid =
    Grid(
      for {
        (r, index) <- rows.zipWithIndex
      } yield if (i == index) r.clear() else r
    )
}

object Grid {
  def gen(width: Int, height: Int): Grid =
    Grid(IndexedSeq.fill(height)(Row.gen(width)))
}

case class GameContext(
    bounds: Point,
    blockWidth: Int,
    gridDims: Point,
    grid: Grid,
    linesCleared: Int,
    prevKeys: Set[Int],
    piecePos: Point,
    currentPiece: Piece,
    nextPiece: Piece,
    var moveCount: Int
) {
  private final val DEFAULT_MOVE_COUNT = 15

  val leftBorder: Double   = (bounds.x - blockWidth * gridDims.x) / 2
  val startPosition: Point = GameContext.startPosition(gridDims)

  def incrementLinesCleard(): GameContext =
    copy(linesCleared = linesCleared + 1)

  def setDefaultMoveCount(): Unit =
    moveCount = DEFAULT_MOVE_COUNT

  def decrementMoveCount(): Unit =
    moveCount = moveCount - 1

  def resetToStartPoint(): GameContext =
    copy(piecePos = startPosition)

  def updatePiecePosition(p: Point): GameContext =
    copy(piecePos = p)

  def moveCurrentPieceToDown(): GameContext =
    copy(piecePos = piecePos + Point(0, 1))

  def moveCurrentPieceToLeft(): GameContext =
    copy(piecePos = piecePos + Point(-1, 0))

  def moveCurrentPieceToRight(): GameContext =
    copy(piecePos = piecePos + Point(1, 0))

  def updateKeyInputs(keys: Set[Int]): GameContext =
    copy(prevKeys = keys)

  def getRow(i: Int): Row =
    grid.row(i)

  def getCell(x: Int, y: Int): Cell =
    grid.cell(x, y)

  def getCell(p: Point): Cell =
    getCell(p.x.toInt, p.y.toInt)

  def clearRow(i: Int): GameContext =
    copy(grid = grid.clearRow(i))

  def genNextPiece(): GameContext =
    copy(currentPiece = nextPiece, nextPiece = Pieces.all.randomNext())

  def rotatePiece(): GameContext =
    copy(currentPiece = currentPiece.rotate())

  def rotatePiece1Lap(): GameContext =
    copy(currentPiece = currentPiece.rotate1Lap())

  def within(p: Point): Boolean =
    p.within(Point(0, 0), gridDims)
}

object GameContext {

  def startPosition(gridDims: Point): Point =
    Point(gridDims.x / 2, 0)

  def initialValue(bounds: Point): GameContext = {
    val blockWidth = 20
    val gridDims   = Point(13, bounds.y / blockWidth)
    val grid       = Grid.gen(gridDims.x.toInt, gridDims.y.toInt)
    val pieces     = Pieces.all

    GameContext(
      bounds = bounds,
      blockWidth = blockWidth,
      gridDims = gridDims,
      grid = grid,
      linesCleared = 0,
      prevKeys = Set.empty[Int],
      piecePos = startPosition(gridDims),
      nextPiece = pieces.randomNext(),
      currentPiece = pieces.randomNext(),
      moveCount = 0
    )
  }
}

case class Game(bounds: Point, val resetGame: () => Unit) {

  implicit class pimpedContext(val ctx: dom.CanvasRenderingContext2D) {
    def fillCircle(x: Double, y: Double, r: Double) = {
      ctx.beginPath()
      ctx.arc(x, y, r, 0, math.Pi * 2)
      ctx.fill()
    }

    def strokePath(points: Point*) = {
      ctx.beginPath()
      ctx.moveTo(points.last.x, points.last.y)
      for (p <- points) {
        ctx.lineTo(p.x, p.y)
      }
      ctx.stroke()
    }
  }

  private var gameCtx = GameContext.initialValue(bounds)

  var result: Option[String] = None

  def findCollisions(offset: Point): IndexedSeq[Unit] = {
    val pts = gameCtx.currentPiece.iterator(gameCtx.piecePos).toArray
    for {
      index <- 0 until pts.length
      (i, j) = pts(index)
      newPt  = Point(i, j) + offset
      if !gameCtx.within(newPt) || gameCtx.getCell(newPt).color != Color.Black
    } yield ()
  }

  def moveDown(): Unit = {
    val collisions = findCollisions(Point(0, 1))
    val pts        = gameCtx.currentPiece.iterator(gameCtx.piecePos).toArray
    if (collisions.length > 0) {
      for (index <- 0 until pts.length) {
        val (i, j) = pts(index)
        gameCtx.getCell(i, j).color = gameCtx.currentPiece.color
      }
      gameCtx = gameCtx.genNextPiece()
      gameCtx = gameCtx.resetToStartPoint()
      if (!findCollisions(Point(0, 0)).isEmpty) {
        result = Some("The board has filled up!")
        resetGame()
      }
    } else {
      gameCtx = gameCtx.moveCurrentPieceToDown()
    }
  }

  def update(keys: Set[Int]): Unit = {
    if (keys(InputKeys.KEY_LEFT) && findCollisions(Point(-1, 0)).isEmpty)
      gameCtx = gameCtx.moveCurrentPieceToLeft()
    if (keys(InputKeys.KEY_RIGHT) && findCollisions(Point(1, 0)).isEmpty)
      gameCtx = gameCtx.moveCurrentPieceToRight()
    if (keys(InputKeys.KEY_SPACE) && !gameCtx.prevKeys(InputKeys.KEY_SPACE)) {
      gameCtx = gameCtx.rotatePiece()
      if (findCollisions(Point(0, 0)).nonEmpty) {
        gameCtx = gameCtx.rotatePiece1Lap()
      }
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
      i <- (gameCtx.gridDims.y.toInt - 1 to 0 by -1).toList
      if !gameCtx.getRow(i).hasBlock
    } yield i

    for (i <- gameCtx.gridDims.y.toInt - 1 to 0 by -1) remaining match {
      case first :: rest =>
        remaining = rest
        for ((oldS, newS) <- gameCtx.getRow(i).zip(gameCtx.getRow(first))) {
          oldS.color = newS.color
        }
      case _ =>
        gameCtx = gameCtx.incrementLinesCleard().clearRow(i)
    }
  }

  def draw(implicit ctx: CanvasRenderingContext2D): Unit = {
    val blockWidth = gameCtx.blockWidth

    ctx.fillStyle = Color.Black.value
    ctx.fillRect(0, 0, bounds.x, bounds.y)

    ctx.textAlign = "left"
    ctx.fillStyle = Color.White.value
    ctx.fillText(
      "Lines Cleared: " + gameCtx.linesCleared,
      gameCtx.leftBorder * 1.3 + gameCtx.gridDims.x * blockWidth,
      100
    )
    ctx.fillText("Next Block", gameCtx.leftBorder * 1.35 + gameCtx.gridDims.x * blockWidth, 150)

    for {
      i <- 0 until gameCtx.gridDims.x.toInt
      j <- 0 until gameCtx.gridDims.y.toInt
    } fillBlock(i, j, gameCtx.getCell(i, j).color)

    draw(gameCtx.currentPiece, gameCtx.piecePos, external = false)
    draw(gameCtx.nextPiece, Point(18, 9), external = true)

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

  def draw(piece: Piece, pos: Point, external: Boolean)(implicit
      ctx: dom.CanvasRenderingContext2D
  ): Unit = {
    val pts = piece.iterator(pos)
    for (index <- 0 until pts.length) {
      val (i, j) = pts(index)
      if (gameCtx.within(Point(i, j)) || external)
        fillBlock(i, j, piece.color)
    }
  }

  private def fillBlock(i: Int, j: Int, color: Color)(implicit
      ctx: dom.CanvasRenderingContext2D
  ): Unit = {
    val blockWidth = gameCtx.blockWidth

    ctx.fillStyle = color.replace(255, 128).value
    ctx.fillRect(gameCtx.leftBorder + i * blockWidth, 0 + j * blockWidth, blockWidth, blockWidth)
    ctx.strokeStyle = color.value
    ctx.strokeRect(gameCtx.leftBorder + i * blockWidth, 0 + j * blockWidth, blockWidth, blockWidth)
  }
}
