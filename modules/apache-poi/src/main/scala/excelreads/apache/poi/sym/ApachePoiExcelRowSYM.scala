package excelreads.apache.poi.sym

import cats.data.NonEmptyList
import cats.data.Reader
import cats.data.State
import cats.data.Validated.Invalid
import cats.data.Validated.Valid
import cats.data.ValidatedNel
import excelreads.ExcelRowReads
import excelreads.apache.poi.ApachePoiRow
import excelreads.apache.poi.ApachePoiSheet
import excelreads.exception.ExcelParseError
import excelreads.sym.ExcelRowSYM
import org.atnos.eff.Eff
import org.atnos.eff.reader.ask
import org.atnos.eff.|=
import scala.util.control.NonFatal
import org.atnos.eff.syntax.all.*
import cats.implicits.*
import excelreads.instance.ValidatedMonadInstance.*
import org.atnos.eff.Fx
import excelreads.apache.poi.sym.ApachePoiExcelRowSYM.ApachePoiExcelReadsStack

class ApachePoiExcelRowSYM[R](implicit
  m: Reader[ApachePoiSheet, *] |= R
) extends ExcelRowSYM[ApachePoiRow, ApachePoiExcelReadsStack, Eff[R, *]] {
  override def isEmpty(index: Int): Eff[R, ValidatedNel[ExcelParseError, Boolean]] =
    for {
      sheet <- ask
    } yield try {
      Valid(Option(sheet.value.getRow(index)).isEmpty)
    } catch {
      case NonFatal(e) =>
        Invalid(
          NonEmptyList(ExcelParseError.UnknownError(index, e.getMessage, e), Nil)
        )
    }

  override def isEnd(index: Int): Eff[R, ValidatedNel[ExcelParseError, Boolean]] =
    for {
      sheet <- ask
    } yield try {
      Valid(sheet.value.getLastRowNum < index)
    } catch {
      case NonFatal(e) =>
        Invalid(
          NonEmptyList(ExcelParseError.UnknownError(index, e.getMessage, e), Nil)
        )
    }

  override def getRow(index: Int): Eff[R, ValidatedNel[ExcelParseError, ApachePoiRow]] =
    for {
      sheet <- ask
    } yield try {
      // FIX ME
      Valid(
        ApachePoiRow(sheet.value.getRow(index))
      )
    } catch {
      case NonFatal(e) =>
        Invalid(
          NonEmptyList(ExcelParseError.UnknownError(index, e.getMessage, e), Nil)
        )
    }

  override def withRow[A](
    index: Int,
    reads: ExcelRowReads[ApachePoiExcelReadsStack, A]
  ): Eff[R, ValidatedNel[ExcelParseError, A]] =
    for {
      validationRow <- getRow(index)
    } yield validatedMonadInstance.flatMap(validationRow) { row =>
      reads.parse
        .runReader(row)
        .evalState(0)
        .run
    }

}

object ApachePoiExcelRowSYM {
  type ApachePoiExcelReadsStack = Fx.fx2[Reader[ApachePoiRow, *], State[Int, *]]
}
