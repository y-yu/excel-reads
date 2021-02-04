package excelreads.util

import cats.data.Reader
import org.atnos.eff.Eff
import org.atnos.eff.Fx
import org.atnos.eff.syntax.reader._

trait TestUtils {
  def runReader[Row, A](
    reader: Eff[Fx.fx1[Reader[Row, *]], A],
    row: Row
  ): A =
    Eff.run(reader.runReader(row))
}
