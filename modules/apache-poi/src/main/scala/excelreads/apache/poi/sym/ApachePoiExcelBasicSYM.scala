package excelreads.apache.poi.sym

import cats.data.NonEmptyList
import cats.data.Reader
import cats.data.Validated
import cats.data.ValidatedNel
import excelreads.apache.poi.ApachePoiRow
import excelreads.exception.ExcelParseError
import excelreads.exception.ExcelParseError.UnexpectedEmptyCell
import excelreads.exception.ExcelParseError.UnexpectedTypeCell
import excelreads.sym.ExcelBasicSYM
import org.apache.poi.ss.usermodel.Cell
import org.atnos.eff.Eff
import org.atnos.eff.reader.*
import org.atnos.eff.|=
import scala.jdk.CollectionConverters.*

/** Apache POI implementation
  *
  * @tparam R
  *   effects stack which contains `Reader[Row, *]`
  */
class ApachePoiExcelBasicSYM[R](implicit
  m: Reader[ApachePoiRow, *] |= R
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
    } yield row.value
      .cellIterator()
      .asScala
      .find(_.getColumnIndex == index) match {
      case Some(a) =>
        try {
          pf
            .andThen(a => successNel(a))
            .applyOrElse(
              a,
              (_: Cell) =>
                failureNel(
                  UnexpectedEmptyCell(errorIndex = index)
                )
            )
        } catch {
          case e: IllegalStateException =>
            failureNel(
              UnexpectedTypeCell(errorIndex = index, actualCellType = a.getCellType.name(), cause = e)
            )
        }
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
