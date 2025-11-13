import uk.gov.hmrc.DefaultBuildSettings


val appName = "register-country-by-country-reporting"
ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.5"
val silencerVersion = "1.7.7"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    Compile / scalafmtOnCompile := true,
    Test / scalafmtOnCompile := true,
    PlayKeys.playDefaultPort := 10027,
    scalacOptions ++= Seq(
      "-release", "11",
      "-Wconf:src=routes/.*:s",
      "-Wconf:src=.*/Routes.scala:s",
      "-Wconf:src=.*/RoutesPrefix.scala:s",
      "-Wconf:src=.*/ReverseRoutes.scala:s",
      "-Wconf:src=.*/test/.*:s",
      "-Wconf:cat=deprecation:s"
    ),
    scalacOptions := scalacOptions.value.distinct
  )
  .settings(CodeCoverageSettings.settings *)
  .settings(ThisBuild / libraryDependencySchemes ++= Seq(
    "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
  ))

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(
    DefaultBuildSettings.itSettings(),
    scalacOptions := scalacOptions.value.distinct
  )
  .settings(libraryDependencies ++= AppDependencies.itDependencies)
