package excelreads.row

object ExcelRowQuantifier {

  /** Parse `A` empty or fail
    */
  case class Optional[A]()

  /** Parse some `A`
    */
  case class Many[A]()

  /** Skip single row
    */
  case class Skip()

  /** Skip some empty rows
    */
  case class SkipOnlyEmpties()
}
