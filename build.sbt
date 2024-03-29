val SCALA_2_12          = "2.12.12"
val SCALA_2_13          = "2.13.10"
val targetScalaVersions = SCALA_2_13 :: Nil

val AIRFRAME_VERSION    = "22.11.1"
val SCALAJS_DOM_VERSION = "2.4.0"
val SPARK_VERSION       = "3.0.1"
val TRINO_VERSION       = "405"

// Reload build.sbt on changes
Global / onChangedBuildSource := ReloadOnSourceChanges

// For using Scala 2.12 in sbt
ThisBuild / scalaVersion := SCALA_2_13
ThisBuild / organization := "org.wvlet.querybase"

// Use dynamic snapshot version strings for non tagged versions
ThisBuild / dynverSonatypeSnapshots := true
// Use coursier friendly version separator
ThisBuild / dynverSeparator := "-"

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
  crossPaths         := true,
  publishMavenStyle  := true,
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  scalacOptions ++= Seq("-feature", "-deprecation"),
  // Use AirSpec for testing
  testFrameworks += new TestFramework("wvlet.airspec.Framework"),
  libraryDependencies ++= Seq(
    "org.scala-lang.modules" %%% "scala-collection-compat" % "2.8.1",
    "org.wvlet.airframe"     %%% "airspec"                 % AIRFRAME_VERSION % Test
  ),
  resolvers ++= Resolver.sonatypeOssRepos("snapshots")
)

ThisBuild / publishTo := sonatypePublishToBundle.value

val jsBuildSettings = Seq[Setting[_]](
  crossScalaVersions := targetScalaVersions,
  coverageEnabled    := false
)

val noPublish = Seq(
  publishArtifact := false,
  publish         := {},
  publishLocal    := {}
)

lazy val jvmProjects = Seq[ProjectReference](apiJVM, server, apiClient, sql, frontendClientJVM, main)
lazy val jsProjects  = Seq[ProjectReference](apiJS, ui, frontendClientJS)

lazy val projectJVM = project.aggregate(jvmProjects: _*)
lazy val projectJS  = project.aggregate(jsProjects: _*)

lazy val querybase =
  project
    .in(file("."))
    .settings(name := "querybase")
    .settings(buildSettings)
    .settings(noPublish)
    .aggregate((jvmProjects ++ jsProjects): _*)

lazy val main =
  project
    .in(file("querybase-main"))
    .enablePlugins(PackPlugin)
    .settings(buildSettings)
    .settings(
      name := "querybase-main",
      packMain := Map(
        "querybase" -> "wvlet.querybase.server.QuerybaseServerMain"
      ),
      packResourceDir ++= Map(
        (ThisBuild / baseDirectory).value / "querybase-ui" / "src" / "main" / "public" -> "public",
        (Compile / managedResourceDirectories).value.head                              -> "public"
      ),
      Compile / resourceGenerators += Def.task {
        val assetFiles = (ui / Compile / fastOptJS / webpack).value
        val inputDir   = (ui / crossTarget).value / "scalajs-bundler" / "main"
        val outputDir  = (Compile / managedResourceDirectories).value.head
        assetFiles.flatMap { f =>
          val file = f.data
          file.relativeTo(inputDir).map { x: File =>
            val outFile = outputDir / x.getPath
            outFile.getParentFile.mkdirs()
            IO.copyFile(file, outFile)
            outFile
          }
        }
      }.taskValue
    )
    .dependsOn(server)

lazy val api =
  crossProject(JVMPlatform, JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("querybase-api"))
    .enablePlugins(BuildInfoPlugin)
    .settings(buildSettings)
    .settings(
      name        := "querybase-api",
      description := "Querybase API",
      libraryDependencies ++= Seq(
        "org.wvlet.airframe" %%% "airframe-http"    % AIRFRAME_VERSION,
        "org.wvlet.airframe" %%% "airframe-metrics" % AIRFRAME_VERSION,
        "org.scala-lang"       % "scala-reflect"    % scalaVersion.value
      ),
      buildInfoPackage := "wvlet.querybase.api",
      buildInfoKeys    := Seq[BuildInfoKey](name, version, scalaVersion, scalaBinaryVersion, sbtVersion)
    )
    .jsSettings(jsBuildSettings)

lazy val apiJVM = api.jvm
lazy val apiJS  = api.js

lazy val ui =
  project
    .in(file("querybase-ui"))
    .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
    .settings(buildSettings)
    .settings(jsBuildSettings)
    .settings(
      name        := "querybase-ui",
      description := "UI for Querybase",
      libraryDependencies ++= Seq(
        "org.wvlet.airframe" %%% "airframe"           % AIRFRAME_VERSION,
        "org.wvlet.airframe" %%% "airframe-rx-html"   % AIRFRAME_VERSION,
        "org.wvlet.airframe" %%% "airframe-rx-widget" % AIRFRAME_VERSION,
        "org.scala-js"       %%% "scalajs-dom"        % SCALAJS_DOM_VERSION
      ),
      scalaJSUseMainModuleInitializer              := true,
      Test / jsEnv                                 := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv(),
      Test / requireJsDomEnv                       := true,
      Compile / webpackConfigFile                  := Some(baseDirectory.value / "webpack.config.js"),
      Test / webpackConfigFile                     := Some(baseDirectory.value / "webpack-test.config.js"),
      startWebpackDevServer / version              := "4.11.1",
      Compile / npmDependencies += "monaco-editor" -> "0.21.3",
      Compile / npmDevDependencies ++= Seq(
        "import-loader"                -> "1.0.1",
        "expose-loader"                -> "1.0.0",
        "style-loader"                 -> "1.2.1",
        "file-loader"                  -> "6.1.0",
        "css-loader"                   -> "4.3.0",
        "monaco-editor-webpack-plugin" -> "2.1.0",
        "webpack-merge"                -> "5.8.0"
      ),
      useYarn := true,
      // webpackEmitSourceMaps := false,
      Compile / webpackBundlingMode := BundlingMode.LibraryOnly()
    )
    .dependsOn(frontendClientJS, apiJS)

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-parser-combinators" % "always"

