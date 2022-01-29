package excelreads.scala.poi

import cats.data.Reader
import cats.data.State
import excelreads.ExcelRowReads
import excelreads.exception.ExcelParseError.ExcelParseErrors
import org.atnos.eff.syntax.all.*
import info.folone.scala.poi.BooleanCell
import info.folone.scala.poi.NumericCell
import info.folone.scala.poi.Row
import info.folone.scala.poi.StringCell
import info.folone.scala.poi.Workbook
import org.atnos.eff.Fx
import org.scalatest.diagrams.Diagrams
import org.scalatest.flatspec.AnyFlatSpec
import scalaz.-\/
import scalaz.\/-

class PoiScalaExcelReadsTest extends AnyFlatSpec with Diagrams {

  type R = Fx.fx3[Reader[PoiScalaRow, *], State[Int, *], Either[ExcelParseErrors, *]]

  "reads" should "return `String` from the `Option[String]` instance" in {
    val row = PoiScalaRow(Row(0) {
      Set(
        StringCell(0, "hello")
      )
    })

    assert(
      ExcelRowReads[R, String].parse
        .runReader(row)
        .evalState(0)
        .runEither
        .run == Right("hello")
    )
  }

  it should "return a case class from the Excel row" in {
    case class HelloExcel(
      hello: String,
      excel: String
    )

    val row = PoiScalaRow(Row(0) {
      Set(
        StringCell(0, "hello"),
        StringCell(1, "excel")
      )
    })

    val actual = ExcelRowReads[R, HelloExcel].parse
      .runReader(row)
      .evalState(0)
      .runEither
      .run
    assert(actual == Right(HelloExcel("hello", "excel")))
  }

  it should "return a case class which has `List[Int]` from the Excel row" in {
    case class Numbers(
      numbers: List[Int]
    )

    val row = PoiScalaRow(Row(0) {
      Set(
        NumericCell(0, 1),
        NumericCell(1, 2),
        NumericCell(2, 3)
      )
    })

    val actual = ExcelRowReads[R, Numbers].parse
      .runReader(row)
      .evalState(0)
      .runEither
      .run
    assert(actual == Right(Numbers(List(1, 2, 3))))
  }

  it should "parse the case class even if it has `Option[Boolean]`" in {
    case class HasOption(
      value: Option[Boolean]
    )

    val row1 = PoiScalaRow(Row(0) {
      Set(
        BooleanCell(0, data = true)
      )
    })
    assert(
      ExcelRowReads[R, HasOption].parse
        .runReader(row1)
        .evalState(0)
        .runEither
        .run == Right(HasOption(Some(true)))
    )

    val row2 = PoiScalaRow(Row(1) { Set.empty })
    assert(
      ExcelRowReads[R, HasOption].parse
        .runReader(row2)
        .evalState(0)
        .runEither
        .run == Right(HasOption(None))
    )
  }

  it should "not compile if the case class has ADTs" in {
    case class HasADT(
      value: ADT
    )
    sealed trait ADT
    case object C1 extends ADT

    assertDoesNotCompile("ExcelReads[R, HasADT]")
  }

  trait SetUp {
    case class RealExcelDataModel(
      a1: String,
      a2: Option[String],
      a3: Double,
      a4: List[String]
    )

    val fileName = getClass.getResource("/test.xlsx").getPath

    val (sheet1Rows, sheet2Rows): (List[PoiScalaRow], List[PoiScalaRow]) =
      Workbook
        .apply(fileName)
        .map { workbook =>
          (for {
            sheet1 <- workbook.sheets.find(_.name == "Sheet1")
            sheet2 <- workbook.sheets.find(_.name == "Sheet2")
          } yield (
            // Rows are `Set` and not ordered.
            sheet1.rows.toList.sortBy(_.index).map(PoiScalaRow.apply),
            sheet2.rows.toList.sortBy(_.index).map(PoiScalaRow.apply)
          )).get
        }
        .run
        .unsafePerformIO() match {
        case -\/(e) =>
          throw e
        case \/-(v) =>
          v
      }
  }

  it should "parse the case class successfully from loading a real Excel file" in new SetUp {
    val expected = List(
      RealExcelDataModel("Hello", Some("Excel"), 1.0, Nil),
      RealExcelDataModel("Goodbye", None, -10.0, List("b1", "b2", "b3"))
    )

    (sheet1Rows zip expected).foreach { case (row, expected) =>
      assert(
        ExcelRowReads[R, RealExcelDataModel].parse
          .runReader(row)
          .evalState(0)
          .runEither
          .run == Right(expected)
      )
    }
  }
}
