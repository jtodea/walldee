import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName = "walldee"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "com.h2database" % "h2" % "1.3.167",

    "org.jfree" % "jfreechart" % "1.0.14",
    "org.squeryl" %% "squeryl" % "0.9.5",
    "org.scalaquery" %% "scalaquery" % "0.10.0-M1"
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    // Add your own project settings here
  )

}
