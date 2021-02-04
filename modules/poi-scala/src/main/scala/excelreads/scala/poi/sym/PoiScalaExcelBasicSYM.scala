package excelreads.scala.poi.sym

import cats.data.NonEmptyList
import cats.data.Reader
import cats.data.Validated
import cats.data.ValidatedNel
import excelreads.exception.ExcelParseError
import excelreads.exception.ExcelParseError.UnexpectedEmptyCell
import excelreads.sym.ExcelBasicSYM
import info.folone.scala.poi.Row
import info.folone.scala.poi.StringCell
import info.folone.scala.poi._
import org.atnos.eff.Eff
import org.atnos.eff.Member
import org.atnos.eff.NoFx
import org.atnos.eff.reader._
import org.atnos.eff.|=

object PoiScalaExcelBasicSYM {
  implicit def scalaPoiInstances[R](implicit
    m: Member.Aux[Reader[Row, *], R, NoFx]
  ): PoiScalaExcelBasicSYM[R]  =
    new PoiScalaExcelBasicSYM
}

/**
  * Poi Scala implementation
  *
  * @tparam R effect stack which contains `Reader[Row, *]`
  */
class PoiScalaExcelBasicSYM[R](
  implicit m: Reader[Row, *] |= R
) extends ExcelBasicSYM[Eff[R, *]] {

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
    } yield row.cells
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
