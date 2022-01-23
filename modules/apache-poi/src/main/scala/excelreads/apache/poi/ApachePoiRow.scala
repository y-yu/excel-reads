package excelreads.apache.poi

import cats.data.Reader
import excelreads.apache.poi.sym.ApachePoiExcelBasicSYM
import excelreads.apache.poi.sym.ApachePoiExcelStyleSYM
import excelreads.sym.ExcelBasicSYM
import excelreads.sym.ExcelStyleSYM
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Row
import org.atnos.eff.Eff
import org.atnos.eff.|=

/** Wrapper class for Apache POI `Row`
  *
  * @note
  *   This case class will be stacked into the `Eff` effects stack(aka `R`). Then Scala compiler will finds implicit
  *   instance where the look companion objects of all types in effects stack. So this wrapper is needed to wiring
  *   implicit instances.
  */
case class ApachePoiRow(
  value: Row
) extends AnyVal

object ApachePoiRow extends ApachePoiRowInstances

trait ApachePoiRowInstances {
  implicit def apachePoiBasicSymInstances[R](implicit
    m: Reader[ApachePoiRow, *] |= R
  ): ExcelBasicSYM[Eff[R, *]] =
    new ApachePoiExcelBasicSYM[R]

  implicit def apachePoiStyleSymInstances[R](implicit
    m: Reader[ApachePoiRow, *] |= R
  ): ExcelStyleSYM[CellStyle, Eff[R, *]] =
    new ApachePoiExcelStyleSYM[R]
}
