package excelreads

import cats.data.State
import cats.data.ValidatedNel
import excelreads.exception.ExcelParseError
import excelreads.instance.ExcelRowReadsInstances
import org.atnos.eff.Eff
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
    m: State[Int, *] |= R
  ): Eff[R, ValidatedNel[ExcelParseError, A]]
}

object ExcelRowReads extends ExcelRowReadsInstances {

  def apply[R, A](implicit
    reads: ExcelRowReads[R, A]
  ): ExcelRowReads[R, A] = reads

  def from[R, A](
    f: State[Int, *] |= R => Eff[R, ValidatedNel[ExcelParseError, A]]
  ): ExcelRowReads[R, A] = new ExcelRowReads[R, A] {
    def parse(implicit m: State[Int, *] |= R): Eff[R, ValidatedNel[ExcelParseError, A]] =
      f(m)
  }
}
