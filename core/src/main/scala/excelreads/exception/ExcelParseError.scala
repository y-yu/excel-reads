package excelreads.exception

/** Base class of a parse error.
  */
abstract class ExcelParseError(
  errorIndex: Int,
  message: String,
  cause: Throwable
) extends Throwable(message, cause)
  with Product
  with Serializable

object ExcelParseError {

  /** Cell type error.
    *
    * If you won't give the `message` then make a default one automatically.
    */
  case class UnexpectedTypeCell(
    errorIndex: Int,
    actualCellType: String,
    message: Option[String] = None,
    cause: Throwable = null
  ) extends ExcelParseError(
      errorIndex = errorIndex,
      message = message.getOrElse(
        s"Expected type: actual type: $actualCellType."
      ),
      cause = cause
    )

  /** The cell is empty unexpectedly.
    */
  case class UnexpectedEmptyCell(
    errorIndex: Int,
    message: Option[String] = None,
    cause: Throwable = null
  ) extends ExcelParseError(
      errorIndex = errorIndex,
      message = message.getOrElse(
        s"The cell($errorIndex) is empty."
      ),
      cause = cause
    )
}
