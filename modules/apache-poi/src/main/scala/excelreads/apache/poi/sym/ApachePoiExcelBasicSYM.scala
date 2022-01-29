package excelreads.apache.poi.sym

import cats.data.NonEmptyList
import excelreads.apache.poi.ApachePoiRow
import excelreads.exception.ExcelParseError
import excelreads.exception.ExcelParseError.UnexpectedEmptyCell
import excelreads.sym.ExcelBasicSYM
import org.apache.poi.ss.usermodel.Cell
import org.atnos.eff.Eff
import org.atnos.eff.state.*
import org.atnos.eff.reader.*
import org.atnos.eff.either.*
import org.atnos.eff.syntax.all.*
import scala.jdk.CollectionConverters.*
import scala.util.control.NonFatal
import excelreads.eff.ExcelReadsEffects.*
import org.apache.poi.ss.usermodel.CellType

/** Apache POI implementation
  *
  * @tparam R
  *   effects stack
  */
class ApachePoiExcelBasicSYM[R: _reader[ApachePoiRow, *]: _state: _either] extends ExcelBasicSYM[Eff[R, *]] {
  private def failure[A](e: ExcelParseError): Eff[R, A] =
    fromEither(Left(NonEmptyList(e, Nil)))

  private def getInternal[A](
    pf: PartialFunction[Cell, Option[A]]
  ): Eff[R, Option[A]] =
    for {
      index <- get
      row <- ask
      _ <-
        if (Option(row.value).exists(_.getLastCellNum > 0)) right(())
        else failure(ExcelParseError.UnexpectedEmptyCell(index))
      cellOpt <- fromCatchNonFatal(
        row.value
          .cellIterator()
          .asScala
          .find(_.getColumnIndex == index)
      )(e => NonEmptyList(ExcelParseError.UnknownError(index, e.getMessage, e), Nil))
      result <- cellOpt match {
        case None =>
          right(None)
        case Some(a) =>
          try {
            pf
              .andThen(_.pureEff[R])
              .applyOrElse(
                a,
                (_: Cell) =>
                  failure(
                    UnexpectedEmptyCell(errorIndex = index)
                  )
              )
          } catch {
            case NonFatal(e) =>
              failure(ExcelParseError.UnknownError(index, e.getMessage, e))
          }
      }
    } yield result

  override def getString: Eff[R, Option[String]] =
    getInternal {
      case a if a.getCellType == CellType.STRING =>
        Option(a.getStringCellValue)
    }

  override def getDouble: Eff[R, Option[Double]] =
    getInternal {
      case a if a.getCellType == CellType.NUMERIC =>
        Option(a.getNumericCellValue)
    }

  override def getInt: Eff[R, Option[Int]] =
    getInternal {
      case a if a.getCellType == CellType.NUMERIC =>
        Option(a.getNumericCellValue.toInt)
    }

  override def getBoolean: Eff[R, Option[Boolean]] =
    getInternal {
      case a if a.getCellType == CellType.BOOLEAN =>
        Option(a.getBooleanCellValue)
    }
}
