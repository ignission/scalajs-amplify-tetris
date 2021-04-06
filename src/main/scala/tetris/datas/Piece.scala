package tetris.datas

case class Piece(value: Seq[Array[Int]], color: Color) {

  val width: Int    = value.length
  val height: Int   = value(0).length
  val center: Point = Point(width - 1, height - 1) / 2

  def iterator(offset: Point = Point(0, 0)): IndexedSeq[Point] =
    for {
      i <- 0 until value.length
      j <- 0 until value(0).length
      if value(i)(j) != 0
    } yield Point(i + offset.x, j + offset.y)

  def rotate(): Piece = {
    val out = Seq.fill(width)(Array.fill(height)(0))

    for {
      w <- 0 until width
      h <- 0 until height
      centered = Point(w, h) - center
      rotated  = Point(centered.y * -1, centered.x * 1) + center
    } out(rotated.x.toInt)(rotated.y.toInt) = value(w)(h)

    Piece(out, color)
  }

  def rotate1Lap(): Piece =
    (0 until 3).foldLeft(this)((item, _) => item.rotate())

  override def toString(): String =
    value
      .map(line => "[" + line.mkString(" ") + "]")
      .mkString("\n")
}
