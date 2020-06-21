import ReleaseTransformations._
import UpdateReadme.updateReadme

lazy val root = (project in file("."))
  .settings(
    organization := "com.github.y-yu",
    name := "excel-reads",
    description := "A Excel file parser library using Scala macro",
    homepage := Some(url("https://github.com/y-yu")),
    licenses := Seq("MIT" -> url(s"https://github.com/y-yu/excel-reads/blob/master/LICENSE")),
    scalaVersion := "2.13.2",
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-Xlint",
      "-language:implicitConversions", "-language:higherKinds", "-language:existentials",
      "-unchecked"
    ),
    libraryDependencies ++= Seq(
      "com.chuusai" %% "shapeless" % "2.3.3",
      "info.folone" %% "poi-scala" % "0.19",
      "org.scalatest" %% "scalatest" % "3.1.2" % "test"
    )
  )
  .settings(publishSettings)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo := Some(
    if (isSnapshot.value)
      Opts.resolver.sonatypeSnapshots
    else
      Opts.resolver.sonatypeStaging
  ),
  publishArtifact in Test := false,
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
  s"v${if (releaseUseGlobalVersion.value) (version in ThisBuild).value else version.value}"
}

val tagOrHash = Def.setting {
  if (isSnapshot.value) sys.process.Process("git rev-parse HEAD").lineStream_!.head
  else tagName.value
}
