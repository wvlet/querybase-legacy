// Ignore binary incompatible errors for libraries using scala-xml.
// sbt-scoverage upgraded to scala-xml 2.1.0, but other sbt-plugins and Scala compilier 2.12 uses scala-xml 1.x.x
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % "always"

addDependencyTreePlugin

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-parser-combinators" % "always"

val AIRFRAME_VERSION = "22.11.1"
addSbtPlugin("org.xerial.sbt"     % "sbt-sonatype"  % "3.9.15")
addSbtPlugin("com.github.sbt"     % "sbt-pgp"       % "2.2.0")
addSbtPlugin("org.scoverage"      % "sbt-scoverage" % "2.0.6")
addSbtPlugin("com.eed3si9n"       % "sbt-buildinfo" % "0.11.0")
addSbtPlugin("org.scalameta"      % "sbt-scalafmt"  % "2.5.0")
addSbtPlugin("org.wvlet.airframe" % "sbt-airframe"  % AIRFRAME_VERSION)
addSbtPlugin("com.dwijnand"       % "sbt-dynver"    % "4.1.1")

// For Scala.js
addSbtPlugin("org.portable-scala"      % "sbt-scalajs-crossproject" % "1.2.0")
addSbtPlugin("org.scala-js"            % "sbt-scalajs"              % "1.13.1")
addSbtPlugin("ch.epfl.scala"           % "sbt-scalajs-bundler"      % "0.21.1")
libraryDependencies += "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.1.0"

addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

addSbtPlugin("org.xerial.sbt" % "sbt-pack" % "0.17")

// For accessing TD
addSbtPlugin("org.xerial.sbt" % "sbt-sql-td" % "0.13")

addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.3.6")

scalacOptions ++= Seq("-deprecation", "-feature")

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
