package com.github.yyu.excelreads

import java.util.Date
import com.github.yyu.excelreads.entity.CellType.{BooleanCellType, DateCellType, NumericCellType, StringCellType}
import com.github.yyu.excelreads.entity.{CellType, RowWithSheetName}
import com.github.yyu.excelreads.exception.ExcelRowParseError
import com.github.yyu.excelreads.exception.ExcelRowParseError.{UnexpectedEmptyCell, UnexpectedTypeCell}
import info.folone.scala.poi.{BooleanCell, DateCell, NumericCell, StringCell}
import scalaz.{Failure, NonEmptyList, State, Success, Validation, ValidationNel}
import scalaz.syntax.traverse._
import scalaz.std.option._
import scalaz.std.stream.unfold
import shapeless.{::, Generic, HList, HNil, Lazy}

/**
  * A type-class to represent type `A` is created by parsing a row of Excel sheet.
  *
  * @tparam A a type that made by parsing Excel row
  *
  * @note `Int` given to the State monad represents Excel row's position.
  */
trait ExcelReads[A] {
  protected def parseState(rowWithSheetName: RowWithSheetName): State[Int, ValidationNel[ExcelRowParseError, A]]

  def read(rowWithSheetName: RowWithSheetName, initial: Int = 1): ValidationNel[ExcelRowParseError, A] =
    parseState(rowWithSheetName).eval(initial)

  def map[B](f: A => B): ExcelReads[B] = { rowWithSheetName =>
    parseState(rowWithSheetName).map(_.map(f))
  }
}

/**
  * Default instances.
  */
object ExcelReads {
  def apply[A](implicit p: ExcelReads[A]): ExcelReads[A] = p

  private def successNel[A](e: A): ValidationNel[ExcelRowParseError, A] =
    Validation.success[NonEmptyList[ExcelRowParseError], A](e)

  private def failureNel[A](e: ExcelRowParseError): ValidationNel[ExcelRowParseError, A] =
    Validation.failureNel(e)

  /**
    * For simpler, I made `Option[A]` parsers before `A` parsers.
    * So if you want to parse the type containing a type like `Option[Option[A]]`
    * then you have to make your instance.
    * In my opinion it doesn't make sense the nested `Option` parser so I didn't make it.
    */
  implicit val parserStringOption: ExcelReads[Option[String]] = { rowWithSheetName =>
    State { s =>
      (
        s + 1,
        rowWithSheetName.row.cells.find(_.index == s).map {
          case StringCell(_, data) =>
            successNel(data.trim)
          case cell =>
            failureNel[String](
              UnexpectedTypeCell(
                errorIndex = s,
                expectedCellType = StringCellType,
                actualCellType = CellType.fromCell(cell)
              )
            )
        }.sequence
      )
    }
  }

  implicit val parserIntOption: ExcelReads[Option[Int]] = { rowWithSheetName =>
    State { s =>
      (
        s + 1,
        rowWithSheetName.row.cells.find(_.index == s).map {
          case NumericCell(_, data) =>
            // This converting may not be suitable.
            // Even if `data` is larger than `Int.MaxValue` then
            // the result will be `Int.MaxValue`.
            successNel(data.toInt)
          case cell =>
            failureNel[Int](
              UnexpectedTypeCell(
                errorIndex = s,
                expectedCellType = NumericCellType,
                actualCellType = CellType.fromCell(cell)
              )
            )
        }.sequence
      )
    }
  }

  implicit val parserDoubleOption: ExcelReads[Option[Double]] = { rowWithSheetName =>
    State { s =>
      (
        s + 1,
        rowWithSheetName.row.cells.find(_.index == s).map {
          case NumericCell(_, data) =>
            successNel(data)
          case cell =>
            failureNel[Double](
              UnexpectedTypeCell(
                errorIndex = s,
                expectedCellType = NumericCellType,
                actualCellType = CellType.fromCell(cell)
              )
            )
        }.sequence
      )
    }
  }

