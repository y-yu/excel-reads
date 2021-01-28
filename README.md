Excel-Reads
============================
![CI](https://github.com/y-yu/excel-reads/workflows/CI/badge.svg)

```scala
libraryDependencies += "com.github.y-yu" %% "excel-reads" % "0.1.0"
```

This is a Excel row parser library using Scala macro.

```scala
case class HelloWorld(
  hello: String,
  world: String
)

val rowWithSheetName = RowWithSheetName(
  "sheet",
  Row(1) {
    Set(StringCell(1, "hello"), StringCell(2, "world"))
  }
)

val actual = ExcelReads[HelloWorld].read(rowWithSheetName)

assert(actual == Success(HelloWorld("hello", "world")))
```
