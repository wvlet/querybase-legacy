addDependencyTreePlugin

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-parser-combinators" % "always"

val AIRFRAME_VERSION = "21.7.0"
addSbtPlugin("org.xerial.sbt"     % "sbt-sonatype"  % "3.9.7")
addSbtPlugin("com.jsuereth"       % "sbt-pgp"       % "2.1.1")
addSbtPlugin("org.scoverage"      % "sbt-scoverage" % "1.8.2")
addSbtPlugin("com.eed3si9n"       % "sbt-buildinfo" % "0.10.0")
addSbtPlugin("org.scalameta"      % "sbt-scalafmt"  % "2.4.3")
addSbtPlugin("org.wvlet.airframe" % "sbt-airframe"  % AIRFRAME_VERSION)
addSbtPlugin("com.dwijnand"       % "sbt-dynver"    % "4.1.1")

// For Scala.js
addSbtPlugin("org.portable-scala"      % "sbt-scalajs-crossproject" % "1.1.0")
addSbtPlugin("org.scala-js"            % "sbt-scalajs"              % "1.6.0")
addSbtPlugin("ch.epfl.scala"           % "sbt-scalajs-bundler"      % "0.20.0")
libraryDependencies += "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.1.0"

addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

addSbtPlugin("org.xerial.sbt" % "sbt-pack" % "0.14")

// For accessing TD
addSbtPlugin("org.xerial.sbt" % "sbt-sql-td" % "0.13")

addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.2.22")

scalacOptions ++= Seq("-deprecation", "-feature")

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
