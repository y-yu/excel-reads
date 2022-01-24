package excelreads

import cats.data.State
import cats.data.ValidatedNel
import excelreads.exception.ExcelParseError
import excelreads.instance.ExcelSheetReadsInstances
import org.atnos.eff.Eff
import org.atnos.eff.|=

/** Excel sheet parser type class
  *
  * @tparam R
  *   Effect stack
  * @tparam A
  *   Result type
  */
abstract class ExcelSheetReads[R, A] {
  type Result

  def parse(implicit
    m: State[Int, *] |= R
  ): Eff[R, ValidatedNel[ExcelParseError, Result]]
}

object ExcelSheetReads extends ExcelSheetReadsUtils with ExcelSheetReadsInstances {

  def from[R, A, B](
    f: State[Int, *] |= R => Eff[R, ValidatedNel[ExcelParseError, B]]
  ): ExcelSheetReads[R, A] = new ExcelSheetReads[R, A] {
    type Result = B

    def parse(implicit m: State[Int, *] |= R): Eff[R, ValidatedNel[ExcelParseError, B]] =
      f(m)
  }
}
