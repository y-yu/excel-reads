package excelreads

import cats.data.NonEmptyList
import cats.data.Validated.Invalid
import cats.data.Validated.Valid
import cats.data.ValidatedNel
import excelreads.exception.ExcelParseError
import excelreads.exception.ExcelParseError.UnexpectedEmptyCell
import excelreads.sym.ExcelBasicSYM
import excelreads.sym.ExcelStyleSYM
import org.atnos.eff.Eff
import org.atnos.eff.state._

/** Basic instances
  *
  * @note
  *   This parser uses `HList` to parse the `case class` which do not contain data types like neither `Either` nor ADT.
  *   `Either` and ADT are required `Coproduct` but the parser sometimes cannot determine which type it should parse.
  *   That's the why I don't make `Coproduct` instances. If you use ADT in the type representing a Excel row, you have
  *   to implement a instance to parse it.
  */
trait ExcelReadsInstances extends ExcelReadsGenericInstances with ExcelReadsLowPriorityInstance {
  // Primitive instances
  private def basicInstance[R, A](
    f: ExcelBasicSYM[Eff[R, *]] => Int => Eff[R, ValidatedNel[ExcelParseError, A]]
  )(implicit
    sym: ExcelBasicSYM[Eff[R, *]]
  ): ExcelReads[R, A] =
    ExcelReads.from { implicit m =>
      for {
        s <- get
        aOpt <- f(sym)(s)
        _ <- put(s + 1)
      } yield aOpt
    }

  implicit def stringInstance[R](implicit
    sym: ExcelBasicSYM[Eff[R, *]]
  ): ExcelReads[R, Option[String]] =
    basicInstance { sym => sym.getString }

  implicit def doubleInstance[R](implicit
    sym: ExcelBasicSYM[Eff[R, *]]
  ): ExcelReads[R, Option[Double]] =
    basicInstance { sym => sym.getDouble }

  implicit def intInstance[R](implicit
    sym: ExcelBasicSYM[Eff[R, *]]
  ): ExcelReads[R, Option[Int]] =
    basicInstance { sym => sym.getInt }

  implicit def booleanInstance[R](implicit
    sym: ExcelBasicSYM[Eff[R, *]]
  ): ExcelReads[R, Option[Boolean]] =
    basicInstance { sym => sym.getBoolean }

  // This instance won't increment state
  // even if getting a style data from the cell.
  implicit def styleInstance[R, Style](implicit
    sym: ExcelStyleSYM[Style, Eff[R, *]]
  ): ExcelReads[R, Option[Style]] =
    ExcelReads.from { implicit m =>
      for {
        s <- get
        styleOpt <- sym.getStyle(s)
      } yield styleOpt
    }

  /** This instance only can parse the type whose sequence is at the end. If we want to parse any place on the type, it
    * requires backtrack like regular-expression matcher. It's hard to implement so I haven't implemented it yet for
    * now.
    */
  implicit def listInstance[R, A](implicit
    reads: ExcelReads[R, Option[A]]
  ): ExcelReads[R, List[A]] =
    ExcelReads.from { implicit m =>
      def loop(
        acc: ValidatedNel[ExcelParseError, List[A]]
      ): Eff[R, ValidatedNel[ExcelParseError, List[A]]] =
        for {
          value <- reads.parse
          result <- value match {
            case Valid(Some(a)) =>
              loop(acc.map(a :: _))

            case Valid(None) =>
              Eff.pure[R, ValidatedNel[ExcelParseError, List[A]]](acc.map(_.reverse))

            case Invalid(e) =>
              // To collect all errors.
              loop(acc.leftMap(l => e ::: l))
          }
        } yield result

      loop(Valid(Nil))
    }
}

trait ExcelReadsLowPriorityInstance {
  implicit def aInstance[R, A](implicit
    reads: ExcelReads[R, Option[A]]
  ): ExcelReads[R, A] =
    ExcelReads.from { implicit m =>
      for {
        s <- get
        result <- reads.parse
      } yield result.andThen {
        case Some(a) => Valid(a)
        case None =>
          Invalid(
            NonEmptyList(UnexpectedEmptyCell(s), Nil)
          )
      }
    }
}
