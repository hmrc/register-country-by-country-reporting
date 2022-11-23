import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % "7.11.0"
  )

  val test = Seq(
    "org.scalatest"           %% "scalatest"                  % "3.2.10"      % Test,
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % "5.24.0"      % "test, it",
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.36.8"      % "test, it",
    "org.mockito"             %% "mockito-scala"              % "1.17.12"     % Test,
    "org.scalatestplus"       %% "scalatestplus-scalacheck"   % "3.1.0.0-RC2" % Test,
    "wolfendale"              %% "scalacheck-gen-regexp"      % "0.1.2"       % Test,
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0"       % Test,
    "com.github.tomakehurst"  %  "wiremock-standalone"        % "2.27.0"      % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.62.2"
  )
}
