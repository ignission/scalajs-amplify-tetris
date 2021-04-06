package tetris.datas

import scala.util.Random

case class Pieces(values: Seq[Piece]) {
  val length: Int = values.length

  def randomNext(): Piece =
    values(Random.nextInt(values.length))
}

object Pieces {
  val piece1: Piece = Piece(
    Seq(
      Array(0, 0, 0, 0),
      Array(1, 1, 1, 1),
      Array(0, 0, 0, 0),
      Array(0, 0, 0, 0)
    ),
    Color.White
  )
  val piece2: Piece = Piece(
    Seq(
      Array(1, 1),
      Array(1, 1)
    ),
    Color.Red
  )
  val piece3: Piece = Piece(
    Seq(
      Array(0, 0, 1),
      Array(0, 1, 1),
      Array(0, 1, 0)
    ),
    Color.Green
  )
  val piece4: Piece = Piece(
    Seq(
      Array(1, 0, 0),
      Array(1, 1, 0),
      Array(0, 1, 0)
    ),
    Color.Blue
  )
  val piece5: Piece = Piece(
    Seq(
      Array(0, 1, 0),
      Array(1, 1, 0),
      Array(0, 1, 0)
    ),
    Color.Cyan
  )
  val piece6: Piece = Piece(
    Seq(
      Array(0, 0, 0),
      Array(1, 1, 1),
      Array(0, 0, 1)
    ),
    Color.Magenta
  )
  val piece7: Piece = Piece(
    Seq(
      Array(0, 0, 1),
      Array(1, 1, 1),
      Array(0, 0, 0)
    ),
    Color.Yellow
  )

  val all = Pieces(
    Seq(
      piece1,
      piece2,
      piece3,
      piece4,
      piece5,
      piece6,
      piece7
    )
  )
}
