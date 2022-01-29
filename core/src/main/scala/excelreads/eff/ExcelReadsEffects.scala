package excelreads.eff

import cats.data.Reader
import cats.data.State
import excelreads.exception.ExcelParseError.ExcelParseErrors
import org.atnos.eff.|=

object ExcelReadsEffects extends ExcelReadsEffects

trait ExcelReadsEffects {
  type _state[R] = State[Int, *] |= R
  type _either[R] = Either[ExcelParseErrors, *] |= R
  type _reader[Row, R] = Reader[Row, *] |= R
}
