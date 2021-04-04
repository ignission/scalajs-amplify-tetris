addSbtPlugin("org.scala-js"  % "sbt-scalajs"         % "1.5.1")
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.20.0")
addSbtPlugin("org.scalameta" % "sbt-scalafmt"        % "2.4.2")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix"        % "0.9.27")

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "upickle" % "1.2.2"
)
