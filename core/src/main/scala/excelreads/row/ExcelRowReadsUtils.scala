package excelreads.row

import cats.data.State
import cats.data.ValidatedNel
import excelreads.ExcelRowReads
import excelreads.exception.ExcelParseError
import org.atnos.eff.Eff
import org.atnos.eff.|=

trait ExcelRowReadsUtils {
  def parse[R, A](implicit
    m: State[Int, *] |= R,
    r1: ExcelRowReads[R, A]
  ): Eff[R, ValidatedNel[ExcelParseError, r1.Result]] = r1.parse

  def parse[R, A, B](implicit
    m: State[Int, *] |= R,
    r1: ExcelRowReads[R, A],
    r2: ExcelRowReads[R, B]
  ): Eff[R, ValidatedNel[ExcelParseError, (r1.Result, r2.Result)]] =
    for {
      o1 <- r1.parse
      o2 <- r2.parse
    } yield o1.product(o2)

  def parse[R, A, B, C](implicit
    m: State[Int, *] |= R,
    r1: ExcelRowReads[R, A],
    r2: ExcelRowReads[R, B],
    r3: ExcelRowReads[R, C]
  ): Eff[R, ValidatedNel[ExcelParseError, (r1.Result, r2.Result, r3.Result)]] =
    for {
      o1 <- r1.parse
      o2 <- r2.parse
      o3 <- r3.parse
    } yield o1.product(o2).product(o3).map { case ((a, b), c) =>
      (a, b, c)
    }

  def parse[R, A, B, C, D](implicit
    m: State[Int, *] |= R,
    r1: ExcelRowReads[R, A],
    r2: ExcelRowReads[R, B],
    r3: ExcelRowReads[R, C],
    r4: ExcelRowReads[R, D]
  ): Eff[R, ValidatedNel[ExcelParseError, (r1.Result, r2.Result, r3.Result, r4.Result)]] =
    for {
      o1 <- r1.parse
      o2 <- r2.parse
      o3 <- r3.parse
      o4 <- r4.parse
    } yield o1.product(o2).product(o3).product(o4).map { case (((a, b), c), d) =>
      (a, b, c, d)
    }

  def parse[R, A, B, C, D, E](implicit
    m: State[Int, *] |= R,
    r1: ExcelRowReads[R, A],
    r2: ExcelRowReads[R, B],
    r3: ExcelRowReads[R, C],
    r4: ExcelRowReads[R, D],
    r5: ExcelRowReads[R, E]
  ): Eff[R, ValidatedNel[ExcelParseError, (r1.Result, r2.Result, r3.Result, r4.Result, r5.Result)]] =
    for {
      o1 <- r1.parse
      o2 <- r2.parse
      o3 <- r3.parse
      o4 <- r4.parse
      o5 <- r5.parse
    } yield o1.product(o2).product(o3).product(o4).product(o5).map { case ((((a, b), c), d), e) =>
      (a, b, c, d, e)
    }

  def parse[R, A, B, C, D, E, F](implicit
    m: State[Int, *] |= R,
    r1: ExcelRowReads[R, A],
    r2: ExcelRowReads[R, B],
    r3: ExcelRowReads[R, C],
    r4: ExcelRowReads[R, D],
    r5: ExcelRowReads[R, E],
    r6: ExcelRowReads[R, F]
  ): Eff[R, ValidatedNel[ExcelParseError, (r1.Result, r2.Result, r3.Result, r4.Result, r5.Result, r6.Result)]] =
    for {
      o1 <- r1.parse
      o2 <- r2.parse
      o3 <- r3.parse
      o4 <- r4.parse
      o5 <- r5.parse
      o6 <- r6.parse
    } yield o1.product(o2).product(o3).product(o4).product(o5).product(o6).map { case (((((a, b), c), d), e), f) =>
      (a, b, c, d, e, f)
    }
}
