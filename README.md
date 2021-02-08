Excel-Reads
============================
![CI](https://github.com/y-yu/excel-reads/workflows/CI/badge.svg)

## Abstract

This is the Excel row parser library using Scala macro.

```scala
case class HelloWorld(
  hello: String,
  world: String
)

type R = Fx.fx2[Reader[PoiScalaRow, *], State[Int, *]]

val row = PoiScalaRow(
  Row(0) {
    Set(StringCell(0, "hello"), StringCell(1, "world"))
  }
)

val actual = ExcelReads[R, HelloWorld]
  .parse
  .runReader(row)
  .evalState(0)
  .run

assert(actual == Valid(HelloWorld("hello", "world")))
```

## How to use

```scala
libraryDependencies += "com.github.y-yu" %% "excel-reads-apache-poi" % "0.4.0"
```

or

```scala
libraryDependencies += "com.github.y-yu" %% "excel-reads-poi-scala" % "0.4.0"
```