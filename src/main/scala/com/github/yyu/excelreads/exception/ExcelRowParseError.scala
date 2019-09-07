package com.github.yyu.excelreads.exception

import com.github.yyu.excelreads.entity.CellType

/**
  * Base class of a parse error.
  */
abstract class ExcelRowParseError(
  errorIndex: Int,
  message: String,
  cause: Throwable
) extends Throwable(message, cause) with Product

object ExcelRowParseError {

  /**
    * Cell type error.
    *
    * If you won't give the `message` then make a default one automatically.
    */
  case class UnexpectedTypeCell(
    errorIndex: Int,
    expectedCellType: CellType,
    actualCellType: CellType,
    message: Option[String] = None,
    cause: Throwable = null
  ) extends ExcelRowParseError(
    errorIndex = errorIndex,
    message = message.getOrElse(
      s"Expected type: ${expectedCellType.value} but actual type: ${actualCellType.value}."
    ),
    cause = cause
  )

  /**
    * The cell is empty unexpectedly.
    */
  case class UnexpectedEmptyCell(
    errorIndex: Int,
    message: Option[String] = None,
    cause: Throwable = null
  ) extends ExcelRowParseError(
    errorIndex = errorIndex,
    message = message.getOrElse(
      s"The cell($errorIndex) is empty."
    ),
    cause = cause
  )
}