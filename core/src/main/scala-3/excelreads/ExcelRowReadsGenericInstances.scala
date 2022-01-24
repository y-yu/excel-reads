package excelreads

import cats.implicits.catsStdInstancesForList
import cats.data.State
import cats.data.ValidatedNel
import cats.data.Validated.Valid
import excelreads.exception.ExcelParseError
import excelreads.instance.ExcelRowReadsInstances
import org.atnos.eff.Eff
import org.atnos.eff.|=
import scala.compiletime.*
import scala.deriving.*
import excelreads.instance.ValidatedMonadInstance.*

trait ExcelRowReadsGenericInstances { self: ExcelRowReadsInstances =>

  inline implicit def derive[R, A]: ExcelRowReads[R, A] =
    summonFrom {
      case x: ExcelRowReads[R, A] =>
        x
      case _: Mirror.ProductOf[A] =>
        deriveProduct[R, A]
    }

  inline def deriveProduct[R, A](using inline a: Mirror.ProductOf[A]): ExcelRowReads[R, A] = {
    def p: ExcelRowReads[R, A] = {
      val xs = deriveRec[R, a.MirroredElemTypes]
      productImpl[R, A](xs, a)
    }
    p
  }

  final def productImpl[R, A](xs: List[ExcelRowReads[R, _]], a: Mirror.ProductOf[A]): ExcelRowReads[R, A] = {
    def loop(
      xs: List[ExcelRowReads[R, _]],
      acc: List[_]
    )(implicit m: State[Int, *] |= R): Eff[R, ValidatedNel[ExcelParseError, List[_]]] =
      xs match {
        case Nil =>
          Eff.pure[R, ValidatedNel[ExcelParseError, List[?]]](Valid(acc))

        case x :: t =>
          for {
            h <- x.parse
            result <- Eff.flatTraverseA(h) { a =>
              loop(t, acc :+ a)
            }
          } yield result
      }

    new ExcelRowReads[R, A] {
      def parse(implicit m: State[Int, *] |= R): Eff[R, ValidatedNel[ExcelParseError, A]] =
        loop(xs, Nil).map(_.map(vs => a.fromProduct(new SeqProduct(vs))))
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
