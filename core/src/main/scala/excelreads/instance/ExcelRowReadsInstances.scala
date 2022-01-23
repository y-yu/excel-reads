package excelreads.instance

import cats.implicits.*
import cats.data.State
import cats.data.Validated.Invalid
import cats.data.Validated.Valid
import cats.data.ValidatedNel
import excelreads.ExcelReads
import excelreads.row.ExcelRowQuantifier.*
import excelreads.ExcelRowReads
import excelreads.exception.ExcelParseError
import excelreads.sym.ExcelRowSYM
import org.atnos.eff.Eff
import org.atnos.eff.state.*
import org.atnos.eff.syntax.all.*
import org.atnos.eff.|=

trait ExcelRowReadsInstances extends ExcelRowReadsLowPriorityInstances {
  implicit def skipInstance[Row, R, A](implicit
    sym: ExcelRowSYM[Row, _, Eff[R, *]]
  ): ExcelRowReads[R, Skip] = {
    ExcelRowReads.from[R, Skip, Unit] { implicit m =>
      for {
        s <- get
        _ <- put(s + 1)
      } yield Valid(())
    }
  }

  implicit def skipOnlyEmptiesInstance[Row, R, A](implicit
    sym: ExcelRowSYM[Row, _, Eff[R, *]]
  ): ExcelRowReads[R, SkipOnlyEmpties] = {
    def loop(
      skipLineCount: Int
    )(implicit
      m: State[Int, *] |= R
    ): Eff[R, ValidatedNel[ExcelParseError, Int]] =
      for {
        s <- get
        isEmpty <- sym.isEmpty(s)
        result <- Eff.traverseA(isEmpty) {
          case true => put(s + 1) >> loop(skipLineCount + 1)
          case false =>
            Eff.pure[R, ValidatedNel[ExcelParseError, Int]](Valid(skipLineCount))
        }
      } yield result.map(_.toEither).toEither.flatten.toValidated

    ExcelRowReads.from[R, SkipOnlyEmpties, Int] { implicit m =>
      loop(0)
    }
  }

  implicit def manyInstance[Row, R, U, A](implicit
    sym: ExcelRowSYM[Row, U, Eff[R, *]],
    reads: ExcelReads[U, A]
  ): ExcelRowReads[R, Many[A]] = {
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
        result <- Eff.traverseA(isEmpty) {
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
      } yield result.map(x => x.toEither).toEither.flatten.toValidated // HELP!
    }

    ExcelRowReads.from[R, Many[A], Seq[A]] { implicit m =>
      loop(Seq.empty[A])
    }
  }

  implicit def optionalInstance[Row, R, U, A](implicit
    sym: ExcelRowSYM[Row, U, Eff[R, *]],
    reads: ExcelReads[U, A]
  ): ExcelRowReads[R, Optional[A]] = {
    val emptyResult = Eff.pure[R, ValidatedNel[ExcelParseError, Option[A]]](Valid(None))

    ExcelRowReads.from[R, Optional[A], Option[A]] { implicit m =>
      for {
        s <- get
        isEmpty <- sym.isEmpty(s)
        result <- Eff.traverseA(isEmpty) {
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
      } yield result.map(x => x.toEither).toEither.flatten.toValidated
    }
  }
}

trait ExcelRowReadsLowPriorityInstances {
  implicit def aInstance[Row, R, U, A](implicit
    sym: ExcelRowSYM[Row, U, Eff[R, *]],
    reads: ExcelReads[U, A]
  ): ExcelRowReads[R, A] =
    ExcelRowReads.from[R, A, A] { implicit m =>
      for {
        s <- get
        validationA <- sym.withRow(s, reads)
        _ <- put(s + 1)
      } yield validationA
    }
}
