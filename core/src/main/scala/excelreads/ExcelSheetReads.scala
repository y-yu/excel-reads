package excelreads

import cats.data.State
import cats.data.ValidatedNel
import excelreads.exception.ExcelParseError
import excelreads.instance.ExcelSheetReadsInstances
import org.atnos.eff.Eff
import org.atnos.eff.|=
import excelreads.instance.ValidatedMonadInstance.*
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
    m: State[Int, *] |= R
  ): Eff[R, ValidatedNel[ExcelParseError, Result]]

  final def product[B](reads: ExcelSheetReads[R, B]): ExcelSheetReads[R, (A, B)] =
    new ExcelSheetReads[R, (A, B)] {
      type Result = (self.Result, reads.Result)

      def parse(implicit m: State[Int, *] |= R): Eff[R, ValidatedNel[ExcelParseError, Result]] =
        for {
          aValidation <- self.parse
          result <- Eff.flatTraverseA(aValidation) { a =>
            reads.parse.map(_.map(b => (a, b)))
          }
        } yield result
    }
}

object ExcelSheetReads extends ExcelSheetReadsParse with ExcelSheetReadsParseLoop with ExcelSheetReadsInstances {

  def from[R, A, B](
    f: State[Int, *] |= R => Eff[R, ValidatedNel[ExcelParseError, B]]
  ): ExcelSheetReads[R, A] = new ExcelSheetReads[R, A] {
    type Result = B

    def parse(implicit m: State[Int, *] |= R): Eff[R, ValidatedNel[ExcelParseError, B]] =
      f(m)
  }
}
