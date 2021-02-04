package excelreads

import cats.data.NonEmptyList
import cats.data.State
import cats.data.Validated
import cats.data.Validated.Invalid
import cats.data.Validated.Valid
import cats.data.ValidatedNel
import excelreads.exception.ExcelParseError
import excelreads.exception.ExcelParseError.UnexpectedEmptyCell
import shapeless.::
import shapeless.Generic
import shapeless.HList
import shapeless.HNil
import shapeless.Lazy
import scala.annotation.tailrec

/**
  * This parser uses `HList` to parse the `case class`
  * which do not contain data types like neither `Either` nor ADT.
  *
  * @note `Either` and ADT are required `Coproduct` but
  *       the parser sometimes cannot determine which type it should parse.
  *       That's the why I don't make `Coproduct` instances.
  *       If you use ADT in the type representing a Excel row,
  *       you have to implement a instance to parse it.
  */
trait ExcelReadsInstances[Row] {
  protected type Reads[A] <: ExcelReads[Row, A]

  def fromInstance[A](
    implicit reads: ExcelReads[Row, A]
  ): Reads[A]

  /**
    * This instance only can parse the type whose sequence is at the end.
    * If we want to parse any place on the type, it requires backtrack
    * like regular-expression matcher. It's hard to implement so
    * I haven't implemented it yet for now.
    */
  implicit def listInstance[A](
    implicit reads: Reads[Option[A]]
  ): Reads[List[A]] = fromInstance { row =>
    @tailrec
    def loop(
      s: Int,
      acc: ValidatedNel[ExcelParseError, List[A]]
    ): (Int, ValidatedNel[ExcelParseError, List[A]]) = {
      val (next, value) = reads.parse(row).run(s).value

      value match {
        case Valid(Some(a)) =>
          loop(next, acc.map(a :: _))

        case Valid(None) =>
          (s, acc.map(_.reverse))

        case Invalid(e) =>
          // To collect all errors.
          loop(next, acc.leftMap(l => e ::: l))
      }
    }

    State(s => loop(s, Valid(Nil)))
  }

  implicit def hNilInstance: Reads[HNil] = fromInstance { _ =>
    State(s => (s, Validated.Valid(HNil)))
  }

  implicit def hConsInstances[H, T <: HList](implicit
    head: Reads[H],
    tail: Reads[T]
  ): Reads[H :: T] = fromInstance { row =>
    for {
      hv <- head.parse(row)
      tv <- tail.parse(row)
    } yield
      hv.ap(tv.map(t => h => h :: t))
  }

  implicit def hListInstance[A, L <: HList](implicit
    gen: Generic.Aux[A, L],
    instance: Lazy[Reads[L]]
  ): Reads[A] = fromInstance { row =>
    instance.value.parse(row).map(_.map(gen.from))
  }

  /**
    * For simpler, I made type `A` parsers through the `Option[A]` parser.
    * If you want to parse the type such a `Option[Option[A]]`
    * then you have to make your instance.
    * In my opinion it doesn't make sense the nested `Option` parser so I didn't make it.
    */
  implicit def aInstance[A](implicit
    reads: Reads[Option[A]]
  ): Reads[A] = fromInstance { row =>
    for {
      s <- State.get[Int]
      result <- reads.parse(row)
    } yield result.andThen {
      case Some(a) => Valid(a)
      case None => Invalid(
        NonEmptyList(UnexpectedEmptyCell(s), Nil)
      )
    }
  }
}
