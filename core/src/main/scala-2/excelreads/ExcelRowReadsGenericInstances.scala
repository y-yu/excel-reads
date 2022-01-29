package excelreads

import cats.data.Validated
import excelreads.instance.ExcelRowReadsInstances
import org.atnos.eff.state.get
import shapeless.::
import shapeless.Generic
import shapeless.HList
import shapeless.HNil
import shapeless.Lazy
import org.atnos.eff.Eff

trait ExcelRowReadsGenericInstances { self: ExcelRowReadsInstances =>
  implicit def hNilInstance[R]: ExcelRowReads[R, HNil] =
    ExcelRowReads.from { implicit m1 => implicit m2 =>
      get.map(_ => HNil)
    }

  implicit def hConsInstances[R, H, T <: HList](implicit
    head: ExcelRowReads[R, H],
    tail: ExcelRowReads[R, T]
  ): ExcelRowReads[R, H :: T] =
    ExcelRowReads.from { implicit m1 => implicit m2 =>
      for {
        hv <- head.parse
        tv <- tail.parse
      } yield hv :: tv
    }

  implicit def hListInstance[R, A, L <: HList](implicit
    gen: Generic.Aux[A, L],
    instance: Lazy[ExcelRowReads[R, L]]
  ): ExcelRowReads[R, A] =
    ExcelRowReads.from { implicit m1 => implicit m2 =>
      instance.value.parse.map(gen.from)
    }
}
