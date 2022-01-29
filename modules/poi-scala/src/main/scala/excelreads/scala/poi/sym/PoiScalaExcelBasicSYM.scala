package excelreads.scala.poi.sym

import cats.data.NonEmptyList
import cats.data.Validated
import cats.data.ValidatedNel
import excelreads.exception.ExcelParseError
import excelreads.exception.ExcelParseError.UnexpectedEmptyCell
import excelreads.scala.poi.PoiScalaRow
import excelreads.sym.ExcelBasicSYM
import info.folone.scala.poi.StringCell
import info.folone.scala.poi.*
import org.atnos.eff.Eff
import org.atnos.eff.reader.*
import excelreads.eff.ExcelReadsEffects.*

/** Poi Scala implementation
  *
  * @tparam R
  *   effects stack which contains `Reader[Row, *]`
  */
class PoiScalaExcelBasicSYM[R: _reader[PoiScalaRow, *]: _either] extends ExcelBasicSYM[Eff[R, *]] {

  private def successNel[A](a: A): ValidatedNel[ExcelParseError, A] =
    Validated.Valid(a)

  private def failureNel[A](e: ExcelParseError): ValidatedNel[ExcelParseError, A] =
    Validated.Invalid(NonEmptyList(e, Nil))

  private def get[A](
    index: Int,
    pf: PartialFunction[Cell, A]
  ): Eff[R, ValidatedNel[ExcelParseError, Option[A]]] =
    for {
      row <- ask
    } yield row.value.cells
      .find(_.index == index) match {
      case Some(a) =>
        pf
          .andThen(a => successNel(Some(a)))
          .applyOrElse(
            a,
            (_: Cell) =>
              failureNel(
                UnexpectedEmptyCell(errorIndex = index)
              )
          )
      case None =>
        successNel(None)
    }

  override def getString(index: Int): Eff[R, ValidatedNel[ExcelParseError, Option[String]]] =
    get(
      index,
      { case StringCell(_, data) => data }
    )

  override def getDouble(index: Int): Eff[R, ValidatedNel[ExcelParseError, Option[Double]]] =
    get(
      index,
      { case NumericCell(_, data) => data }
    )

  override def getInt(index: Int): Eff[R, ValidatedNel[ExcelParseError, Option[Int]]] =
    get(
      index,
      { case NumericCell(_, data) => data.toInt }
    )

  override def getBoolean(index: Int): Eff[R, ValidatedNel[ExcelParseError, Option[Boolean]]] =
    get(
      index,
      { case BooleanCell(_, data) => data }
    )
}
