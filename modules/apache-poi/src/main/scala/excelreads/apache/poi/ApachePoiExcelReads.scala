package excelreads.apache.poi

import excelreads.ExcelReads
import org.apache.poi.ss.usermodel.Row

trait ApachePoiExcelReads[A]
  extends ExcelReads[Row, A]

object ApachePoiExcelReads
  extends ApachePoiExcelReadsInstances {

  def apply[A](implicit
    reads: ApachePoiExcelReads[A]
  ): ExcelReads[Row, A] = reads
}