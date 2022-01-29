package excelreads.sym

import cats.Monad
import cats.data.State
import excelreads.ExcelRowReads
import excelreads.exception.ExcelParseError.ExcelParseErrors
import org.atnos.eff.|=

/** Basic getter interface from an Excel row
  *
  * @tparam R
  *   Effect stack for parsing cells
  */
abstract class ExcelRowSYM[Row, R, F[_]: Monad](implicit
  m: State[Int, *] |= R
) {
  def isEmpty: F[Boolean]

  def isEnd: F[Boolean]

  def getRow: F[Row]

  def withRow[A](
    f: ExcelRowReads[R, A]
  ): F[Either[ExcelParseErrors, A]]
}
