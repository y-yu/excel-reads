package excelreads.scala.poi

import excelreads.scala.poi.sym.PoiScalaExcelBasicSYM
import excelreads.sym.ExcelBasicSYM
import info.folone.scala.poi.Row
import org.atnos.eff.Eff
import excelreads.eff.ExcelReadsEffects.*

/** Wrapper class for Poi Scala `Row`
  *
  * @note
  *   This case class will be stacked into the `Eff` effects stack(aka `R`). Then Scala compiler will finds implicit
  *   instance where the look companion objects of all types in effects stack. So this wrapper is needed to wiring
  *   implicit instances.
  */
case class PoiScalaRow(
  value: Row
) extends AnyVal

object PoiScalaRow {
  implicit def poiScalaSymInstances[
    R: _reader[PoiScalaRow, *]: _state: _either
  ]: ExcelBasicSYM[Eff[R, *]] =
    new PoiScalaExcelBasicSYM[R]
}
