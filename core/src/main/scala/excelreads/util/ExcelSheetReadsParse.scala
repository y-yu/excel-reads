package excelreads.util

import cats.data.State
import excelreads.ExcelSheetReads
import excelreads.exception.ExcelParseError.ExcelParseErrors
import org.atnos.eff.Eff
import org.atnos.eff.|=

trait ExcelSheetReadsParse {
  def parse[R, A](implicit
    m1: State[Int, *] |= R,
    m2: Either[ExcelParseErrors, *] |= R,
    r1: ExcelSheetReads[R, A]
  ): Eff[R, r1.Result] = r1.parse

  def parse[R, A, B](implicit
    m1: State[Int, *] |= R,
    m2: Either[ExcelParseErrors, *] |= R,
    r1: ExcelSheetReads[R, A],
    r2: ExcelSheetReads[R, B]
  ): Eff[R, (r1.Result, r2.Result)] =
    for {
      a <- r1.parse
      b <- r2.parse
    } yield (a, b)

  def parse[R, A, B, C](implicit
    m1: State[Int, *] |= R,
    m2: Either[ExcelParseErrors, *] |= R,
    r1: ExcelSheetReads[R, A],
    r2: ExcelSheetReads[R, B],
    r3: ExcelSheetReads[R, C]
  ): Eff[R, (r1.Result, r2.Result, r3.Result)] =
    for {
      a <- r1.parse
      b <- r2.parse
      c <- r3.parse
    } yield (a, b, c)

  def parse[R, A, B, C, D](implicit
    m1: State[Int, *] |= R,
    m2: Either[ExcelParseErrors, *] |= R,
    r1: ExcelSheetReads[R, A],
    r2: ExcelSheetReads[R, B],
    r3: ExcelSheetReads[R, C],
    r4: ExcelSheetReads[R, D]
  ): Eff[R, (r1.Result, r2.Result, r3.Result, r4.Result)] =
    for {
      a <- r1.parse
      b <- r2.parse
      c <- r3.parse
      d <- r4.parse
    } yield (a, b, c, d)

  def parse[R, A, B, C, D, E](implicit
    m1: State[Int, *] |= R,
    m2: Either[ExcelParseErrors, *] |= R,
    r1: ExcelSheetReads[R, A],
    r2: ExcelSheetReads[R, B],
    r3: ExcelSheetReads[R, C],
    r4: ExcelSheetReads[R, D],
    r5: ExcelSheetReads[R, E]
  ): Eff[R, (r1.Result, r2.Result, r3.Result, r4.Result, r5.Result)] =
    for {
      a <- r1.parse
      b <- r2.parse
      c <- r3.parse
      d <- r4.parse
      e <- r5.parse
    } yield (a, b, c, d, e)

  def parse[R, A, B, C, D, E, F](implicit
    m1: State[Int, *] |= R,
    m2: Either[ExcelParseErrors, *] |= R,
    r1: ExcelSheetReads[R, A],
    r2: ExcelSheetReads[R, B],
    r3: ExcelSheetReads[R, C],
    r4: ExcelSheetReads[R, D],
    r5: ExcelSheetReads[R, E],
    r6: ExcelSheetReads[R, F]
  ): Eff[R, (r1.Result, r2.Result, r3.Result, r4.Result, r5.Result, r6.Result)] =
    for {
      a <- r1.parse
      b <- r2.parse
      c <- r3.parse
      d <- r4.parse
      e <- r5.parse
      f <- r6.parse
    } yield (a, b, c, d, e, f)
}
