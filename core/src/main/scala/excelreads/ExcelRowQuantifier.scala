package excelreads

object ExcelRowQuantifier {

  /** Parse `A` empty or fail
    */
  case class Optional[A]()

  /** Parse `A` over some rows
    */
  case class Many[A]()

  /** Skip single row
    */
  case class Skip()

  /** Skip some empty rows
    */
  case class SkipOnlyEmpties()

  /** Match at end of sheet
    *
    * @note
    *   This match the position, not data. So it doesn't advance to the next row. If you want to go next row, you have
    *   to use `Skip` manually.
    */
  case class End()
}
