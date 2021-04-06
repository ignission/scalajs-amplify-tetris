package tetris.datas

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
