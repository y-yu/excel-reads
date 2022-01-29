package excelreads.apache.poi

import cats.data.Reader
import excelreads.apache.poi.sym.ApachePoiExcelRowSYM
import excelreads.apache.poi.sym.ApachePoiExcelRowSYM.ApachePoiExcelReadsStack
import excelreads.eff.ExcelReadsEffects.*
import excelreads.sym.ExcelRowSYM
import org.apache.poi.ss.usermodel.Sheet
import org.atnos.eff.Eff
import org.atnos.eff.|=

/** Wrapper class for Apache POI `Sheet`
  */
case class ApachePoiSheet(
  value: Sheet
)

object ApachePoiSheet extends ApachePoiRowInstances {
  implicit def apachePoiRowSymInstances[
    R: _reader[ApachePoiSheet, *]: _state: _either
  ]: ExcelRowSYM[ApachePoiRow, ApachePoiExcelReadsStack, Eff[R, *]] =
    new ApachePoiExcelRowSYM[R]
}
