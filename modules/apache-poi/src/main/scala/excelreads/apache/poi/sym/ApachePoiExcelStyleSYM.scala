package excelreads.apache.poi.sym

import cats.data.Reader
import cats.data.Validated
import cats.data.ValidatedNel
import excelreads.apache.poi.ApachePoiRow
import excelreads.exception.ExcelParseError
import excelreads.sym.ExcelStyleSYM
import org.apache.poi.ss.usermodel.CellStyle
import org.atnos.eff.Eff
import org.atnos.eff.|=
import org.atnos.eff.reader._

/** Apache POI style sym implementation
  *
  * @tparam R
  *   effects stack which contains `Reader[Row, *]`
  */
class ApachePoiExcelStyleSYM[R](implicit
  m: Reader[ApachePoiRow, *] |= R
) extends ExcelStyleSYM[CellStyle, Eff[R, *]] {

  private def successNel[A](a: A): ValidatedNel[ExcelParseError, A] =
    Validated.Valid(a)

  def getStyle(index: Int): Eff[R, ValidatedNel[ExcelParseError, Option[CellStyle]]] =
    for {
      row <- ask
    } yield successNel(Option(row.value.getCell(index)).map(_.getCellStyle))
}
