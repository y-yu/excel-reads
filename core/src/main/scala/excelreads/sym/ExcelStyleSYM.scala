package excelreads.sym

import cats.Monad

/** Cell style getter interface
  *
  * @tparam Style
  *   cell style type
  */
abstract class ExcelStyleSYM[Style, F[_]: Monad] {

  /** Get `Style` data in the cell
    */
  def getStyle: F[Option[Style]]
}
