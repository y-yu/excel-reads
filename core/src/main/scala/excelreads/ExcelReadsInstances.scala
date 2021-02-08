package excelreads

import cats.data.NonEmptyList
import cats.data.Validated
import cats.data.Validated.Invalid
import cats.data.Validated.Valid
import cats.data.ValidatedNel
import excelreads.exception.ExcelParseError
import excelreads.exception.ExcelParseError.UnexpectedEmptyCell
import excelreads.sym.ExcelBasicSYM
import excelreads.sym.ExcelStyleSYM
import org.atnos.eff.Eff
import shapeless.::
import shapeless.Generic
import shapeless.HList
import shapeless.HNil
import shapeless.Lazy
import org.atnos.eff.state._

/**
  * Basic instances
  *
  * @note This parser uses `HList` to parse the `case class`
  *       which do not contain data types like neither `Either` nor ADT.
  *       `Either` and ADT are required `Coproduct` but
  *       the parser sometimes cannot determine which type it should parse.
  *       That's the why I don't make `Coproduct` instances.
  *       If you use ADT in the type representing a Excel row,
  *       you have to implement a instance to parse it.
  */
trait ExcelReadsInstances {
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

  /**
    * This instance only can parse the type whose sequence is at the end.
    * If we want to parse any place on the type, it requires backtrack
    * like regular-expression matcher. It's hard to implement so
    * I haven't implemented it yet for now.
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

  implicit def hNilInstance[R]: ExcelReads[R, HNil] =
    ExcelReads.from { implicit m =>
      get.map(_ => Validated.Valid(HNil))
    }

  implicit def hConsInstances[R, H, T <: HList](implicit
    head: ExcelReads[R, H],
    tail: ExcelReads[R, T]
  ): ExcelReads[R, H :: T] =
    ExcelReads.from { implicit m =>
      for {
        hv <- head.parse
        tv <- tail.parse
      } yield hv.ap(tv.map(t => h => h :: t))
    }

  implicit def hListInstance[R, A, L <: HList](implicit
    gen: Generic.Aux[A, L],
    instance: Lazy[ExcelReads[R, L]]
  ): ExcelReads[R, A] =
    ExcelReads.from { implicit m =>
      instance.value.parse.map(_.map(gen.from))
    }

  /**
    * For simpler, I made type `A` parsers through the `Option[A]` parser.
    * If you want to parse the type such a `Option[Option[A]]`
    * then you have to make your instance.
    * In my opinion it doesn't make sense the nested `Option` parser so I didn't make it.
    */
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
