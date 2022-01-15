package excelreads.sym

import cats.Monad
import cats.data.ValidatedNel
import excelreads.exception.ExcelParseError

/** Cell style getter interface
  *
  * @tparam Style
  *   cell style type
  */
abstract class ExcelStyleSYM[Style, F[_]: Monad] {

  /** Get `Style` data in the cell
    */
  def getStyle(
    index: Int
  ): F[ValidatedNel[ExcelParseError, Option[Style]]]
}
