package excelreads.util

import cats.data.State
import excelreads.ExcelRowQuantifier.End
import excelreads.ExcelSheetReads
import excelreads.exception.ExcelParseError.ExcelParseErrors
import org.atnos.eff.Eff
import org.atnos.eff.|=
import org.atnos.eff.syntax.all.*

trait ExcelSheetReadsParseLoop {
  private def loopInternal[R, A, B](
    reads: ExcelSheetReads[R, A]
  )(
    transform: reads.Result => B
  )(implicit
    m1: State[Int, *] |= R,
    m2: Either[ExcelParseErrors, *] |= R,
    end: ExcelSheetReads[R, End]
  ): Eff[R, Seq[B]] =
    for {
      isEnd <- end.parse
      result <- isEnd match {
        case true =>
          Seq.empty.pureEff[R]
        case false =>
          for {
            a <- reads.parse
            ts <- loop1(end, reads, m1, m2)
          } yield a +: ts
      }
    } yield result.map(transform)

  private def loop1[R, A](implicit
    end: ExcelSheetReads[R, End],
    r1: ExcelSheetReads[R, A],
    m1: State[Int, *] |= R,
    m2: Either[ExcelParseErrors, *] |= R
  ): Eff[R, Seq[r1.Result]] =
    loopInternal[R, A, r1.Result](r1)(x => x)

  def loop[R, A](implicit
    end: ExcelSheetReads[R, End],
    r1: ExcelSheetReads[R, A],
    m1: State[Int, *] |= R,
    m2: Either[ExcelParseErrors, *] |= R
  ): Eff[R, Seq[r1.Result]] =
    loop1[R, A]

  def loop[R, A, B](implicit
    end: ExcelSheetReads[R, End],
    r1: ExcelSheetReads[R, A],
    r2: ExcelSheetReads[R, B],
    m1: State[Int, *] |= R,
    m2: Either[ExcelParseErrors, *] |= R
  ): Eff[R, Seq[(r1.Result, r2.Result)]] = {
    loopInternal[R, (A, B), (r1.Result, r2.Result)](
      r1 product r2
    ) {
      // I don't understand why this needs `asInstanceOf`...?
      // TODO: Don't use `asInstanceOf`
      _.asInstanceOf[(r1.Result, r2.Result)]
    }
  }

  def loop[R, A, B, C](implicit
    end: ExcelSheetReads[R, End],
    r1: ExcelSheetReads[R, A],
    r2: ExcelSheetReads[R, B],
    r3: ExcelSheetReads[R, C],
    m1: State[Int, *] |= R,
    m2: Either[ExcelParseErrors, *] |= R
  ): Eff[R, Seq[(r1.Result, r2.Result, r3.Result)]] =
    loopInternal[R, ((A, B), C), (r1.Result, r2.Result, r3.Result)](
      r1 product r2 product r3
    ) {
      _.asInstanceOf[((r1.Result, r2.Result), r3.Result)] match {
        case ((a, b), c) => (a, b, c)
      }
    }

  def loop[R, A, B, C, D](implicit
    end: ExcelSheetReads[R, End],
    r1: ExcelSheetReads[R, A],
    r2: ExcelSheetReads[R, B],
    r3: ExcelSheetReads[R, C],
    r4: ExcelSheetReads[R, D],
    m1: State[Int, *] |= R,
    m2: Either[ExcelParseErrors, *] |= R
  ): Eff[R, Seq[(r1.Result, r2.Result, r3.Result, r4.Result)]] =
    loopInternal[R, (((A, B), C), D), (r1.Result, r2.Result, r3.Result, r4.Result)](
      r1 product r2 product r3 product r4
    ) {
      _.asInstanceOf[(((r1.Result, r2.Result), r3.Result), r4.Result)] match {
        case (((a, b), c), d) => (a, b, c, d)
      }
    }
}
