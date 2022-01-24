package excelreads.util

import cats.data.State
import cats.data.Validated.Invalid
import cats.data.Validated.Valid
import cats.data.ValidatedNel
import excelreads.ExcelRowQuantifier.End
import excelreads.ExcelSheetReads
import excelreads.exception.ExcelParseError
import org.atnos.eff.Eff
import org.atnos.eff.|=
import excelreads.instance.ValidatedMonadInstance.*

trait ExcelSheetReadsParseLoop {
  private def loopInternal[R, A, B](
    reads: ExcelSheetReads[R, A]
  )(
    transform: reads.Result => B
  )(implicit
    m: State[Int, *] |= R,
    end: ExcelSheetReads[R, End]
  ): Eff[R, ValidatedNel[ExcelParseError, Seq[B]]] =
    for {
      aValidation <- reads.parse
      result <- aValidation match {
        case Valid(a) =>
          loop1[R, A](end, reads, m).map { _.map(as => a +: as) }
        case Invalid(e) =>
          for {
            isEnd <- end.parse
            isValidEnd <- Eff.flatTraverseA(isEnd) {
              case true =>
                Eff.pure[R, ValidatedNel[ExcelParseError, Seq[reads.Result]]](Valid(Seq.empty))
              case false =>
                Eff.pure[R, ValidatedNel[ExcelParseError, Seq[reads.Result]]](Invalid(e))
            }
          } yield isValidEnd
      }
    } yield result.map(_.map(transform))

  private def loop1[R, A](implicit
    end: ExcelSheetReads[R, End],
    r1: ExcelSheetReads[R, A],
    m: State[Int, *] |= R
  ): Eff[R, ValidatedNel[ExcelParseError, Seq[r1.Result]]] =
    loopInternal[R, A, r1.Result](r1)(x => x)

  def loop[R, A](implicit
    end: ExcelSheetReads[R, End],
    r1: ExcelSheetReads[R, A],
    m: State[Int, *] |= R
  ): Eff[R, ValidatedNel[ExcelParseError, Seq[r1.Result]]] =
    loop1[R, A]

  def loop[R, A, B](implicit
    end: ExcelSheetReads[R, End],
    r1: ExcelSheetReads[R, A],
    r2: ExcelSheetReads[R, B],
    m: State[Int, *] |= R
  ): Eff[R, ValidatedNel[ExcelParseError, Seq[(r1.Result, r2.Result)]]] = {
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
    m: State[Int, *] |= R
  ): Eff[R, ValidatedNel[ExcelParseError, Seq[(r1.Result, r2.Result, r3.Result)]]] =
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
    m: State[Int, *] |= R
  ): Eff[R, ValidatedNel[ExcelParseError, Seq[(r1.Result, r2.Result, r3.Result, r4.Result)]]] =
    loopInternal[R, (((A, B), C), D), (r1.Result, r2.Result, r3.Result, r4.Result)](
      r1 product r2 product r3 product r4
    ) {
      _.asInstanceOf[(((r1.Result, r2.Result), r3.Result), r4.Result)] match {
        case (((a, b), c), d) => (a, b, c, d)
      }
    }
}
