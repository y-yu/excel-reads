package excelreads.scala.poi.sym

import cats.data.Reader
import cats.data.Validated.Valid
import excelreads.scala.poi.PoiScalaRow
import excelreads.util.TestUtils
import info.folone.scala.poi.NumericCell
import info.folone.scala.poi.Row
import info.folone.scala.poi.StringCell
import org.atnos.eff.Fx
import org.scalatest.diagrams.Diagrams
import org.scalatest.flatspec.AnyFlatSpec

class PoiScalaExcelBasicSYMTest
  extends AnyFlatSpec
    with Diagrams
    with TestUtils {

  val sut = new PoiScalaExcelBasicSYM[
    Fx.fx1[Reader[PoiScalaRow, *]]
  ]

  "getString" should "get `String` from the poi scala row" in {
    val row = PoiScalaRow(Row(0) {
      Set(
        StringCell(0, "hello"),
        StringCell(1, "excel")
      )
    })
    assert(runReader(sut.getString(0), row) == Valid(Some("hello")))
    assert(runReader(sut.getString(1), row) == Valid(Some("excel")))
  }

  it should "return `None` if the cell is empty" in {
    val row = PoiScalaRow(Row(0) {
      Set(
        StringCell(0, "hello")
      )
    })
    assert(runReader(sut.getString(1), row) == Valid(None))
  }

  it should "return `Invalid` if the cell is not `String`" in {
    val row = PoiScalaRow(Row(0) {
      Set(
        NumericCell(0, 1.0)
      )
    })
    val actual = runReader(sut.getString(0), row)
    assert(actual.isInvalid)
  }
}
