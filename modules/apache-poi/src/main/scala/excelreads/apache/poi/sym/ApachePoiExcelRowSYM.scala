package excelreads.apache.poi.sym

import cats.data.NonEmptyList
import cats.data.Reader
import cats.data.State
import excelreads.ExcelRowReads
import excelreads.apache.poi.ApachePoiRow
import excelreads.apache.poi.ApachePoiSheet
import excelreads.exception.ExcelParseError
import excelreads.sym.ExcelRowSYM
import cats.implicits.*
import org.atnos.eff.Eff
import org.atnos.eff.syntax.all.*
import org.atnos.eff.state.*
import org.atnos.eff.reader.*
import org.atnos.eff.either.*
import org.atnos.eff.Fx
import excelreads.apache.poi.sym.ApachePoiExcelRowSYM.ApachePoiExcelReadsStack
import excelreads.eff.ExcelReadsEffects.*
import excelreads.exception.ExcelParseError.ExcelParseErrors
import excelreads.exception.ExcelParseError.UnexpectedCellInRow

class ApachePoiExcelRowSYM[
  R: _reader[ApachePoiSheet, *]: _state: _either
] extends ExcelRowSYM[ApachePoiRow, ApachePoiExcelReadsStack, Eff[R, *]] {
  private def toErrors[A](index: Int)(e: Throwable): ExcelParseErrors =
    NonEmptyList(ExcelParseError.UnknownError(index, e.getMessage, e), Nil)

  override def isEmpty: Eff[R, Boolean] =
    for {
      index <- get
      sheet <- ask
      result <-
        fromCatchNonFatal(Option(sheet.value.getRow(index)).isEmpty)(toErrors(index))
    } yield result

  override def isEnd: Eff[R, Boolean] =
    for {
      index <- get
      sheet <- ask
      result <-
        fromCatchNonFatal(sheet.value.getLastRowNum <= index)(toErrors(index))
    } yield result

  override def getRow: Eff[R, ApachePoiRow] =
    for {
      index <- get
      sheet <- ask
      result <-
        fromCatchNonFatal(ApachePoiRow(sheet.value.getRow(index)))(toErrors(index))
    } yield result

  override def withRow[A](
    reads: ExcelRowReads[ApachePoiExcelReadsStack, A]
  ): Eff[R, Either[ExcelParseErrors, A]] =
    for {
      index <- get
      row <- getRow
      result <-
        reads.parse
          .runReader(row)
          .evalState(0)
          .runEither
          .run
          .leftMap { es: ExcelParseErrors =>
            NonEmptyList(UnexpectedCellInRow(index, es), Nil)
          }
          .pureEff[R]
    } yield result
}

object ApachePoiExcelRowSYM {
  type ApachePoiExcelReadsStack =
    Fx.fx3[Reader[ApachePoiRow, *], State[Int, *], Either[ExcelParseErrors, *]]
}
