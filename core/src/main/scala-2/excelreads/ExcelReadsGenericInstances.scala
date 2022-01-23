package excelreads

import cats.data.NonEmptyList
import cats.data.Validated
import cats.data.Validated.Invalid
import cats.data.Validated.Valid
import excelreads.exception.ExcelParseError.UnexpectedEmptyCell
import excelreads.instance.ExcelReadsInstances
import org.atnos.eff.state.get
import shapeless.::
import shapeless.Generic
import shapeless.HList
import shapeless.HNil
import shapeless.Lazy

trait ExcelReadsGenericInstances { self: ExcelReadsInstances =>
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
}
