package excelreads.scala.poi

import cats.data.Reader
import cats.data.State
import excelreads.ExcelReads
import excelreads.ExcelReadsInstances
import excelreads.scala.poi.sym.PoiScalaExcelBasicSYM
import info.folone.scala.poi.Row
import org.atnos.eff.Eff
import org.atnos.eff.Member
import org.atnos.eff.NoFx
import org.atnos.eff.syntax.reader._

trait PoiScalaExcelReadsInstances
  extends ExcelReadsInstances[Row] {

  type Reads[A] = PoiScalaExcelReads[A]

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
    sym: PoiScalaExcelBasicSYM[R]
  ): PoiScalaExcelReads[Option[String]] = { row =>
    State(s => (s + 1, runEffReader(row, sym.getString(s))))
  }

  implicit def doubleInstance[R: Member.Aux[Reader[Row, *], *, NoFx]](implicit
    sym: PoiScalaExcelBasicSYM[R]
  ): PoiScalaExcelReads[Option[Double]] = { row =>
    State(s => (s + 1, runEffReader(row, sym.getDouble(s))))
  }

  implicit def intInstance[R: Member.Aux[Reader[Row, *], *, NoFx]](implicit
    sym: PoiScalaExcelBasicSYM[R]
  ): PoiScalaExcelReads[Option[Int]] = { row =>
    State(s => (s + 1, runEffReader(row, sym.getInt(s))))
  }

  implicit def booleanInstance[R: Member.Aux[Reader[Row, *], *, NoFx]](implicit
    sym: PoiScalaExcelBasicSYM[R]
  ): PoiScalaExcelReads[Option[Boolean]] = { row =>
    State(s => (s + 1, runEffReader(row, sym.getBoolean(s))))
  }

}
