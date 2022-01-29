package excelreads.instance

import cats.data.NonEmptyList
import excelreads.ExcelRowReads
import excelreads.ExcelRowReadsGenericInstances
import excelreads.exception.ExcelParseError.ExcelParseErrors
import excelreads.exception.ExcelParseError.UnexpectedEmptyCell
import excelreads.sym.ExcelBasicSYM
import excelreads.sym.ExcelStyleSYM
import org.atnos.eff.Eff
import org.atnos.eff.state.*
import org.atnos.eff.syntax.all.*
import org.atnos.eff.either.*

/** Basic instances
  *
  * @note
  *   This parser uses `HList` to parse the `case class` which do not contain data types like neither `Either` nor ADT.
  *   `Either` and ADT are required `Coproduct` but the parser sometimes cannot determine which type it should parse.
  *   That's the why I don't make `Coproduct` instances. If you use ADT in the type representing a Excel row, you have
  *   to implement a instance to parse it.
  */
trait ExcelRowReadsInstances extends ExcelRowReadsGenericInstances with ExcelReadsLowPriorityInstance {
  // Primitive instances
  private def basicInstance[R, A](
    f: ExcelBasicSYM[Eff[R, *]] => Eff[R, A]
  )(implicit
    sym: ExcelBasicSYM[Eff[R, *]]
  ): ExcelRowReads[R, A] =
    ExcelRowReads.from { implicit m1 => implicit m2 =>
      for {
        a <- f(sym)
        s <- get
        _ <- put(s + 1)
      } yield a
    }

  implicit def stringInstance[R](implicit
    sym: ExcelBasicSYM[Eff[R, *]]
  ): ExcelRowReads[R, Option[String]] =
    basicInstance { sym => sym.getString }

  implicit def doubleInstance[R](implicit
    sym: ExcelBasicSYM[Eff[R, *]]
  ): ExcelRowReads[R, Option[Double]] =
    basicInstance { sym => sym.getDouble }

  implicit def intInstance[R](implicit
    sym: ExcelBasicSYM[Eff[R, *]]
  ): ExcelRowReads[R, Option[Int]] =
    basicInstance { sym => sym.getInt }

  implicit def booleanInstance[R](implicit
    sym: ExcelBasicSYM[Eff[R, *]]
  ): ExcelRowReads[R, Option[Boolean]] =
    basicInstance { sym => sym.getBoolean }

  // This instance won't increment state
  // even if getting a style data from the cell.
  implicit def styleInstance[R, Style](implicit
    sym: ExcelStyleSYM[Style, Eff[R, *]]
  ): ExcelRowReads[R, Option[Style]] =
    ExcelRowReads.from { implicit m1 => implicit m2 =>
      for {
        s <- get
        styleOpt <- sym.getStyle
      } yield styleOpt
    }

  /** This instance only can parse the type whose sequence is at the end. If we want to parse any place on the type, it
    * requires backtrack like regular-expression matcher. It's hard to implement so I haven't implemented it yet for
    * now.
    */
  implicit def listInstance[R, A](implicit
    reads: ExcelRowReads[R, Option[A]]
  ): ExcelRowReads[R, List[A]] =
    ExcelRowReads.from { implicit m1 => implicit m2 =>
      def loop(
        acc: List[A]
      ): Eff[R, List[A]] = {
        reads.parse
        for {
          value <- reads.parse
          result <- value match {
            case Some(a) =>
              loop(a :: acc)

            case None =>
              acc.reverse.pureEff[R]
          }
        } yield result
      }

      loop(Nil)
    }
}

trait ExcelReadsLowPriorityInstance {
  implicit def aInstance[R, A](implicit
    reads: ExcelRowReads[R, Option[A]]
  ): ExcelRowReads[R, A] =
    ExcelRowReads.from { implicit m1 => implicit m2 =>
      for {
        s <- get
        opt <- reads.parse
        a <- optionEither[R, ExcelParseErrors, A](
          opt,
          NonEmptyList(UnexpectedEmptyCell(s), Nil)
        )
      } yield a
    }
}
