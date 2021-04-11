name := "tetris"

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"

lazy val commonSettings = Seq(
  version := "0.2.0",
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
    webpack / version := "4.46.0",
    startWebpackDevServer / version := "3.11.2",
    webpackResources := baseDirectory.value / "webpack" * "*",
    packageJson := PackageJson.readFrom(baseDirectory.value / "package.json"),
    Compile / npmDependencies ++= packageJson.value.dependencies,
    Compile / npmDevDependencies ++= packageJson.value.devDependencies,
    fastOptJS / webpackConfigFile := Some(
      baseDirectory.value / "webpack" / "webpack-fast.config.js"
    ),
    fullOptJS / webpackConfigFile := Some(
      baseDirectory.value / "webpack" / "webpack-full.config.js"
    ),
    fastOptJS / webpackDevServerExtraArgs := Seq("--inline"),
    fastOptJS / webpackBundlingMode := BundlingMode.LibraryOnly(),
    Test / requireJsDomEnv := true
  )

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
