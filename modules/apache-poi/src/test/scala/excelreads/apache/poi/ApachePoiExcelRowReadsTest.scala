package excelreads.apache.poi

import cats.data.Reader
import cats.data.State
import excelreads.ExcelRowReads
import excelreads.exception.ExcelParseError.ExcelParseErrors
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.atnos.eff.Fx
import org.scalatest.diagrams.Diagrams
import org.scalatest.flatspec.AnyFlatSpec
import java.io.File
import org.atnos.eff.syntax.all.*

class ApachePoiExcelRowReadsTest extends AnyFlatSpec with Diagrams {

  type R = Fx.fx3[Reader[ApachePoiRow, *], State[Int, *], Either[ExcelParseErrors, *]]

  trait SetUp {
    val workbook = new XSSFWorkbook
    val sheet = workbook.createSheet("Sheet1")
    val row = ApachePoiRow(sheet.createRow(0))
    val style: CellStyle = workbook.createCellStyle()
    style.setFillBackgroundColor(IndexedColors.BLUE.index)
    val cell = row.value.createCell(0)

    cell.setCellValue(1.0)
    cell.setCellStyle(style)
  }

  "reads" should "return `Int` from the `Option[Int]` instance" in new SetUp {
    assert(
      ExcelRowReads[R, Int].parse
        .runReader(row)
        .evalState(0)
        .runEither
        .run == Right(1)
    )
  }

  it should "parse the case class consist of `Int`" in new SetUp {
    case class OneInt(
      value: Int
    )
    assert(
      ExcelRowReads[R, OneInt].parse
        .runReader(row)
        .evalState(0)
        .runEither
        .run == Right(OneInt(1))
    )
  }

  it should "get the cell style" in new SetUp {
    case class IntAndStyle(
      style: CellStyle,
      value: Int
    )
    assert(
      ExcelRowReads[R, IntAndStyle].parse
        .runReader(row)
        .evalState(0)
        .runEither
        .run == Right(IntAndStyle(style, 1))
    )
  }

  it should "get the empty style from the empty cell" in new SetUp {
    val row2 = ApachePoiRow(sheet.createRow(1))

    assert(
      ExcelRowReads[R, Option[CellStyle]].parse
        .runReader(row2)
        .evalState(0)
        .runEither
        .run == Right(None)
    )
  }

  trait RealExcelSetUp {
    val workbook = WorkbookFactory.create(
      new File(getClass.getResource("/test.xlsx").getFile)
    )
    val sheet = workbook.getSheet("Sheet1")
    val rows = List(
      ApachePoiRow(sheet.getRow(0)),
      ApachePoiRow(sheet.getRow(1))
    )
  }

  it should "parse the case class successfully from loading a real Excel file" in new RealExcelSetUp {
    val expected = List(
      RealExcelDataModel("Hello", Some("Excel"), 1.0, Nil),
      RealExcelDataModel("Goodbye", None, -10.0, List("b1", "b2", "b3"))
    )
    (rows zip expected).foreach { case (row, expected) =>
      assert(
        ExcelRowReads[R, RealExcelDataModel].parse
          .runReader(row)
          .evalState(0)
          .runEither
          .run == Right(expected)
      )
    }
  }

  it should "fail if the type is not match for the rows" in new RealExcelSetUp {
    case class Dummy(
      a1: Boolean,
      a2: Boolean,
      a3: Boolean
    )

    val actual = ExcelRowReads[R, Dummy].parse
      .runReader(rows.head)
      .evalState(0)
      .runEither
      .run

    assert(actual.isLeft)
  }

  it should "fail if the start is void row" in new RealExcelSetUp {
    val voidRow = ApachePoiRow(sheet.getRow(2))

    val actual = ExcelRowReads[R, RealExcelDataModel].parse
      .runReader(voidRow)
      .evalState(0)
      .runEither
      .run

    assert(actual.isLeft)
  }
}
