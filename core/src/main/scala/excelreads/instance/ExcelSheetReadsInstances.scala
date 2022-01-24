package excelreads.instance

import cats.implicits.*
import cats.data.State
import cats.data.Validated.Invalid
import cats.data.Validated.Valid
import cats.data.ValidatedNel
import excelreads.ExcelRowReads
import excelreads.ExcelRowQuantifier.*
import excelreads.ExcelSheetReads
import excelreads.exception.ExcelParseError
import excelreads.sym.ExcelRowSYM
import org.atnos.eff.Eff
import org.atnos.eff.state.*
import org.atnos.eff.syntax.all.*
import org.atnos.eff.|=
import excelreads.instance.ValidatedMonadInstance.*

trait ExcelSheetReadsInstances extends ExcelSheetReadsLowPriorityInstances {
  implicit def endInstance[Row, R, A](implicit
    sym: ExcelRowSYM[Row, _, Eff[R, *]]
  ): ExcelSheetReads[R, End] =
    ExcelSheetReads.from[R, End, Boolean] { implicit m =>
      for {
        s <- get
        isEmpty <- sym.isEmpty(s)
        isEnd <- sym.isEnd(s)
      } yield (isEmpty product isEnd) map { case (x, y) =>
        x && y
      }
    }

  implicit def skipInstance[Row, R, A]: ExcelSheetReads[R, Skip] =
    ExcelSheetReads.from[R, Skip, Unit] { implicit m =>
      for {
        s <- get
        _ <- put(s + 1)
      } yield Valid(())
    }

  implicit def skipOnlyEmptiesInstance[Row, R, A](implicit
    sym: ExcelRowSYM[Row, _, Eff[R, *]]
  ): ExcelSheetReads[R, SkipOnlyEmpties] = {
    def loop(
      skipLineCount: Int
    )(implicit
      m: State[Int, *] |= R
    ): Eff[R, ValidatedNel[ExcelParseError, Int]] =
      for {
        s <- get
        isEmpty <- sym.isEmpty(s)
        isEnd <- sym.isEnd(s)
        result <- Eff.flatTraverseA(
          (isEmpty product isEnd) map { case (x, y) =>
            x && !y // empty, NOT end
          }
        ) {
          case true => put(s + 1) >> loop(skipLineCount + 1)
          case false =>
            Eff.pure[R, ValidatedNel[ExcelParseError, Int]](Valid(skipLineCount))
        }
      } yield result

    ExcelSheetReads.from[R, SkipOnlyEmpties, Int] { implicit m =>
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
      m: State[Int, *] |= R
    ): Eff[R, ValidatedNel[ExcelParseError, Seq[A]]] = {
      val asResult = Eff.pure[R, ValidatedNel[ExcelParseError, Seq[A]]](Valid(as))

      for {
        s <- get
        isEmpty <- sym.isEmpty(s)
        result <- Eff.flatTraverseA(isEmpty) {
          case true =>
            asResult
          case false =>
            for {
              validationA <- sym.withRow(s, reads)
              result <- validationA match {
                case Valid(a) =>
                  put(s + 1) >> loop(as :+ a)
                case Invalid(_) =>
                  asResult
              }
            } yield result
        }
      } yield result
    }

    ExcelSheetReads.from[R, Many[A], Seq[A]] { implicit m =>
      loop(Seq.empty[A])
    }
  }

  implicit def optionalInstance[Row, R, U, A](implicit
    sym: ExcelRowSYM[Row, U, Eff[R, *]],
    reads: ExcelRowReads[U, A]
  ): ExcelSheetReads[R, Optional[A]] = {
    val emptyResult = Eff.pure[R, ValidatedNel[ExcelParseError, Option[A]]](Valid(None))

    ExcelSheetReads.from[R, Optional[A], Option[A]] { implicit m =>
      for {
        s <- get
        isEmpty <- sym.isEmpty(s)
        result <- Eff.flatTraverseA(isEmpty) {
          case true =>
            emptyResult
          case false =>
            for {
              validationA <- sym.withRow(s, reads)
              result <- validationA match {
                case Valid(a) =>
                  put(s + 1).map(_ => Valid(Some(a)): ValidatedNel[ExcelParseError, Option[A]])
                case Invalid(_) =>
                  emptyResult
              }
            } yield result
        }
      } yield result
    }
  }
}

trait ExcelSheetReadsLowPriorityInstances {
  implicit def aInstance[Row, R, U, A](implicit
    sym: ExcelRowSYM[Row, U, Eff[R, *]],
    reads: ExcelRowReads[U, A]
  ): ExcelSheetReads[R, A] =
    ExcelSheetReads.from[R, A, A] { implicit m =>
      for {
        s <- get
        validationA <- sym.withRow(s, reads)

        _ <- Eff.traverseA(validationA) { _ =>
          put(s + 1)
        }
      } yield validationA
    }
}
