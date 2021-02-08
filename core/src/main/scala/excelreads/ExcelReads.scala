package excelreads

import cats.data.State
import cats.data.ValidatedNel
import excelreads.exception.ExcelParseError
import org.atnos.eff.Eff
import org.atnos.eff.|=

/**
  * Excel parser type class
  *
  * @tparam R effects stack for [[Eff]]
  * @tparam A return type
  */
trait ExcelReads[R, A] {
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
  ): ExcelReads[R, A] = (m: State[Int, *] |= R) => f(m)
}
