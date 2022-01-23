package excelreads

import cats.data.State
import cats.data.ValidatedNel
import excelreads.exception.ExcelParseError
import excelreads.instance.ExcelReadsInstances
import org.atnos.eff.Eff
import org.atnos.eff.|=

/** Excel cells parser type class
  *
  * @tparam R
  *   effects stack for Eff
  * @tparam A
  *   return type
  */
abstract class ExcelReads[R, A] {
  def parse(implicit
    m: State[Int, *] |= R
  ): Eff[R, ValidatedNel[ExcelParseError, A]]
}

object ExcelReads extends ExcelReadsInstances {

  def apply[R, A](implicit
    reads: ExcelReads[R, A]
  ): ExcelReads[R, A] = reads

  def from[R, A](
    f: State[Int, *] |= R => Eff[R, ValidatedNel[ExcelParseError, A]]
  ): ExcelReads[R, A] = new ExcelReads[R, A] {
    def parse(implicit m: State[Int, *] |= R): Eff[R, ValidatedNel[ExcelParseError, A]] =
      f(m)
  }
}
