package excelreads.apache.poi.sym

import excelreads.apache.poi.ApachePoiRow
import excelreads.eff.ExcelReadsEffects._either
import excelreads.eff.ExcelReadsEffects._reader
import excelreads.eff.ExcelReadsEffects._state
import excelreads.sym.ExcelStyleSYM
import org.apache.poi.ss.usermodel.CellStyle
import org.atnos.eff.Eff
import org.atnos.eff.state.*
import org.atnos.eff.reader.*

/** Apache POI style sym implementation
  *
  * @tparam R
  *   effects stack which contains `Reader[Row, *]`
  */
class ApachePoiExcelStyleSYM[R: _reader[ApachePoiRow, *]: _state: _either] extends ExcelStyleSYM[CellStyle, Eff[R, *]] {

  def getStyle: Eff[R, Option[CellStyle]] =
    for {
      index <- get
      row <- ask
    } yield Option(
      row.value.getCell(index)
    ).map(_.getCellStyle)
}
