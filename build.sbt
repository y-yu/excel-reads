import ReleaseTransformations._

import sbt._
import Keys._
import org.scalafmt.sbt.ScalafmtPlugin.autoImport._

val defaultDependencyConfiguration = "test->test;compile->compile"

val scala213 = "2.13.8"
val scala3 = "3.1.1"

val isScala3 = Def.setting(
  CrossVersion.partialVersion(scalaVersion.value).exists(_._1 == 3)
)

lazy val root =
  (project in file("."))
    .settings(
      name := "excel-reads",
      publishArtifact := false,
      publish := {},
      publishLocal := {},
      publish / skip := true,
      addCommandAlias("SetScala3", s"++ $scala3!"),
      addCommandAlias("SetScala2", s"++ $scala213!")
    )
    .aggregate(
      core,
      poiScala,
      apachePoi
    )

lazy val core =
  (project in file("core"))
    .settings(
      name := "excel-reads-core",
      description := "A Excel file parser library core using Scala macro",
      libraryDependencies ++= {
        if (scalaBinaryVersion.value == "3") {
          Nil
        } else {
          Seq("com.chuusai" %% "shapeless" % "2.3.7")
        }
      },
      libraryDependencies ++= Seq(
        "org.atnos" %% "eff" % "5.22.0",
        "org.scalatest" %% "scalatest" % "3.2.11" % "test"
      )
    )
    .settings(baseSettings ++ publishSettings)

lazy val poiScala =
  (project in file("modules/poi-scala"))
    .settings(
      name := "excel-reads-poi-scala",
      description := "Excel reads poi scala implementation",
      Test / unmanagedResourceDirectories += baseDirectory.value / ".." / "resources",
      libraryDependencies ++= Seq(
        "info.folone" %% "poi-scala" % "0.20" cross CrossVersion.for3Use2_13
      )
    )
    .settings(baseSettings ++ publishSettings)
    .dependsOn(
      core % defaultDependencyConfiguration
    )

lazy val apachePoi =
  (project in file("modules/apache-poi"))
    .settings(
      name := "excel-reads-apache-poi",
      description := "Excel reads Apache POI implementation",
      Test / unmanagedResourceDirectories += baseDirectory.value / ".." / "resources",
      libraryDependencies ++= Seq(
        "org.apache.poi" % "poi" % "5.2.0",
        "org.apache.poi" % "poi-ooxml" % "5.2.0"
      )
    )
    .settings(baseSettings ++ publishSettings)
    .dependsOn(
      core % defaultDependencyConfiguration
    )

val baseSettings = Seq(
  organization := "com.github.y-yu",
  homepage := Some(url("https://github.com/y-yu")),
  licenses := Seq("MIT" -> url(s"https://github.com/y-yu/excel-reads/blob/master/LICENSE")),
  scalaVersion := scala213,
  crossScalaVersions := Seq(scala213, scala3),
  scalacOptions ++= {
    if (isScala3.value) {
      Seq(
        "-Ykind-projector",
        "-source",
        "3.0-migration"
      )
    } else {
      Seq(
        "-Xlint:infer-any",
        "-Xsource:3",
        "-Ybackend-parallelism",
        "16"
      )
    }
  },
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-unchecked"
  ),
  scalafmtOnCompile := !isScala3.value,
  libraryDependencies ++= {
    if (isScala3.value) {
      // Scala 3 uses `-Ykind-projector` compiler option instead of compiler plugin.
      Nil
    } else {
      Seq(
        compilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full)
      )
    }
  }
)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo := Some(
    if (isSnapshot.value)
      Opts.resolver.sonatypeSnapshots
    else
      Opts.resolver.sonatypeStaging
  ),
  Test / publishArtifact := false,
  pomExtra :=
    <developers>
      <developer>
        <id>y-yu</id>
        <name>Yoshimura Hikaru</name>
        <url>https://github.com/y-yu</url>
      </developer>
    </developers>
      <scm>
        <url>git@github.com:y-yu/excel-reads.git</url>
        <connection>scm:git:git@github.com:y-yu/excel-reads.git</connection>
        <tag>{tagOrHash.value}</tag>
      </scm>,
  releaseTagName := tagName.value,
  releaseCrossBuild := true,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("^ publishSigned"),
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"),
    pushChanges
  )
)

val tagName = Def.setting {
  s"v${if (releaseUseGlobalVersion.value) (ThisBuild / version).value else version.value}"
}

val tagOrHash = Def.setting {
  if (isSnapshot.value) sys.process.Process("git rev-parse HEAD").lineStream_!.head
  else tagName.value
}
