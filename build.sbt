name := "tetris"

addCommandAlias("fix", "all compile:scalafix; test:scalafix")
addCommandAlias("fixCheck", "; compile:scalafix --check; test:scalafix --check")
addCommandAlias("format", "; scalafmt; test:scalafmt; scalafmtSbt")
addCommandAlias("formatCheck", "; scalafmtCheck; test:scalafmtCheck; scalafmtSbtCheck")
addCommandAlias("fixAll", "fix; format")
addCommandAlias("checkAll", "fixCheck; formatCheck")
addCommandAlias("devStart", "fastOptJS::startWebpackDevServer")
addCommandAlias("devStop", "fastOptJS::stopWebpackDevServer")
addCommandAlias("dev", "~devStart")
addCommandAlias("dist", "fixAll; fullOptJS::webpack")

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"

lazy val commonSettings = Seq(
  version := "0.1.0",
  scalaVersion := "2.13.5",
  scalacOptions ++= List(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Yrangepos",
    "-Ymacro-annotations",
    "-Ywarn-unused",
    "-Xlint",
    "-Xfatal-warnings"
  ),
  // scalafix
  addCompilerPlugin(scalafixSemanticdb),
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision
)

lazy val packageJson = settingKey[PackageJson]("package.json")

lazy val root = (project in file("."))
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "1.1.0"
    )
  )
  .settings(
    useYarn := true,
    version in webpack := "4.46.0",
    version in startWebpackDevServer := "3.11.2",
    webpackResources := baseDirectory.value / "webpack" * "*",
    packageJson := PackageJson.readFrom(baseDirectory.value / "package.json"),
    npmDependencies in Compile ++= packageJson.value.dependencies,
    npmDevDependencies in Compile ++= packageJson.value.devDependencies,
    webpackConfigFile in fastOptJS := Some(
      baseDirectory.value / "webpack" / "webpack-fast.config.js"
    ),
    webpackConfigFile in fullOptJS := Some(
      baseDirectory.value / "webpack" / "webpack-full.config.js"
    ),
    webpackDevServerExtraArgs in fastOptJS := Seq("--inline"),
    webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly(),
    requireJsDomEnv in Test := true
  )
