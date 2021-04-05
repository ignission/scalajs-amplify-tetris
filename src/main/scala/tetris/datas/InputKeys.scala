package tetris.datas

object InputKeys {

  final val KEY_SPACE = 32
  final val KEY_LEFT  = 37
  final val KEY_RIGHT = 39
  final val KEY_DOWN  = 40

  final val definedAll: Seq[Int] = Seq(
    KEY_SPACE,
    KEY_LEFT,
    KEY_RIGHT,
    KEY_DOWN
  )

  def contains(keyCode: Int): Boolean =
    definedAll.contains(keyCode)
}
