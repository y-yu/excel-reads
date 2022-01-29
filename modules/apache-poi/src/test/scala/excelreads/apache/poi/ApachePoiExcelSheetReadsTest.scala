package excelreads.apache.poi

import cats.data.Reader
import cats.data.State
import excelreads.ExcelRowQuantifier.*
import excelreads.ExcelSheetReads
import excelreads.exception.ExcelParseError.ExcelParseErrors
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.atnos.eff.Fx
import org.scalatest.diagrams.Diagrams
import org.scalatest.flatspec.AnyFlatSpec
import org.atnos.eff.syntax.all.*
import java.io.File

class ApachePoiExcelSheetReadsTest extends AnyFlatSpec with Diagrams {
  type R = Fx.fx3[Reader[ApachePoiSheet, *], State[Int, *], Either[ExcelParseErrors, *]]

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

  "parse" should "return rows, for each has a `Int` and `String` cell" in new SetUp {
    val actual = ExcelSheetReads
      .parse[R, Int, String]
      .runReader(ApachePoiSheet(sheet))
      .evalState(0)
      .runEither
      .run

    assert(actual == Right((2, "dummy")))
  }

  it should "return 100 `Int` rows" in new SetUp {
    (0 until 100) foreach { i =>
      val row = sheet.createRow(i)
      val cell = row.createCell(0)
      cell.setCellValue(i.toDouble)
    }

    val actual = ExcelSheetReads
      .parse[R, Many[Int]]
      .runReader(ApachePoiSheet(sheet))
      .evalState(0)
      .runEither
      .run

    assert(actual == Right(0 until 100))
  }

  it should "return `Seq[Int]` and `String` by `[Many[Int], String]`" in new SetUp {
    val actual = ExcelSheetReads
      .parse[R, Many[Int], String]
      .runReader(ApachePoiSheet(sheet))
      .evalState(0)
      .runEither
      .run

    assert(actual == Right((Seq(2), "dummy")))
  }

  it should "return `Seq[Int]` and `Seq[String]` by `[Many[Int], Many[String]]`" in new SetUp {
    val actual = ExcelSheetReads
      .parse[R, Many[Int], Many[String]]
      .runReader(ApachePoiSheet(sheet))
      .evalState(0)
      .runEither
      .run

    assert(actual == Right((Seq(2), Seq("dummy"))))
  }

  it should "return `Int` and `Some[String]` by `[Int, Optional[String]]`" in new SetUp {
    val actual = ExcelSheetReads
      .parse[R, Int, Optional[String]]
      .runReader(ApachePoiSheet(sheet))
      .evalState(0)
      .runEither
      .run

    assert(actual == Right((2, Some("dummy"))))
  }

  it should "return `None`, `Int` and `Some[String]` by `[Optional[Boolean], Int, Optional[String]]`" in new SetUp {
    val actual = ExcelSheetReads
      .parse[R, Optional[Boolean], Int, Optional[String]]
      .runReader(ApachePoiSheet(sheet))
      .evalState(0)
      .runEither
      .run

    assert(actual == Right((None, 2, Some("dummy"))))
  }

  it should "return `Unit`, String` by `[Skip, String]`" in new SetUp {
    val actual = ExcelSheetReads
      .parse[R, Skip, String]
      .runReader(ApachePoiSheet(sheet))
      .evalState(0)
      .runEither
      .run

    assert(actual == Right(((), "dummy")))
  }

  it should "return `Int`, `Unit`, the number of skip lines and `Boolean` by `[Int, Skip, SkipOnlyEmpties, Boolean]`" in new SetUp {
    val row = sheet.createRow(10)
    val cell = row.createCell(0)
    cell.setCellValue(true)

    val actual = ExcelSheetReads
      .parse[R, Int, Skip, SkipOnlyEmpties, Boolean]
      .runReader(ApachePoiSheet(sheet))
      .evalState(0)
      .runEither
      .run

    assert(actual == Right((2, (), 8, true)))
  }

