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
}
