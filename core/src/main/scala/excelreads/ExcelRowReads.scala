package excelreads

import cats.implicits.*
import cats.data.State
import cats.data.ValidatedNel
import excelreads.exception.ExcelParseError
import excelreads.instance.ExcelRowReadsInstances
import excelreads.row.ExcelRowReadsUtils
import org.atnos.eff.Eff
import org.atnos.eff.|=

/** Excel rows parser type class
  *
  * @tparam R
  *   Effect stack
  * @tparam A
  *   Result type
  */
abstract class ExcelRowReads[R, A] {
  type Result

  def parse(implicit
    m: State[Int, *] |= R
  ): Eff[R, ValidatedNel[ExcelParseError, Result]]
}

object ExcelRowReads extends ExcelRowReadsUtils with ExcelRowReadsInstances {

  def from[R, A, B](
    f: State[Int, *] |= R => Eff[R, ValidatedNel[ExcelParseError, B]]
  ): ExcelRowReads[R, A] = new ExcelRowReads[R, A] {
    type Result = B

    def parse(implicit m: State[Int, *] |= R): Eff[R, ValidatedNel[ExcelParseError, B]] =
      f(m)
  }
}
