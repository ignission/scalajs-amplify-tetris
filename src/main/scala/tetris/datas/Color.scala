package tetris.datas

sealed abstract class Color(val r: Int, val g: Int, val b: Int) {
  val value: String = s"rgb($r, $g, $b)"
  def replace(before: Int, after: Int): Color = {
    val newR = if (before == r) after else r
    val newG = if (before == g) after else g
    val newB = if (before == b) after else b

    new Color(newR, newG, newB) {
      override val r: Int = newR
      override val g: Int = newG
      override val b: Int = newB
    }
  }
}

object Color {
  case object White   extends Color(255, 255, 255)
  case object Red     extends Color(255, 0, 0)
  case object Green   extends Color(0, 255, 0)
  case object Blue    extends Color(0, 0, 255)
  case object Cyan    extends Color(0, 255, 255)
  case object Magenta extends Color(255, 0, 255)
  case object Yellow  extends Color(255, 255, 0)
  case object Black   extends Color(0, 0, 0)

  val all: Seq[Color] = Seq(
    White,
    Red,
    Green,
    Blue,
    Cyan,
    Magenta,
    Yellow,
    Black
  )
}
