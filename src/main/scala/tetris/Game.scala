package tetris

import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import tetris.datas.{Color, Piece, Pieces, Point}

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

  private final val KEY_SPACE = 32
  private final val KEY_LEFT  = 37
  private final val KEY_RIGHT = 39
  private final val KEY_DOWN  = 40

  private val pieces = Pieces.all

  var result: Option[String] = None

  var moveCount           = 0
  var keyCount            = 0
  val blockWidth          = 20
  val gridDims            = Point(13, bounds.y / blockWidth)
  val leftBorder          = (bounds.x - blockWidth * gridDims.x) / 2
  var linesCleared        = 0
  var nextPiece: Piece    = pieces.randomNext()
  var currentPiece: Piece = pieces.randomNext()
  var piecePos            = Point(gridDims.x / 2, 0)

  case class Cell(var color: Color = Color.Black)

  val grid     = Array.fill(gridDims.x.toInt, gridDims.y.toInt)(Cell())
  var prevKeys = Set.empty[Int]

  def findCollisions(offset: Point) = {
    val pts = currentPiece.iterator(piecePos).toArray
    for {
      index <- 0 until pts.length
      (i, j) = pts(index)
      newPt  = Point(i, j) + offset
      if !newPt.within(Point(0, 0), gridDims) || grid(newPt.x.toInt)(
        newPt.y.toInt
      ).color != Color.Black
    } yield ()
  }

  def moveDown() = {
    val collisions = findCollisions(Point(0, 1))
    val pts        = currentPiece.iterator(piecePos).toArray
    if (collisions.length > 0) {
      for (index <- 0 until pts.length) {
        val (i, j) = pts(index)
        grid(i)(j).color = currentPiece.color
      }
      currentPiece = nextPiece
      nextPiece = pieces.randomNext()
      piecePos = Point(gridDims.x / 2, 0)
      if (!findCollisions(Point(0, 0)).isEmpty) {
        result = Some("The board has filled up!")
        resetGame()
      }
    } else {
      piecePos += Point(0, 1)
    }
  }

  def update(keys: Set[Int]): Unit = {
    if (keys(KEY_LEFT) && findCollisions(Point(-1, 0)).isEmpty)
      piecePos += Point(-1, 0)
    if (keys(KEY_RIGHT) && findCollisions(Point(1, 0)).isEmpty)
      piecePos += Point(1, 0)
    if (keys(KEY_SPACE) && !prevKeys(KEY_SPACE)) {
      currentPiece = currentPiece.rotate()
      if (findCollisions(Point(0, 0)).nonEmpty) {
        for (_ <- 0 until 3) currentPiece = currentPiece.rotate()
      }
    }
    if (keys(KEY_DOWN)) moveDown()

    prevKeys = keys

    if (moveCount > 0) moveCount -= 1
    else {
      moveCount = 15
      moveDown()
    }

    def row(i: Int) = (0 until gridDims.x.toInt).map(j => grid(j)(i))
    var remaining = for {
      i <- (gridDims.y.toInt - 1 to 0 by -1).toList
      if !row(i).forall(_.color != Color.Black)
    } yield i

    for (i <- gridDims.y.toInt - 1 to 0 by -1) remaining match {
      case first :: rest =>
        remaining = rest
        for ((oldS, newS) <- row(i).zip(row(first))) {
          oldS.color = newS.color
        }
      case _ =>
        linesCleared += 1
        for (s <- grid(i)) s.color = Color.Black
    }
  }

  def draw(ctx: CanvasRenderingContext2D): Unit = {
    ctx.fillStyle = Color.Black.value
    ctx.fillRect(0, 0, bounds.x, bounds.y)

    ctx.textAlign = "left"
    ctx.fillStyle = Color.White.value
    ctx.fillText("Lines Cleared: " + linesCleared, leftBorder * 1.3 + gridDims.x * blockWidth, 100)
    ctx.fillText("Next Block", leftBorder * 1.35 + gridDims.x * blockWidth, 150)

    def fillBlock(i: Int, j: Int, color: Color): Unit = {
      ctx.fillStyle = color.replace(255, 128).value
      ctx.fillRect(leftBorder + i * blockWidth, 0 + j * blockWidth, blockWidth, blockWidth)
      ctx.strokeStyle = color.value
      ctx.strokeRect(leftBorder + i * blockWidth, 0 + j * blockWidth, blockWidth, blockWidth)
    }
    for {
      i <- 0 until gridDims.x.toInt
      j <- 0 until gridDims.y.toInt
    } fillBlock(i, j, grid(i)(j).color)

    def draw(piece: Piece, pos: Point, external: Boolean) = {
      val pts = piece.iterator(pos)
      for (index <- 0 until pts.length) {
        val (i, j) = pts(index)
        if (Point(i, j).within(Point(0, 0), gridDims) || external)
          fillBlock(i, j, piece.color)
      }
    }
    draw(currentPiece, piecePos, external = false)
    draw(nextPiece, Point(18, 9), external = true)

    ctx.strokeStyle = Color.White.value
    ctx.strokePath(
      Point(leftBorder, 0),
      Point(leftBorder, bounds.y)
    )
    ctx.strokePath(
      Point(bounds.x - leftBorder, 0),
      Point(bounds.x - leftBorder, bounds.y)
    )
  }
}
