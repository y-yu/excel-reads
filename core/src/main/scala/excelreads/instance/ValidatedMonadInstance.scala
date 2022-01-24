package excelreads.instance

import cats.MonadError
import cats.data.Validated
import cats.data.Validated.Invalid
import cats.data.Validated.Valid
import cats.implicits.*
import scala.annotation.tailrec

object ValidatedMonadInstance {

  /** This instance uses `Either` monad instance.
    *
    * @note
    *   In `ExcelReads`, we use `Validated` to represent errors since `Validated` is applicative. However in some cases,
    *   we have to do `Eff.flatTraverse` for `Validated` return value. That's the why this instance is defined.
    */
  implicit def validatedMonadInstance[E]: MonadError[Validated[E, *], E] = new MonadError[Validated[E, *], E] {
    override def pure[A](x: A): Validated[E, A] =
      Valid(x)

    override def flatMap[A, B](fa: Validated[E, A])(f: A => Validated[E, B]): Validated[E, B] =
      fa.toEither.flatMap(a => f(a).toEither).toValidated

    @tailrec
    override def tailRecM[A, B](a: A)(f: A => Validated[E, Either[A, B]]): Validated[E, B] =
      f(a) match {
        case Valid(Left(a)) => tailRecM(a)(f)
        case Valid(Right(b)) => Valid(b)
        case v @ Invalid(_) => v
      }

    override def raiseError[A](e: E): Validated[E, A] =
      Invalid(e)

    override def handleErrorWith[A](fa: Validated[E, A])(f: E => Validated[E, A]): Validated[E, A] =
      fa.toEither.handleErrorWith(e => f(e).toEither).toValidated
  }
}
