package excelreads.scala.poi

import excelreads.ExcelReads
import info.folone.scala.poi.Row

trait PoiScalaExcelReads[A]
  extends ExcelReads[Row, A]

object PoiScalaExcelReads
  extends PoiScalaExcelReadsInstances {

  def apply[A](implicit
    reads: PoiScalaExcelReads[A]
  ): ExcelReads[Row, A] = reads
}