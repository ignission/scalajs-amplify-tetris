package tetris.datas

case class GameContext(
    bounds: Point,
    blockWidth: Int,
    private val gridDims: Point,
    grid: Grid,
    linesCleared: Int,
    prevKeys: Set[Int],
    piecePos: Point,
    currentPiece: Piece,
    nextPiece: Piece,
    result: Option[String],
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
    copy(currentPiece = nextPiece, nextPiece = Pieces.randomNext())

  def rotatePiece(): GameContext =
    copy(currentPiece = currentPiece.rotate())

  def rotatePiece1Lap(): GameContext =
    copy(currentPiece = currentPiece.rotate1Lap())

  def within(p: Point): Boolean =
    p.within(Point(0, 0), gridDims)

  def updateSuccess(message: String): GameContext =
    copy(result = Some(message))
}

object GameContext {

  def startPosition(gridDims: Point): Point =
    Point(gridDims.x / 2, 0)

  def initialValue(bounds: Point): GameContext = {
    val blockWidth = 20
    val gridDims   = Point(13, bounds.y / blockWidth)
    val grid       = Grid.gen(gridDims.x.toInt, gridDims.y.toInt)

    GameContext(
      bounds = bounds,
      blockWidth = blockWidth,
      gridDims = gridDims,
      grid = grid,
      linesCleared = 0,
      prevKeys = Set.empty[Int],
      piecePos = startPosition(gridDims),
      nextPiece = Pieces.randomNext(),
      currentPiece = Pieces.randomNext(),
      result = None,
      moveCount = 0
    )
  }
}
