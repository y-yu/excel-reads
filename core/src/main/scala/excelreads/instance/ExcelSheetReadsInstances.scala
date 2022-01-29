package excelreads.instance

import cats.implicits.*
import cats.data.State
import excelreads.ExcelRowReads
import excelreads.ExcelRowQuantifier.*
import excelreads.ExcelSheetReads
import excelreads.exception.ExcelParseError.ExcelParseErrors
import excelreads.sym.ExcelRowSYM
import org.atnos.eff.Eff
import org.atnos.eff.state.*
import org.atnos.eff.either.*
import org.atnos.eff.syntax.all.*
import org.atnos.eff.|=

trait ExcelSheetReadsInstances extends ExcelSheetReadsLowPriorityInstances {
  implicit def endInstance[Row, R, A](implicit
    sym: ExcelRowSYM[Row, _, Eff[R, *]]
  ): ExcelSheetReads[R, End] =
    ExcelSheetReads.from[R, End, Boolean] { implicit m1 => implicit m2 =>
      for {
        isEmptyAndEnd <- sym.isEmpty product sym.isEnd
      } yield isEmptyAndEnd._1 && isEmptyAndEnd._2
    }

  implicit def skipInstance[Row, R, A]: ExcelSheetReads[R, Skip] =
    ExcelSheetReads.from[R, Skip, Unit] { implicit m1 => implicit m2 =>
      for {
        s <- get
        _ <- put(s + 1)
      } yield ()
    }

  implicit def skipOnlyEmptiesInstance[Row, R, A](implicit
    sym: ExcelRowSYM[Row, _, Eff[R, *]]
  ): ExcelSheetReads[R, SkipOnlyEmpties] = {
    def loop(
      skipLineCount: Int
    )(implicit
      m1: State[Int, *] |= R,
      m2: Either[ExcelParseErrors, *] |= R
    ): Eff[R, Int] =
      for {
        s <- get
        isEmptyAndEnd <- sym.isEmpty product sym.isEnd
        result <-
          if (isEmptyAndEnd._1 && !isEmptyAndEnd._2) {
            put(s + 1) >> loop(skipLineCount + 1)
          } else {
            Eff.pure[R, Int](skipLineCount)
          }
      } yield result

    ExcelSheetReads.from[R, SkipOnlyEmpties, Int] { implicit m1 => implicit m2 =>
      loop(0)
    }
  }

  implicit def manyInstance[Row, R, U, A](implicit
    sym: ExcelRowSYM[Row, U, Eff[R, *]],
    reads: ExcelRowReads[U, A]
  ): ExcelSheetReads[R, Many[A]] = {
    // This function is NOT tail recursive!
    def loop(
      as: Seq[A]
    )(implicit
      m1: State[Int, *] |= R,
      m2: Either[ExcelParseErrors, *] |= R
    ): Eff[R, Seq[A]] =
      for {
        s <- get
        isEmpty <- sym.isEmpty
        result <-
          if (isEmpty) {
            as.pureEff[R]
          } else {
            for {
              ae <- sym.withRow(reads)
              result <- Eff.traverseA(ae) { a =>
                put(s + 1) >> loop(as :+ a)
              }
            } yield result.fold(
              { _ => as },
              identity
            )
          }
      } yield result

    ExcelSheetReads.from[R, Many[A], Seq[A]] { implicit m1 => implicit m2 =>
      loop(Seq.empty[A])
    }
  }

  implicit def optionalInstance[Row, R, U, A](implicit
    sym: ExcelRowSYM[Row, U, Eff[R, *]],
    reads: ExcelRowReads[U, A]
  ): ExcelSheetReads[R, Optional[A]] =
    ExcelSheetReads.from[R, Optional[A], Option[A]] { implicit m1 => implicit m2 =>
      for {
        s <- get
        isEmpty <- sym.isEmpty
        result <-
          if (isEmpty) {
            Option.empty[A].pureEff[R]
          } else {
            for {
              ae <- sym.withRow(reads)
              result <- Eff.traverseA(ae) { a =>
                put(s + 1).map(_ => a)
              }
            } yield result.fold(
              _ => Option.empty[A],
              x => Option(x)
            )
          }
      } yield result
    }
}

trait ExcelSheetReadsLowPriorityInstances {
  implicit def aInstance[Row, R, U, A](implicit
    sym: ExcelRowSYM[Row, U, Eff[R, *]],
    reads: ExcelRowReads[U, A]
  ): ExcelSheetReads[R, A] =
    ExcelSheetReads.from[R, A, A] { implicit m1 => implicit m2 =>
      for {
        s <- get
        a <- sym.withRow(reads).flatMap(fromEither[R, ExcelParseErrors, A])
        _ <- put(s + 1)
      } yield a
    }
}
