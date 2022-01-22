package excelreads.apache.poi

import cats.data.Reader
import cats.data.State
import cats.data.Validated.Valid
import excelreads.row.ExcelRowQuantifier.*
import excelreads.ExcelRowReads
import excelreads.util.TestUtils
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.atnos.eff.Fx
import org.scalatest.diagrams.Diagrams
import org.scalatest.flatspec.AnyFlatSpec
import org.atnos.eff.syntax.all.*

class ApachePoiExcelRowReadsTest extends AnyFlatSpec with Diagrams with TestUtils {

  type R = Fx.fx2[Reader[ApachePoiSheet, *], State[Int, *]]

  trait SetUp {
    val workbook = new XSSFWorkbook
    val sheet = workbook.createSheet("Sheet1")
    val style: CellStyle = workbook.createCellStyle()
    style.setFillBackgroundColor(IndexedColors.BLUE.index)

    val row1 = sheet.createRow(0)
    val row2 = sheet.createRow(1)
    val cell1 = row1.createCell(0)
    cell1.setCellValue(2.0)
    val cell2 = row2.createCell(0)
    cell2.setCellValue("dummy")
  }

  "reads" should "return rows, for each has a `Int` and `String` cell" in new SetUp {
    val actual = ExcelRowReads
      .parse[R, Int, String]
      .runReader(ApachePoiSheet(sheet))
      .evalState(0)
      .run

    assert(actual == Valid((2, "dummy")))
  }

  it should "return 100 `Int` rows" in new SetUp {
    (0 until 100) foreach { i =>
      val row = sheet.createRow(i)
      val cell = row.createCell(0)
      cell.setCellValue(i.toDouble)
    }

    val actual = ExcelRowReads
      .parse[R, Many[Int]]
      .runReader(ApachePoiSheet(sheet))
      .evalState(0)
      .run

    assert(actual == Valid(0 until 100))
  }

  it should "return `Seq[Int]` and `String` by `[Many[Int], String]`" in new SetUp {
    val actual = ExcelRowReads
      .parse[R, Many[Int], String]
      .runReader(ApachePoiSheet(sheet))
      .evalState(0)
      .run

    assert(actual == Valid((Seq(2), "dummy")))
  }

  it should "return `Seq[Int]` and `Seq[String]` by `[Many[Int], Many[String]]`" in new SetUp {
    val actual = ExcelRowReads
      .parse[R, Many[Int], Many[String]]
      .runReader(ApachePoiSheet(sheet))
      .evalState(0)
      .run

    assert(actual == Valid((Seq(2), Seq("dummy"))))
  }

  it should "return `Int` and `Some[String]` by `[Int, Optional[String]]`" in new SetUp {
    val actual = ExcelRowReads
      .parse[R, Int, Optional[String]]
      .runReader(ApachePoiSheet(sheet))
      .evalState(0)
      .run

    assert(actual == Valid((2, Some("dummy"))))
  }

  it should "return `None`, `Int` and `Some[String]` by `[Optional[Boolean], Int, Optional[String]]`" in new SetUp {
    val actual = ExcelRowReads
      .parse[R, Optional[Boolean], Int, Optional[String]]
      .runReader(ApachePoiSheet(sheet))
      .evalState(0)
      .run

    assert(actual == Valid((None, 2, Some("dummy"))))
  }

  it should "return `Unit`, String` by `[Skip, String]`" in new SetUp {
    val actual = ExcelRowReads
      .parse[R, Skip, String]
      .runReader(ApachePoiSheet(sheet))
      .evalState(0)
      .run

    assert(actual == Valid(((), "dummy")))
  }

  it should "return `Int`, `Unit`, the number of skip lines and `Boolean` by `[Int, Skip, SkipOnlyEmpties, Boolean]`" in new SetUp {
    val row = sheet.createRow(10)
    val cell = row.createCell(0)
    cell.setCellValue(true)

    val actual = ExcelRowReads
      .parse[R, Int, Skip, SkipOnlyEmpties, Boolean]
      .runReader(ApachePoiSheet(sheet))
      .evalState(0)
      .run

    assert(actual == Valid((2, (), 8, true)))
  }
}
