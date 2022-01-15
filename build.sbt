import ReleaseTransformations._

import UpdateReadme.updateReadme
import sbt._
import Keys._
import org.scalafmt.sbt.ScalafmtPlugin.autoImport._

val defaultDependencyConfiguration = "test->test;compile->compile"

lazy val root =
  (project in file("."))
    .settings(
      name := "excel-reads",
      publishArtifact := false,
      publish := {},
      publishLocal := {},
      publish / skip := true
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
      libraryDependencies ++= Seq(
        "com.chuusai" %% "shapeless" % "2.3.7",
        "org.atnos" %% "eff" % "5.22.0",
        "org.scalatest" %% "scalatest" % "3.2.10" % "test"
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
        "info.folone" %% "poi-scala" % "0.20"
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
  scalaVersion := "2.13.8",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-Xlint:infer-any",
    "-Xsource:3",
    "-feature",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-unchecked",
    "-Ybackend-parallelism",
    "16"
  ),
  scalafmtOnCompile := true,
  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full)
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
    updateReadme,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("^ publishSigned"),
    setNextVersion,
    updateReadme,
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
