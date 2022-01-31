package excelreads

import cats.implicits.catsStdInstancesForList
import cats.data.State
import cats.data.ValidatedNel
import cats.data.Validated.Valid
import excelreads.exception.ExcelParseError
import excelreads.exception.ExcelParseError.ExcelParseErrors
import excelreads.instance.ExcelRowReadsInstances
import org.atnos.eff.Eff
import org.atnos.eff.|=
import scala.compiletime.*
import scala.deriving.*
import org.atnos.eff.syntax.all.*

trait ExcelRowReadsGenericInstances { self: ExcelRowReadsInstances =>

  inline implicit def derive[R, A]: ExcelRowReads[R, A] =
    summonFrom {
      case x: ExcelRowReads[R, A] =>
        x
      case _: Mirror.ProductOf[A] =>
        deriveProduct[R, A]
    }

  inline def deriveProduct[R, A](using inline a: Mirror.ProductOf[A]): ExcelRowReads[R, A] = {
    val xs = deriveRec[R, a.MirroredElemTypes]
    productImpl[R, A](xs, a)
  }

  final def productImpl[R, A](xs: List[ExcelRowReads[R, _]], a: Mirror.ProductOf[A]): ExcelRowReads[R, A] = {
    def loop(
      xs: List[ExcelRowReads[R, _]],
      acc: List[_]
    )(implicit
      m1: State[Int, *] |= R,
      m2: Either[ExcelParseErrors, *] |= R
    ): Eff[R, List[_]] =
      xs match {
        case Nil =>
          acc.pureEff[R]

        case x :: ts =>
          for {
            a <- x.parse
            result <- loop(ts, acc :+ a)
          } yield result
      }

    ExcelRowReads.from { implicit m1 => implicit m2 =>
      loop(xs, Nil).map(vs => a.fromProduct(new SeqProduct(vs)))
    }
  }

  inline def deriveRec[R, T <: Tuple]: List[ExcelRowReads[R, _]] =
    inline erasedValue[T] match {
      case _: EmptyTuple =>
        Nil
      case _: (t *: ts) =>
        derive[R, t] :: deriveRec[R, ts]
    }
}
