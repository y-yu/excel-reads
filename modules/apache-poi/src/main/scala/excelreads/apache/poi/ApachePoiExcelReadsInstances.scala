package excelreads.apache.poi

import cats.data.Reader
import cats.data.State
import excelreads.ExcelReads
import excelreads.ExcelReadsInstances
import excelreads.apache.poi.sym.ApachePoiExcelBasicSYM
import excelreads.apache.poi.sym.ApachePoiExcelStyleSYM
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Row
import org.atnos.eff.Eff
import org.atnos.eff.Member
import org.atnos.eff.NoFx
import org.atnos.eff.syntax.reader._

trait ApachePoiExcelReadsInstances
  extends ExcelReadsInstances[Row] {

  type Reads[A] = ApachePoiExcelReads[A]

  def fromInstance[A](implicit
    reads: ExcelReads[Row, A]
  ): Reads[A] = { row =>
    reads.parse(row)
  }

  private def runEffReader[R: Member.Aux[Reader[Row, *], *, NoFx], A](
    row: Row,
    eff: Eff[R, A]
  ): A =
    Eff.run(eff.runReader(row))

  implicit def stringInstance[R: Member.Aux[Reader[Row, *], *, NoFx]](implicit
    sym: ApachePoiExcelBasicSYM[R]
  ): ApachePoiExcelReads[Option[String]] = { row =>
    State(s => (s + 1, runEffReader(row, sym.getString(s))))
  }

  implicit def doubleInstance[R: Member.Aux[Reader[Row, *], *, NoFx]](implicit
    sym: ApachePoiExcelBasicSYM[R]
  ): ApachePoiExcelReads[Option[Double]] = { row =>
    State(s => (s + 1, runEffReader(row, sym.getDouble(s))))
  }

  implicit def intInstance[R: Member.Aux[Reader[Row, *], *, NoFx]](implicit
    sym: ApachePoiExcelBasicSYM[R]
  ): ApachePoiExcelReads[Option[Int]] = { row =>
    State(s => (s + 1, runEffReader(row, sym.getInt(s))))
  }

  implicit def booleanInstance[R: Member.Aux[Reader[Row, *], *, NoFx]](implicit
    sym: ApachePoiExcelBasicSYM[R]
  ): ApachePoiExcelReads[Option[Boolean]] = { row =>
    State(s => (s + 1, runEffReader(row, sym.getBoolean(s))))
  }

  implicit def styleInstance[R: Member.Aux[Reader[Row, *], *, NoFx]](implicit
    sym: ApachePoiExcelStyleSYM[R]
  ): ApachePoiExcelReads[Option[CellStyle]] = { row =>
    // Don't count up when getting the style
    State(s => (s, runEffReader(row, sym.getStyle(s))))
  }
}
