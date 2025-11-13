import sbt._

object AppDependencies {
  private val bootstrapVersion = "10.4.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-30" % bootstrapVersion,
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test,
    "org.scalatestplus" %% "scalacheck-1-15" % "3.2.11.0" % Test,
    "org.scalamock" %% "scalamock" % "7.5.2" % Test,
    "io.github.wolfendale" %% "scalacheck-gen-regexp" % "1.1.0" % Test
  )

  val itDependencies: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion
  )
}
