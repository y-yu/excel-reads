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

val row =
  Row(0) {
    Set(StringCell(0, "hello"), StringCell(1, "world"))
  }

val actual = ExcelReads[HelloWorld].eval(row)

assert(actual == Valid(HelloWorld("hello", "world")))
```

## How to use

```scala
libraryDependencies += "com.github.y-yu" %% "excel-reads-apache-poi" % "0.3.1"
```

or

```scala
libraryDependencies += "com.github.y-yu" %% "excel-reads-poi-scala" % "0.3.1"
```