package excelreads.sym

import cats.Monad
import excelreads.ExcelRowReads
import excelreads.exception.ExcelParseError.ExcelParseErrors

/** Basic getter interface from an Excel row
  *
  * @tparam R
  *   Effect stack for parsing cells
  */
abstract class ExcelRowSYM[Row, R, F[_]: Monad] {
  def isEmpty: F[Boolean]

  def isEnd: F[Boolean]

  def getRow: F[Row]

  def withRow[A](
    reads: ExcelRowReads[R, A]
  ): F[Either[ExcelParseErrors, A]]
}