lazy val server =
  project
    .in(file("querybase-server"))
    .settings(buildSettings)
    .settings(
      name := "querybase-server",
      // For using project root as a working folder
      reStart / baseDirectory := (ThisBuild / baseDirectory).value,
      libraryDependencies ++= Seq(
        "org.wvlet.airframe" %% "airframe"              % AIRFRAME_VERSION,
        "org.wvlet.airframe" %% "airframe-config"       % AIRFRAME_VERSION,
        "org.wvlet.airframe" %% "airframe-launcher"     % AIRFRAME_VERSION,
        "org.wvlet.airframe" %% "airframe-http-finagle" % AIRFRAME_VERSION,
        "org.wvlet.airframe" %% "airframe-http-grpc"    % AIRFRAME_VERSION,
        "org.wvlet.airframe" %% "airframe-jdbc"         % AIRFRAME_VERSION,
        "io.trino"            % "trino-cli"             % TRINO_VERSION,
        "io.trino"            % "trino-jdbc"            % TRINO_VERSION,
        "io.trino"            % "trino-spi"             % TRINO_VERSION,
        // Trino / Finagle uses different versions of Jackson.
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.4",
        "org.slf4j"                     % "slf4j-jdk14"          % "2.0.6",
        "org.xerial.snappy"             % "snappy-java"          % "1.1.9.1",
        "org.xerial"                    % "sqlite-jdbc"          % "3.40.0.0",
        "com.google.api-client"         % "google-api-client"    % "2.0.1",
        "com.google.api-client"         % "google-api-client"    % "1.35.2",
        "com.github.ben-manes.caffeine" % "caffeine"             % "3.1.2",
        // SQL formatter
        "com.github.vertical-blank" % "sql-formatter" % "2.0.3"
      )
    )
    .dependsOn(apiJVM, sql, store, apiClient, frontendClientJVM)

lazy val apiClient =
  project
    .in(file("querybase-api-client"))
    .enablePlugins(AirframeHttpPlugin)
    .settings(buildSettings)
    .settings(
      name                := "querybase-api-client",
      airframeHttpClients := Seq("wvlet.querybase.api.backend:grpc"),
      libraryDependencies ++= Seq(
        "org.wvlet.airframe" %% "airframe-http-grpc" % AIRFRAME_VERSION
      )
    )
    .dependsOn(apiJVM)

lazy val frontendClient =
  crossProject(JVMPlatform, JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("querybase-frontend-client"))
    .enablePlugins(AirframeHttpPlugin)
    .settings(buildSettings)
    .settings(
      name := "querybase-frontend-client"
    )
    .jvmSettings(
      airframeHttpClients := Seq("wvlet.querybase.api.frontend:rpc"),
      libraryDependencies ++= Seq(
        "org.wvlet.airframe" %% "airframe-http-finagle" % AIRFRAME_VERSION
      )
    )
    .jsSettings(
      airframeHttpClients := Seq("wvlet.querybase.api.frontend:scalajs"),
      libraryDependencies ++= Seq(
        "org.wvlet.airframe" %%% "airframe-http" % AIRFRAME_VERSION
      )
    )
    .dependsOn(api)

lazy val frontendClientJVM = frontendClient.jvm
lazy val frontendClientJS  = frontendClient.js

lazy val sql =
  project
    .in(file("querybase-sql"))
    .settings(buildSettings)
    .settings(
      name := "querybase-sql",
      libraryDependencies ++= Seq(
        "org.wvlet.airframe" %% "airframe"      % AIRFRAME_VERSION,
        "org.wvlet.airframe" %% "airframe-sql"  % AIRFRAME_VERSION,
        "org.wvlet.airframe" %% "airframe-jdbc" % AIRFRAME_VERSION
      )
    )
    .dependsOn(apiJVM)

lazy val store =
  project
    .in(file("querybase-store"))
    .settings(buildSettings)
    .settings(
      name        := "querybase-store",
      description := "querybase storage engine",
      libraryDependencies ++= Seq(
        "org.wvlet.airframe" %% "airframe"      % AIRFRAME_VERSION,
        "org.wvlet.airframe" %% "airframe-jdbc" % AIRFRAME_VERSION,
        "org.xerial"          % "sqlite-jdbc"   % "3.32.3"
      )
    )
    .dependsOn(apiJVM)

lazy val td =
  project
    .in(file("querybase-td"))
    .enablePlugins(SbtSQLTreasureData)
    .settings(buildSettings)
    .settings(
      name        := "querybase-td",
      description := "querybase log collector for Arm Treasure Data"
    )
    .dependsOn(apiJVM, store)
