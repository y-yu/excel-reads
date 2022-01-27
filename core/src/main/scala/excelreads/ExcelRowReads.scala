package excelreads

import cats.data.State
import excelreads.exception.ExcelParseError.ExcelParseErrors
import excelreads.instance.ExcelRowReadsInstances
import org.atnos.eff.Eff
import org.atnos.eff./=
import org.atnos.eff.|=

/** Excel row parser type class
  *
  * @tparam R
  *   effects stack for Eff
  * @tparam A
  *   return type
  */
abstract class ExcelRowReads[R, A] {
  def parse(implicit
    m1: State[Int, *] |= R,
    m2: Either[ExcelParseErrors, *] |= R
  ): Eff[R, A]
}

object ExcelRowReads extends ExcelRowReadsInstances {
  def apply[R, A](implicit
    reads: ExcelRowReads[R, A]
  ): ExcelRowReads[R, A] = reads

  def from[R, A](
    f: State[Int, *] |= R => Either[ExcelParseErrors, *] |= R => Eff[R, A]
  ): ExcelRowReads[R, A] = new ExcelRowReads[R, A] {
    def parse(implicit
      m1: State[Int, *] |= R,
      m2: Either[ExcelParseErrors, *] |= R
    ): Eff[R, A] =
      f(m1)(m2)
  }
}
