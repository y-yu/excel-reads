package excelreads

import cats.data.State
import cats.data.ValidatedNel
import excelreads.exception.ExcelParseError
import excelreads.exception.ExcelParseError.ExcelParseErrors
import excelreads.instance.ExcelSheetReadsInstances
import org.atnos.eff.Eff
import org.atnos.eff.|=
import excelreads.util.ExcelSheetReadsParse
import excelreads.util.ExcelSheetReadsParseLoop

/** Excel sheet parser type class
  *
  * @tparam R
  *   Effect stack
  * @tparam A
  *   Parsing row type
  */
abstract class ExcelSheetReads[R, A] { self =>
  type Result

  def parse(implicit
    m1: State[Int, *] |= R,
    m2: Either[ExcelParseErrors, *] |= R
  ): Eff[R, Result]

  final def product[B](reads: ExcelSheetReads[R, B]): ExcelSheetReads[R, (A, B)] =
    new ExcelSheetReads[R, (A, B)] {
      type Result = (self.Result, reads.Result)

      def parse(implicit
        m1: State[Int, *] |= R,
        m2: Either[ExcelParseErrors, *] |= R
      ): Eff[R, (self.Result, reads.Result)] =
        self.parse product reads.parse
    }
}

object ExcelSheetReads extends ExcelSheetReadsParse with ExcelSheetReadsParseLoop with ExcelSheetReadsInstances {

  def from[R, A, B](
    f: State[Int, *] |= R => Either[ExcelParseErrors, *] |= R => Eff[R, B]
  ): ExcelSheetReads[R, A] = new ExcelSheetReads[R, A] {
    type Result = B

    def parse(implicit
      m1: State[Int, *] |= R,
      m2: Either[ExcelParseErrors, *] |= R
    ): Eff[R, B] =
      f(m1)(m2)
  }
}
