package excelreads.scala.poi.sym

import cats.data.Reader
import cats.data.State
import excelreads.exception.ExcelParseError.ExcelParseErrors
import excelreads.scala.poi.PoiScalaRow
import info.folone.scala.poi.NumericCell
import info.folone.scala.poi.Row
import info.folone.scala.poi.StringCell
import org.atnos.eff.Fx
import org.scalatest.diagrams.Diagrams
import org.scalatest.flatspec.AnyFlatSpec
import org.atnos.eff.syntax.all.*

class PoiScalaExcelBasicSYMTest extends AnyFlatSpec with Diagrams {

  val sut = new PoiScalaExcelBasicSYM[
    Fx.fx3[Reader[PoiScalaRow, *], State[Int, *], Either[ExcelParseErrors, *]]
  ]

  "getString" should "get `String` from the poi scala row" in {
    val row = PoiScalaRow(Row(0) {
      Set(
        StringCell(0, "hello"),
        StringCell(1, "excel")
      )
    })
    assert(
      sut.getString
        .evalState(0)
        .runReader(row)
        .runEither
        .run == Right(Some("hello"))
    )
    assert(
      sut.getString
        .evalState(1)
        .runReader(row)
        .runEither
        .run == Right(Some("excel"))
    )
  }

  it should "return `None` if the cell is empty" in {
    val row = PoiScalaRow(Row(0) {
      Set(
        StringCell(0, "hello")
      )
    })
    assert(
      sut.getString
        .evalState(1)
        .runReader(row)
        .runEither
        .run == Right(None)
    )
  }

  it should "return `Invalid` if the cell is not `String`" in {
    val row = PoiScalaRow(Row(0) {
      Set(
        NumericCell(0, 1.0)
      )
    })
    val actual = sut.getString
      .evalState(0)
      .runReader(row)
      .runEither
      .run

    assert(actual.isLeft)
  }
}
