package excelreads.exception

import cats.data.NonEmptyList
import excelreads.exception.ExcelParseError.ExcelParseErrors

trait ExcelParseErrorCreation {
  def from(e: ExcelParseError): ExcelParseErrors =
    NonEmptyList(e, Nil)
}
