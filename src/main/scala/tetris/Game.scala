package tetris

import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import tetris.datas.{Color, InputKeys, Piece, Pieces, Point}

case class Cell(var color: Color = Color.Black)

case class GameContext(bounds: Point, linesCleared: Int = 0, prevKeys: Set[Int] = Set.empty[Int]) {
  val blockWidth: Int          = 20
  val gridDims: Point          = Point(13, bounds.y / blockWidth)
  val leftBorder: Double       = (bounds.x - blockWidth * gridDims.x) / 2
  val grid: Array[Array[Cell]] = Array.fill(gridDims.x.toInt, gridDims.y.toInt)(Cell())

  def incrementLinesCleard: GameContext =
    this.copy(linesCleared = linesCleared + 1)

  def updateKeyInputs(keys: Set[Int]): GameContext =
    this.copy(prevKeys = keys)
}

object GameContext {
  def initialValue(bounds: Point): GameContext = GameContext(bounds)
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

  private val pieces              = Pieces.all
  private var gameCtx             = GameContext.initialValue(bounds)
  private var moveCount           = 0
  private var nextPiece: Piece    = pieces.randomNext()
  private var currentPiece: Piece = pieces.randomNext()
  private var piecePos            = Point(gameCtx.gridDims.x / 2, 0)

  var result: Option[String] = None

  def findCollisions(offset: Point): IndexedSeq[Unit] = {
    val pts = currentPiece.iterator(piecePos).toArray
    for {
      index <- 0 until pts.length
      (i, j) = pts(index)
      newPt  = Point(i, j) + offset
      if !newPt.within(Point(0, 0), gameCtx.gridDims) || gameCtx
        .grid(newPt.x.toInt)(
          newPt.y.toInt
        )
        .color != Color.Black
    } yield ()
  }

  def moveDown(): Unit = {
    val collisions = findCollisions(Point(0, 1))
    val pts        = currentPiece.iterator(piecePos).toArray
    if (collisions.length > 0) {
      for (index <- 0 until pts.length) {
        val (i, j) = pts(index)
        gameCtx.grid(i)(j).color = currentPiece.color
      }
      currentPiece = nextPiece
      nextPiece = pieces.randomNext()
      piecePos = Point(gameCtx.gridDims.x / 2, 0)
      if (!findCollisions(Point(0, 0)).isEmpty) {
        result = Some("The board has filled up!")
        resetGame()
      }
    } else {
      piecePos += Point(0, 1)
    }
  }

  def update(keys: Set[Int]): Unit = {
    if (keys(InputKeys.KEY_LEFT) && findCollisions(Point(-1, 0)).isEmpty)
      piecePos += Point(-1, 0)
    if (keys(InputKeys.KEY_RIGHT) && findCollisions(Point(1, 0)).isEmpty)
      piecePos += Point(1, 0)
    if (keys(InputKeys.KEY_SPACE) && !gameCtx.prevKeys(InputKeys.KEY_SPACE)) {
      currentPiece = currentPiece.rotate()
      if (findCollisions(Point(0, 0)).nonEmpty) {
        for (_ <- 0 until 3) currentPiece = currentPiece.rotate()
      }
    }
    if (keys(InputKeys.KEY_DOWN)) moveDown()

    gameCtx.updateKeyInputs(keys)

    if (moveCount > 0) moveCount -= 1
    else {
      moveCount = 15
      moveDown()
    }

    def row(i: Int): IndexedSeq[Cell] =
      (0 until gameCtx.gridDims.x.toInt).map(j => gameCtx.grid(j)(i))

    var remaining = for {
      i <- (gameCtx.gridDims.y.toInt - 1 to 0 by -1).toList
      if !row(i).forall(_.color != Color.Black)
    } yield i

    for (i <- gameCtx.gridDims.y.toInt - 1 to 0 by -1) remaining match {
      case first :: rest =>
        remaining = rest
        for ((oldS, newS) <- row(i).zip(row(first))) {
          oldS.color = newS.color
        }
      case _ =>
        gameCtx = gameCtx.incrementLinesCleard
        for (s <- gameCtx.grid(i)) s.color = Color.Black
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
    } fillBlock(i, j, gameCtx.grid(i)(j).color)

    draw(currentPiece, piecePos, external = false)
    draw(nextPiece, Point(18, 9), external = true)

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
  ) = {
    val pts = piece.iterator(pos)
    for (index <- 0 until pts.length) {
      val (i, j) = pts(index)
      if (Point(i, j).within(Point(0, 0), gameCtx.gridDims) || external)
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
