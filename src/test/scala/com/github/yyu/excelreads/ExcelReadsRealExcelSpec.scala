package com.github.yyu.excelreads

import com.github.yyu.excelreads.entity.RowWithSheetName
import info.folone.scala.poi.{Row, Workbook}
import org.scalatest.diagrams.Diagrams
import org.scalatest.flatspec.AnyFlatSpec
import scalaz.{-\/, Validation, \/-}

class ExcelReadsRealExcelSpec
  extends AnyFlatSpec
    with Diagrams {

  trait SetUp {
    val fileName = getClass.getResource("/test.xlsx").getPath

    val (sheet1Rows, sheet2Rows): (List[Row], List[Row]) =
      Workbook
        .apply(fileName)
        .map { workbook =>
          (for {
            sheet1 <- workbook.sheets.find(_.name == "Sheet1")
            sheet2 <- workbook.sheets.find(_.name == "Sheet2")
          } yield (
            sheet1.rows.toList.sortBy(_.index),
            sheet2.rows.toList.sortBy(_.index)
          )).get
        }
        .run
        .unsafePerformIO() match {
        case -\/(e) =>
          println(e)
          throw e
        case \/-(v) =>
          v
      }
  }

  "ExcelReads" should "load a real Excel file" in new SetUp {
    case class RealExcelDataModel(
      a1: String,
      a2: Option[String],
      a3: Double,
      a4: Seq[String]
    )

    val rowWithSheetName = sheet1Rows.map { row =>
      RowWithSheetName(
        "Sheet1",
        row
      )
    }
    val expected1 = List(
      RealExcelDataModel("Hello", Some("Excel"), 1.0, Nil),
      RealExcelDataModel("Goodbye", None, -10.0, Seq("b1", "b2", "b3"))
    )

    (rowWithSheetName zip expected1).foreach {
      case (row, expected) =>
        assert(ExcelReads[RealExcelDataModel].read(row)
          == Validation.success(expected)
        )
    }

  }
}
