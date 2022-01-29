package excelreads.util

import excelreads.ExcelSheetReads
import excelreads.eff.ExcelReadsEffects.*
import org.atnos.eff.Eff

trait ExcelSheetReadsParse {
  def parse[R: _state: _either, A](implicit
    r1: ExcelSheetReads[R, A]
  ): Eff[R, r1.Result] = r1.parse

  def parse[R: _state: _either, A, B](implicit
    r1: ExcelSheetReads[R, A],
    r2: ExcelSheetReads[R, B]
  ): Eff[R, (r1.Result, r2.Result)] =
    for {
      a <- r1.parse
      b <- r2.parse
    } yield (a, b)

  def parse[R: _state: _either, A, B, C](implicit
    r1: ExcelSheetReads[R, A],
    r2: ExcelSheetReads[R, B],
    r3: ExcelSheetReads[R, C]
  ): Eff[R, (r1.Result, r2.Result, r3.Result)] =
    for {
      a <- r1.parse
      b <- r2.parse
      c <- r3.parse
    } yield (a, b, c)

  def parse[R: _state: _either, A, B, C, D](implicit
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

  def parse[R: _state: _either, A, B, C, D, E](implicit
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

  def parse[R: _state: _either, A, B, C, D, E, F](implicit
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
