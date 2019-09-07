package com.github.yyu.excelreads.entity

import info.folone.scala.poi.Row

/**
  * A tuple of Excel sheet name and a row.
  */
case class RowWithSheetName(
  sheetName: String,
  row: Row
)