  implicit val parserDateOption: ExcelReads[Option[Date]] = { rowWithSheetName =>
    State { s =>
      (
        s + 1,
        rowWithSheetName.row.cells.find(_.index == s).map {
          case DateCell(_, data) =>
            successNel(data)
          case cell =>
            failureNel[Date](
              UnexpectedTypeCell(
                errorIndex = s,
                expectedCellType = DateCellType,
                actualCellType = CellType.fromCell(cell)
              )
            )
        }.sequence
      )
    }
  }

  implicit val parserBooleanOption: ExcelReads[Option[Boolean]] = { rowWithSheetName =>
    State { s =>
      (
        s + 1,
        rowWithSheetName.row.cells.find(_.index == s).map {
          case BooleanCell(_, data) =>
            successNel(data)
          case cell =>
            failureNel[Boolean](
              UnexpectedTypeCell(
                errorIndex = s,
                expectedCellType = BooleanCellType,
                actualCellType = CellType.fromCell(cell)
              )
            )
        }.sequence
      )
    }
  }

  implicit def parseA[A](implicit R: ExcelReads[Option[A]]): ExcelReads[A] = { rowWithSheetName =>
    for {
      validation <- R.parseState(rowWithSheetName)
      s <- State.get[Int]
    } yield validation andThen {
      case Some(a) => Success(a)
      case None => failureNel(UnexpectedEmptyCell(s - 1))
    }
  }

  /**
    * This instance only can parse the type whose sequence is at the end.
    * If we want to parse any place on the type, it requires backtrack
    * like regular-expression matcher. It's hard to implement so
    * I haven't implemented it yet for now.
    */
  implicit def parserSeq[A](implicit R: ExcelReads[A]): ExcelReads[Seq[A]] = { rowWithSheetName =>
    val row = rowWithSheetName.row

    State { s =>
      val res: Seq[ValidationNel[ExcelRowParseError, A]] = unfold(s) { x =>
        val (next, value) = R.parseState(rowWithSheetName)(x)

        value match {
          case v @ Success(_) =>
            Some((v, next))
          case v @ Failure(_) =>
            if (row.cells.exists(_.index >= x))
              // Even if an error occurred at somewhere in the row,
              // it parses at the end to concat all errors.
              Some((v, next))
            else
              // Otherwise parsing is done.
              None
        }
      }

      (
        s + res.length,
        res.foldRight[ValidationNel[ExcelRowParseError, Seq[A]]](Success(Nil)) {
          (xv, acc) =>
            xv.ap(acc.map(xs => x => x +: xs))
        }
      )
    }
  }

  /**
    * This parser uses `HList` to parse the `case class`
    * which do not contain data types like neither `Either` nor ADT.
    *
    * @note `Either` and ADT are required [[scalaz.Coproduct]] but
    *       the parser sometimes cannot determine which type it should parse.
    *       That's the why I don't make `Coproduct` instances.
    *       If you use ADT in the type representing a Excel row,
    *       you have to implement a instance to parse it.
    */
  implicit val parserHNil: ExcelReads[HNil] = { _ =>
    State(s => (s, Success(HNil: HNil)))
  }

  implicit def parserHCons[H, T <: HList](
    implicit head: ExcelReads[H],
    tail: ExcelReads[T]
  ): ExcelReads[H :: T] = { rowWithSheetName =>
    for {
      hv <- head.parseState(rowWithSheetName)
      tv <- tail.parseState(rowWithSheetName)
    } yield
      hv.ap(tv.map(t => h => h :: t))
  }

  implicit def parserHList[A, L <: HList](
    implicit gen: Generic.Aux[A, L],
    parserHList: Lazy[ExcelReads[L]]
  ): ExcelReads[A] = { rowWithSheetName =>
    parserHList.value.parseState(rowWithSheetName).map(_.map(gen.from))
  }
}