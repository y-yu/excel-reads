package excelreads.sym

import cats.Monad
import cats.data.ValidatedNel
import excelreads.exception.ExcelParseError

/** Basic getter interface from an Excel cell
  *
  * @note
  *   These methods return the `Option` value. In the Excel convention, it's very difficult to distinguish that the cell
  *   is empty or not. This optional design requires that its implementation need to care for the empty.
  */
abstract class ExcelBasicSYM[F[_]: Monad] {

  /** Get `String` data in the cell
    */
  def getString: F[Option[String]]

  /** Get `Double` data in the cell
    */
  def getDouble: F[Option[Double]]

  /** Get `Int` data in the cell
    */
  def getInt: F[Option[Int]]

  /** Get `Boolean` data in the cell
    */
  def getBoolean: F[Option[Boolean]]
}
