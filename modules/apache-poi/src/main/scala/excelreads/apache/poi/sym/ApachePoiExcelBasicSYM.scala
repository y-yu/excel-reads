package excelreads.apache.poi.sym

import cats.data.NonEmptyList
import cats.data.Reader
import cats.data.Validated
import cats.data.ValidatedNel
import excelreads.exception.ExcelParseError
import excelreads.exception.ExcelParseError.UnexpectedEmptyCell
import excelreads.sym.ExcelBasicSYM
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.atnos.eff.Eff
import org.atnos.eff.reader._
import org.atnos.eff.|=
import scala.jdk.CollectionConverters._

object ApachePoiExcelBasicSYM {
  implicit def apachePoiExcelBasicInstance[R](
    implicit m: Reader[Row, *] |= R
  ): ApachePoiExcelBasicSYM[R] =
    new ApachePoiExcelBasicSYM[R]
}

class ApachePoiExcelBasicSYM[R] (
  implicit m: Reader[Row, *] |= R
) extends ExcelBasicSYM[Eff[R, *]] {
  private def successNel[A](a: A): ValidatedNel[ExcelParseError, A] =
    Validated.Valid(a)

  private def failureNel[A](e: ExcelParseError): ValidatedNel[ExcelParseError, A] =
    Validated.Invalid(NonEmptyList(e, Nil))

  private def get[A](
    index: Int,
    pf: PartialFunction[Cell, Option[A]]
  ): Eff[R, ValidatedNel[ExcelParseError, Option[A]]] =
    for {
      row <- ask
    } yield row
      .cellIterator()
      .asScala
      .find(_.getColumnIndex == index) match {
      case Some(a) =>
        pf
          .andThen(a => successNel(a))
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
      { case a => Option(a.getStringCellValue) }
    )

  override def getDouble(index: Int): Eff[R, ValidatedNel[ExcelParseError, Option[Double]]] =
    get(
      index,
      { case a => Option(a.getNumericCellValue) }
    )

  override def getInt(index: Int): Eff[R, ValidatedNel[ExcelParseError, Option[Int]]] =
    get(
      index,
      { case a => Option(a.getNumericCellValue.toInt) }
    )

  override def getBoolean(index: Int): Eff[R, ValidatedNel[ExcelParseError, Option[Boolean]]] =
    get(
      index,
      { case a => Option(a.getBooleanCellValue) }
    )
}