  trait RealExcelSetUp {
    val workbook = WorkbookFactory.create(
      new File(getClass.getResource("/test.xlsx").getFile)
    )
    val sheet = workbook.getSheet("Sheet1")
  }

  it should "return case classes successfully from loading a real Excel file" in new RealExcelSetUp {
    val actual = ExcelSheetReads
      .parse[R, Many[RealExcelDataModel]]
      .runReader(ApachePoiSheet(sheet))
      .evalState(0)
      .runEither
      .run

    val expected = Right(
      List(
        RealExcelDataModel("Hello", Some("Excel"), 1.0, Nil),
        RealExcelDataModel("Goodbye", None, -10.0, List("b1", "b2", "b3"))
      )
    )
    assert(actual == expected)
  }

  it should "stop if `SkipOnlyEmpties` is put on the end" in new RealExcelSetUp {
    val actual = ExcelSheetReads
      .parse[R, Many[RealExcelDataModel], SkipOnlyEmpties]
      .runReader(ApachePoiSheet(sheet))
      .evalState(0)
      .runEither
      .run

    val expected = Right(
      (
        Seq(
          RealExcelDataModel("Hello", Some("Excel"), 1.0, Nil),
          RealExcelDataModel("Goodbye", None, -10.0, List("b1", "b2", "b3"))
        ),
        0 // Not define any rows after data
      )
    )
    assert(actual == expected)
  }

  case class Header(
    a1: String,
    a2: String,
    a3: Int,
    a4: Boolean
  )

  abstract class RealExcelSetUp2(sheetName: String = "Sheet1") {
    val workbook = WorkbookFactory.create(
      new File(getClass.getResource("/test2.xlsx").getFile)
    )
    val sheet = workbook.getSheet(sheetName)
  }

  it should "parse successfully" in new RealExcelSetUp2 {
    val actual = ExcelSheetReads
      .parse[R, Header, List[Int], Optional[Boolean], SkipOnlyEmpties, (String, String)]
      .runReader(ApachePoiSheet(sheet))
      .evalState(0)
      .runEither
      .run

    val expected = Right(
      (
        Header("Hello", "Excel", 1, true),
        List(1, 2, 3, 4, 5),
        Some(true),
        1,
        ("Good", "Bye!")
      )
    )
    assert(actual == expected)
  }

  "loop" should "parse data repeatedly" in new RealExcelSetUp {
    val actual = ExcelSheetReads
      .loop[R, RealExcelDataModel]
      .runReader(ApachePoiSheet(sheet))
      .evalState(0)
      .runEither
      .run

    val expected = Right(
      List(
        RealExcelDataModel("Hello", Some("Excel"), 1.0, Nil),
        RealExcelDataModel("Goodbye", None, -10.0, List("b1", "b2", "b3"))
      )
    )
    assert(actual == expected)
  }

  it should "parse some data repeatedly" in new RealExcelSetUp {
    val actual = ExcelSheetReads
      .loop[R, RealExcelDataModel, RealExcelDataModel]
      .runReader(ApachePoiSheet(sheet))
      .evalState(0)
      .runEither
      .run

    val expected = Right(
      List(
        (
          RealExcelDataModel("Hello", Some("Excel"), 1.0, Nil),
          RealExcelDataModel("Goodbye", None, -10.0, List("b1", "b2", "b3"))
        )
      )
    )
    assert(actual == expected)
  }

  it should "parse two data repeatedly" in new RealExcelSetUp2("Sheet2") {
    val actual = ExcelSheetReads
      .loop[R, List[String], List[Int]]
      .runReader(ApachePoiSheet(sheet))
      .evalState(0)
      .runEither
      .run

    val expected = Right(
      List(
        (List("Hello", "Excel"), List(1, 2, 3)),
        (List("This", "is", "a", "pen."), List(9, 8, 7, 6))
      )
    )
    assert(actual == expected)
  }
}
