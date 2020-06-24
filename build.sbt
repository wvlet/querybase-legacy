val SCALA_2_12 = "2.12.11"
val SCALA_2_13 = "2.13.2"
val targetScalaVersions = SCALA_2_12 :: Nil

val AIRFRAME_VERSION = "20.6.1"

// Reload build.sbt on changes
Global / onChangedBuildSource := ReloadOnSourceChanges

// For using Scala 2.12 in sbt
scalaVersion in ThisBuild := SCALA_2_12
organization in ThisBuild := "org.wvlet.querybase"

// Use dynamic snapshot version strings for non tagged versions
dynverSonatypeSnapshots in ThisBuild := true
// Use coursier friendly version separator
dynverSeparator in ThisBuild := "-"

val buildSettings = Seq[Setting[_]](
  sonatypeProfileName := "org.wvlet",
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  homepage := Some(url("https://wvlet.org/querybase")),
  scmInfo := Some(
    ScmInfo(
      browseUrl = url("https://github.com/wvlet/querybase"),
      connection = "scm:git@github.com:wvlet/querybase.git"
    )
  ),
  developers := List(
    Developer(id = "leo", name = "Taro L. Saito", email = "leo@xerial.org", url = url("http://xerial.org/leo"))
  ),
  // Exclude compile-time only projects. This is a workaround for bloop,
  // which cannot resolve Optional dependencies nor compile-internal dependencie.
  crossScalaVersions := targetScalaVersions,
  crossPaths := true,
  publishMavenStyle := true,
  // Support JDK8 for Spark
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  scalacOptions ++= Seq("-feature", "-deprecation"), 
  // Use AirSpec for testing
  testFrameworks += new TestFramework("wvlet.airspec.Framework"),
  libraryDependencies ++= Seq(
    "org.scala-lang.modules" %%% "scala-collection-compat" % "2.1.6",
    "org.wvlet.airframe" % "airspec" % AIRFRAME_VERSION % Test
  )
)

publishTo in ThisBuild := sonatypePublishToBundle.value

val jsBuildSettings = Seq[Setting[_]](
  crossScalaVersions := targetScalaVersions,
  coverageEnabled := false
)

val noPublish = Seq(
  publishArtifact := false,
  publish := {},
  publishLocal := {}
)

lazy val querybase =
  project
    .in(file("."))
    .settings(name := "querybase")
    .settings(buildSettings)
    .settings(noPublish)
    .aggregate(apiJVM, apiJS, uiJS, server)
    
lazy val api = 
  crossProject(JVMPlatform, JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("querybase-api"))
    .settings(buildSettings)
    .settings(
      name := "querybase-api",
      description := "Querybase API",
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-reflect" % scalaVersion.value
      )
    )
    .jsSettings(jsBuildSettings)

lazy val apiJVM = api.jvm
lazy val apiJS = api.js

lazy val ui =
  crossProject(JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("querybase-ui"))
    .enablePlugins(ScalaJSBundlerPlugin)
    .settings(buildSettings)
    .settings(
      name := "querybase-ui",
      description := "UI for Querybase"
    )
    .jsSettings(
      jsBuildSettings,
      jsEnv in Test := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv()
//      npmDependencies in Test += "node" -> "12.14.1"
    )
    .dependsOn(api)

val uiJS = ui.js

lazy val server =
  project
    .in(file("querybase-server"))
    .enablePlugins(BuildInfoPlugin)
    .settings(buildSettings)
    .settings(
      name := "querybase-server",
      libraryDependencies ++= Seq(
        "org.wvlet.airframe"           %% "airframe"              % AIRFRAME_VERSION,
        "org.wvlet.airframe"           %% "airframe-config"       % AIRFRAME_VERSION,
        "org.wvlet.airframe"           %% "airframe-launcher"     % AIRFRAME_VERSION,
        "org.wvlet.airframe"           %% "airframe-http-finagle" % AIRFRAME_VERSION,
        "org.xerial"                   % "sqlite-jdbc"            % "3.32.3"
      )
    )
    .dependsOn(apiJVM)
