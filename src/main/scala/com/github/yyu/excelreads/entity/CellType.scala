package com.github.yyu.excelreads.entity

import info.folone.scala.poi.{BooleanCell, Cell, DateCell, FormulaCell, NumericCell, StringCell, StyledCell}

/**
  * Excel's cell type
  */
sealed abstract class CellType(val value: String)

object CellType {
  sealed abstract class PrimitiveCellType(value: String) extends CellType(value)

  case object StringCellType extends PrimitiveCellType("StringCell")
  case object NumericCellType extends PrimitiveCellType("NumericCell")
  case object DateCellType extends PrimitiveCellType("DateCell")
  case object BooleanCellType extends PrimitiveCellType("BooleanCell")
  case object FormulaCellType extends PrimitiveCellType("FormulaCell")
  case object StyledCellType extends PrimitiveCellType("StyledCell")

  case class SeqCellType[A <: PrimitiveCellType](
    primitive: A
  ) extends CellType(s"Seq[${primitive.value}]")

  case class OptionCellType[A <: PrimitiveCellType](
    primitive: A
  ) extends CellType(s"Option[${primitive.value}]")

  def fromCell(cell: Cell): CellType = cell match {
    case _: StringCell => StringCellType
    case _: NumericCell => NumericCellType
    case _: DateCell => DateCellType
    case _: BooleanCell => BooleanCellType
    case _: FormulaCell => FormulaCellType
    case _: StyledCell => StyledCellType
  }
}