package excelreads.scala.poi.sym

import cats.data.NonEmptyList
import excelreads.exception.ExcelParseError
import excelreads.exception.ExcelParseError.UnexpectedEmptyCell
import excelreads.scala.poi.PoiScalaRow
import excelreads.sym.ExcelBasicSYM
import info.folone.scala.poi.StringCell
import info.folone.scala.poi.*
import org.atnos.eff.Eff
import org.atnos.eff.state.*
import org.atnos.eff.reader.*
import org.atnos.eff.either.*
import excelreads.eff.ExcelReadsEffects.*
import org.atnos.eff.syntax.all.*

/** Poi Scala implementation
  *
  * @tparam R
  *   effects stack
  */
class PoiScalaExcelBasicSYM[R: _reader[PoiScalaRow, *]: _state: _either] extends ExcelBasicSYM[Eff[R, *]] {

  private def failure[A](e: ExcelParseError): Eff[R, A] =
    fromEither(Left(NonEmptyList(e, Nil)))

  private def getInternal[A](
    pf: PartialFunction[Cell, A]
  ): Eff[R, Option[A]] =
    for {
      index <- get
      row <- ask
      cellOpt = row.value.cells.find(_.index == index)
      result <- Eff.traverseA(cellOpt) { cell =>
        pf
          .andThen(_.pureEff[R])
          .applyOrElse(
            cell,
            (_: Cell) =>
              failure[A](
                UnexpectedEmptyCell(errorIndex = index)
              )
          )
      }
    } yield result

  override def getString: Eff[R, Option[String]] =
    getInternal { case StringCell(_, data) => data }

  override def getDouble: Eff[R, Option[Double]] =
    getInternal { case NumericCell(_, data) => data }

  override def getInt: Eff[R, Option[Int]] =
    getInternal { case NumericCell(_, data) => data.toInt }

  override def getBoolean: Eff[R, Option[Boolean]] =
    getInternal { case BooleanCell(_, data) => data }
}
