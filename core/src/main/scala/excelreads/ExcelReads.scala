package excelreads

import cats.data.State
import cats.data.ValidatedNel
import excelreads.exception.ExcelParseError

/**
  * Excel parser type class
  *
  * @tparam Row Excel row type
  * @tparam A return type
  */
trait ExcelReads[Row, A] { self =>
  protected[excelreads] def parse(row: Row): State[Int, ValidatedNel[ExcelParseError, A]]

  final def map[B](
    f: A => B
  ): ExcelReads[Row, B] = { row =>
    self.parse(row).map(_.map(f))
  }

  final def eval(
    row: Row,
    init: Int = 0
  ): ValidatedNel[ExcelParseError, A] =
    parse(row).run(init).value._2
}
