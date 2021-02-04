package excelreads.apache.poi.sym

import cats.data.Reader
import cats.data.Validated
import cats.data.ValidatedNel
import excelreads.exception.ExcelParseError
import excelreads.sym.ExcelStyleSYM
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Row
import org.atnos.eff.Eff
import org.atnos.eff.|=
import org.atnos.eff.reader._

object ApachePoiExcelStyleSYM {
  implicit def apachePoiExcelStyleSYMInstance[R](
    implicit m: Reader[Row, *] |= R
  ): ApachePoiExcelStyleSYM[R] =
    new ApachePoiExcelStyleSYM[R]
}

class ApachePoiExcelStyleSYM[R](
  implicit m: Reader[Row, *] |= R
) extends ExcelStyleSYM[CellStyle, Eff[R, *]] {

  private def successNel[A](a: A): ValidatedNel[ExcelParseError, A] =
    Validated.Valid(a)

  def getStyle(index: Int): Eff[R, ValidatedNel[ExcelParseError, Option[CellStyle]]] =
    for {
      row <- ask
    } yield successNel(Option(row.getCell(index)).map(_.getCellStyle))
}
