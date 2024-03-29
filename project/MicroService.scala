import play.routes.compiler.StaticRoutesGenerator
import play.sbt.PlayImport.PlayKeys
import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt._
import scoverage.ScoverageKeys
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import uk.gov.hmrc.versioning.SbtGitVersioning
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

trait MicroService {

  import uk.gov.hmrc._
  import DefaultBuildSettings._
  import play.sbt.routes.RoutesKeys.routesGenerator

  val appName: String

  lazy val appDependencies: Seq[ModuleID] = Seq.empty
  lazy val playSettings: Seq[Setting[_]] = Seq.empty

  lazy val FuncTest = config("func") extend Test

  lazy val scoverageSettings: Seq[Def.Setting[_]] = {

    Seq(
      ScoverageKeys.coverageExcludedPackages := "<empty>;.*(Reverse|BuildInfo|Routes).*;" +
        "uk.gov.hmrc.r2.selfassessmentapi.config.*; uk.gov.hmrc.r2.selfassessmentapi.domain.*;" +
        "uk.gov.hmrc.r2.selfassessmentapi.services.*;" +
        "uk.gov.hmrc.selfassessmentapi.domain.*; uk.gov.hmrc.kenshoo.monitoring.*;",
      ScoverageKeys.coverageMinimum := 85,
      ScoverageKeys.coverageFailOnMinimum := true,
      ScoverageKeys.coverageHighlighting := true
    )
  }

  lazy val microservice = Project(appName, file("."))
    .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
    .settings(playSettings: _*)
    .settings(scalaSettings: _*)
    .settings(publishingSettings: _*)
    .settings(scoverageSettings: _*)
    .settings(defaultSettings(): _*)
    .settings(majorVersion := 0)
    .settings(
      targetJvm := "jvm-1.8",
      scalaVersion := "2.11.11",
      scalacOptions ++= Seq(
                            // FIXME: Uncomment fatal-warnings will make compilation fail because of usage of Play.current instead of DI
                            // Choose a DI strategy and uncomment
//                            "-Xfatal-warnings",
                            "-deprecation",
                            "-encoding",
                            "UTF-8",
                            "-unchecked",
                            "-language:postfixOps",
                            "-language:implicitConversions",
                            "-Ywarn-numeric-widen",
                            "-Yno-adapted-args"),
      libraryDependencies ++= appDependencies,
      parallelExecution in Test := false,
      fork in Test := true,
      javaOptions in Test += "-Dlogger.resource=logback-test.xml",
      retrieveManaged := true,
      evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
    )
    .configs(FuncTest)
    .settings(inConfig(FuncTest)(Defaults.testSettings): _*)
    .settings(Keys.fork in FuncTest := false,
              unmanagedSourceDirectories in FuncTest := Seq((baseDirectory in FuncTest).value / "func"),
              unmanagedClasspath in FuncTest += baseDirectory.value / "resources",
              unmanagedClasspath in Runtime += baseDirectory.value / "resources",
              unmanagedResourceDirectories in FuncTest += baseDirectory.value / "resources",
              unmanagedResourceDirectories in Compile += baseDirectory.value / "resources",
              addTestReportOption(FuncTest, "int-test-reports"),
              testGrouping in FuncTest := FuncTestPhases.oneForkedJvmPerTest((definedTests in FuncTest).value),
              parallelExecution in FuncTest := false,
              routesGenerator := StaticRoutesGenerator)
    .settings(resolvers += Resolver.bintrayRepo("hmrc", "releases"), resolvers += Resolver.jcenterRepo, resolvers += Resolver.sonatypeRepo("snapshots"))
    .settings(PlayKeys.playDefaultPort := 9778)
}

private object FuncTestPhases {

  def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
    tests map { test =>
      new Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name, "-Dlogger.resource=logback-test.xml"))))
    }
}
