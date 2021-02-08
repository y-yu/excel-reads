package excelreads.apache.poi

import cats.data.Reader
import cats.data.State
import cats.data.Validated.Valid
import excelreads.ExcelReads
import excelreads.util.TestUtils
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.atnos.eff.Fx
import org.scalatest.diagrams.Diagrams
import org.scalatest.flatspec.AnyFlatSpec
import java.io.File
import org.atnos.eff.syntax.all._

class ApachePoiExcelReadsTest
  extends AnyFlatSpec
    with Diagrams
    with TestUtils {

  type R = Fx.fx2[Reader[ApachePoiRow, *], State[Int, *]]

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
      ExcelReads[R, Int]
        .parse
        .runReader(row)
        .evalState(0)
        .run == Valid(1))
  }

  it should "parse the case class consist of `Int`" in new SetUp {
    case class OneInt(
      value: Int
    )
    assert(ExcelReads[R, OneInt]
      .parse
      .runReader(row)
      .evalState(0)
      .run == Valid(OneInt(1)))
  }

  it should "get the cell style" in new SetUp {
    case class IntAndStyle(
      style: CellStyle,
      value: Int
    )
    assert(ExcelReads[R, IntAndStyle]
      .parse
      .runReader(row)
      .evalState(0)
      .run == Valid(IntAndStyle(style, 1))
    )
  }

  it should "get the empty style from the empty cell" in new SetUp {
    val row2 = ApachePoiRow(sheet.createRow(1))

    assert(
      ExcelReads[R, Option[CellStyle]]
        .parse
        .runReader(row2)
        .evalState(0)
        .run == Valid(None)
    )
  }

  trait RealExcelSetUp {
    case class RealExcelDataModel(
      a1: String,
      a2: Option[String],
      a3: Double,
      a4: List[String]
    )

    val workbook = WorkbookFactory.create(
      new File(getClass.getResource("/test.xlsx").getFile)
    )
    val sheet = workbook.getSheet("Sheet1")
    val rows = List(
      ApachePoiRow(sheet.getRow(0)),
      ApachePoiRow(sheet.getRow(1)),
    )
  }

  it should "parse the case class successfully from loading a real Excel file" in new RealExcelSetUp {
    val expected = List(
      RealExcelDataModel("Hello", Some("Excel"), 1.0, Nil),
      RealExcelDataModel("Goodbye", None, -10.0, List("b1", "b2", "b3"))
    )
    (rows zip expected).foreach {
      case (row, expected) =>
        assert(ExcelReads[R, RealExcelDataModel]
          .parse
          .runReader(row)
          .evalState(0)
          .run == Valid(expected)
        )
    }
  }
}
