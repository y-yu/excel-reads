package com.github.yyu.excelreads

import java.text.SimpleDateFormat
import java.util.Date
import com.github.yyu.excelreads.entity.RowWithSheetName
import com.github.yyu.excelreads.exception.ExcelRowParseError.{UnexpectedEmptyCell, UnexpectedTypeCell}
import info.folone.scala.poi._
import org.scalatest._
import scalaz.{Failure, Success}

class ExcelReadsSpec
  extends FlatSpec
  with DiagrammedAssertions {

  "ExcelReads" should "parse a `case class` which contains `String`" in {
    case class HelloWorld(
      hello: String,
      world: String
    )

    val rowWithSheetName = RowWithSheetName(
      "sheet",
      Row(1) {
        Set(StringCell(1, "hello"), StringCell(2, "world"))
      }
    )

    val actual = ExcelReads[HelloWorld].read(rowWithSheetName)

    assert(actual == Success(HelloWorld("hello", "world")))
  }

  it should "parse what contains `Option[String]`" in {
    case class HelloOptionWorld(
      hello: Option[String],
      world: String
    )

    val rowWithSheetName1 = RowWithSheetName(
      "sheet",
      Row(1) {
        Set(StringCell(1, "hello"), StringCell(2, "world"))
      }
    )
    val rowWithSheetName2 = RowWithSheetName(
      "sheet",
      Row(1) {
        Set(StringCell(2, "world"))
      }
    )

    val actual1 = ExcelReads[HelloOptionWorld].read(rowWithSheetName1)
    val actual2 = ExcelReads[HelloOptionWorld].read(rowWithSheetName2)

    assert(actual1 == Success(HelloOptionWorld(Some("hello"), "world")))
    assert(actual2 == Success(HelloOptionWorld(None, "world")))
  }

  it should "parse what contains `Int`" in {
    case class Numerical(
      value1: Int,
      value2: Int
    )

    val rowWithSheetName = RowWithSheetName(
      "sheet",
      Row(1) {
        Set(NumericCell(1, 100.5), NumericCell(2, Int.MaxValue.toDouble + 1))
      }
    )

    val actual = ExcelReads[Numerical].read(rowWithSheetName)
    assert(actual == Success(Numerical(100, Int.MaxValue)))
  }

  it should "parse what contains `Double`" in {
    case class Numerical(
      value1: Double,
      value2: Double
    )

    val rowWithSheetName = RowWithSheetName(
      "sheet",
      Row(1) {
        Set(NumericCell(1, 100.5), NumericCell(2, Int.MaxValue.toDouble + 1))
      }
    )

    val actual = ExcelReads[Numerical].read(rowWithSheetName)
    assert(actual == Success(Numerical(100.5, Int.MaxValue.toDouble + 1)))
  }

  it should "parse what contains `Date`" in {
    case class Day(
      date: Date
    )
    val format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
    val day = format.parse("2019/09/08 01:23:45")


    val rowWithSheetName = RowWithSheetName(
      "sheet",
      Row(1) {
        Set(DateCell(1, day))
      }
    )

    val actual = ExcelReads[Day].read(rowWithSheetName)
    assert(actual == Success(Day(day)))
  }

  it should "parse what contains `Boolean`" in {
    case class Bool(
      bool: Boolean
    )

    val rowWithSheetName = RowWithSheetName(
      "sheet",
      Row(1) {
        Set(BooleanCell(1, data = true))
      }
    )

    val actual = ExcelReads[Bool].read(rowWithSheetName)
    assert(actual == Success(Bool(bool = true)))
  }

  it should "parse what contains `Seq[String]`" in {
    case class RowData(
      bool: Boolean,
      number: Int,
      strings: Seq[String]
    )

    val rowWithSheetName = RowWithSheetName(
      "sheet",
      Row(1) {
        Set(
          BooleanCell(1, data = true),
          NumericCell(2, 10),
          StringCell(3, "foo"),
          StringCell(4, "bar"),
        )
      }
    )

    val actual = ExcelReads[RowData].read(rowWithSheetName)
    assert(actual == Success(RowData(bool = true, 10, Seq("foo", "bar"))))
  }

  it should "throw an error if the the cell type isn't match type of a case class field" in {
    case class RowData(
      bool: Boolean
    )

    val rowWithSheetName = RowWithSheetName(
      "sheet",
      Row(1) {
        Set(StringCell(1, "true"))
      }
    )

    val actual = ExcelReads[RowData].read(rowWithSheetName)
    assert(actual match {
      case Failure(e) => e.head.isInstanceOf[UnexpectedTypeCell]
      case _ => false
    })
  }

  it should "throw an error if the the cell is empty" in {
    case class RowData(
      bool: Boolean
    )

    val rowWithSheetName = RowWithSheetName(
      "sheet",
      Row(1) {
        Set.empty
      }
    )

    val actual = ExcelReads[RowData].read(rowWithSheetName)
    assert(actual match {
      case Failure(e) => e.head.isInstanceOf[UnexpectedEmptyCell]
      case _ => false
    })
  }

  it should "throw errors if the case class contains `Seq[String]` at the middle" in {
    case class RowData(
      bool: Boolean,
      strings: Seq[String],
      number: Int
    )

    val rowWithSheetName = RowWithSheetName(
      "sheet",
      Row(1) {
        Set(
          BooleanCell(1, data = true),
          StringCell(2, "foo"),
          StringCell(3, "bar"),
          NumericCell(4, 10)
        )
      }
    )

    val actual = ExcelReads[RowData].read(rowWithSheetName)
    assert(actual.isFailure)
  }
}
