Excel-Reads
============================
![CI](https://github.com/y-yu/excel-reads/workflows/CI/badge.svg)

## Abstract

This is the Excel row(s) parser library using Scala macro.

## How to use

I recommend to use Apache POI version.

```scala
libraryDependencies += "com.github.y-yu" %% "excel-reads-apache-poi" % "0.5.1"
```

```scala
case class HelloWorld(
  hello: String,
  world: String
)

type R = Fx.fx3[Reader[ApachePoiSheet, *], State[Int, *], Either[ExcelParseErrors, *]]

val workbook = WorkbookFactory.create(
  new File("/test.xlsx")
)
val sheet = ApachePoiSheet(workbook.getSheet("Sheet1"))

val actual = ExcelSheetReads
  .parse[R, HelloWorld]
  .evalState(0)
  .runReader(sheet)
  .runEither
  .run

assert(actual == Right(HelloWorld("hello", "world")))
```

See also [tests](https://github.com/y-yu/excel-reads/tree/master/modules/apache-poi/src/test/scala/excelreads/apache/poi).

## References

- (Japanese) [Tagless-final + EffでExcelシートをパーズする](https://zenn.dev/yyu/articles/d7d965b661e158)
- (Japanese) [Tagless-final + EffなScalaによるExcelパーザー](https://zenn.dev/yyu/articles/61799662c042ac)
