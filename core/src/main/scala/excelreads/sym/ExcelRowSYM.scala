package excelreads.sym

import cats.Monad
import cats.data.State
import cats.data.ValidatedNel
import excelreads.ExcelReads
import excelreads.exception.ExcelParseError
import org.atnos.eff.|=

/** Basic getter interface from an Excel row
  *
  * @tparam R
  *   Effect stack for parsing cells
  */
abstract class ExcelRowSYM[Row, R, F[_]: Monad](implicit
  m: State[Int, *] |= R
) {
  def isEmpty(
    index: Int
  ): F[ValidatedNel[ExcelParseError, Boolean]]

  def getRow(
    index: Int
  ): F[ValidatedNel[ExcelParseError, Row]]

  def withRow[A](
    index: Int,
    f: ExcelReads[R, A]
  ): F[ValidatedNel[ExcelParseError, A]]
}
