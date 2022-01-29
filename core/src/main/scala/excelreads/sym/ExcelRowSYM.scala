package excelreads.sym

import cats.Monad
import excelreads.ExcelRowReads
import excelreads.eff.ExcelReadsEffects.*
import excelreads.exception.ExcelParseError.ExcelParseErrors

/** Basic getter interface from an Excel row
  *
  * @tparam R
  *   Effect stack for parsing cells
  */
abstract class ExcelRowSYM[Row, R: _state, F[_]: Monad] {
  def isEmpty: F[Boolean]

  def isEnd: F[Boolean]

  def getRow: F[Row]

  def withRow[A](
    f: ExcelRowReads[R, A]
  ): F[Either[ExcelParseErrors, A]]
}
