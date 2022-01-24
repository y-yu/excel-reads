package excelreads

import cats.implicits.*
import cats.data.State
import cats.data.ValidatedNel
import excelreads.exception.ExcelParseError
import excelreads.instance.ExcelRowReadsInstances
import org.atnos.eff.Eff
import org.atnos.eff.|=
import scala.compiletime.*
import scala.deriving.*

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

  final def productImpl[R, A](xs: List[ExcelRowReads[R, _]], a: Mirror.ProductOf[A]): ExcelRowReads[R, A] =
    new ExcelRowReads[R, A] {
      def parse(implicit m: State[Int, *] |= R): Eff[R, ValidatedNel[ExcelParseError, A]] =
        xs.traverse(_.parse: Eff[R, ?]).map { case values =>
          values
            .asInstanceOf[List[ValidatedNel[ExcelParseError, ?]]]
            .sequence
            .map(vs => a.fromProduct(new SeqProduct(vs)))
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
