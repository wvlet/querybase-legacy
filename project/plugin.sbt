val AIRFRAME_VERSION = "21.3.1-20-dfa27cb9-SNAPSHOT"
addSbtPlugin("org.xerial.sbt"     % "sbt-sonatype"  % "3.9.5")
addSbtPlugin("com.jsuereth"       % "sbt-pgp"       % "2.1.1")
addSbtPlugin("org.scoverage"      % "sbt-scoverage" % "1.6.1")
addSbtPlugin("com.eed3si9n"       % "sbt-buildinfo" % "0.10.0")
addSbtPlugin("org.scalameta"      % "sbt-scalafmt"  % "2.4.2")
addSbtPlugin("org.wvlet.airframe" % "sbt-airframe"  % AIRFRAME_VERSION)
addSbtPlugin("com.dwijnand"       % "sbt-dynver"    % "4.1.1")

// For Scala.js
addSbtPlugin("org.portable-scala"      % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("org.scala-js"            % "sbt-scalajs"              % "1.5.0")
addSbtPlugin("ch.epfl.scala"           % "sbt-scalajs-bundler"      % "0.20.0")
libraryDependencies += "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.1.0"

addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

addSbtPlugin("org.xerial.sbt" % "sbt-pack" % "0.13")

// For accessing TD
addSbtPlugin("org.xerial.sbt" % "sbt-sql-td" % "0.13")

addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.2.18")

scalacOptions ++= Seq("-deprecation", "-feature")

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)
