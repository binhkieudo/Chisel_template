ThisBuild / version          := "1.2-SNAPSHOT"
ThisBuild / organization     := "MyOrganization"

val chiselVersion = "7.0.0-RC1"

val verRegex = raw"""(\d+).(\d+).([\d+])(?:-(.*))?""".r
val chiselVer = try {
  chiselVersion match {
    case verRegex(major, minor, "+", null) => (major.toInt, minor.toInt, None, None)  // patch value "+" encoded as None
    case verRegex(major, minor, pat, null) => (major.toInt, minor.toInt, Some(pat.toInt), None)
    case verRegex(major, minor, pat, tag) => (major.toInt, minor.toInt, Some(pat.toInt), Some(tag))  // e.g., "-RC"
    case _ => throw new NumberFormatException()
  }  // TODO: Account for Chisel SNAPSHOT versions?
} catch {
    case _ : NumberFormatException => throw new MatchError(s"chiselVersion '$chiselVersion' has unexpected format.  " +
      "Should be <major>.<minor>.<patch>[-<tag>], where major, minor, and patch are integers.  A value of '+' for " +
      "patch is also accepted (provided that the tag field is empty).")
}
val patch: String = chiselVer._3.map(_.toString).getOrElse("+")

ThisBuild / scalaVersion := (chiselVer match {
  case (6, _, _, _) => "2.13.12"
  case (7, _, _, _) => "2.13.16"
  case _ => "2.13.+"
})

/** Generates a sequence of external library dependencies as per the chosen Chisel version. */
def libDeps: Seq[ModuleID] = {
  Seq(
    "org.chipsalliance" %% "chisel" % chiselVersion,
    "org.scalacheck" %% "scalacheck" % "1.14.3" % "test",
    "org.scalatest" %% "scalatest" % "3.2.16" % "test"
  )
}

/** Scala compiler options. */
def scalacOpts(scalaVer: String): Seq[String] = {
  Seq(
    "-language:reflectiveCalls",
    "-deprecation",
    "-feature",
    "-Xcheckinit",
    "-Ymacro-annotations",
  )
}

/** SBT main project point of entry: */
lazy val root = (project in file("."))
  .settings(
    name := "blink",
    libraryDependencies ++= libDeps,
    scalacOptions ++= scalacOpts(scalaVersion.value),
    addCompilerPlugin("org.chipsalliance" % "chisel-plugin" % chiselVersion cross CrossVersion.full)
  )
  
// Custom task to generate Verilog for SmuDma
lazy val generateVerilog = taskKey[Unit]("Generates Verilog...")
ThisBuild / generateVerilog := (runMain in Compile).toTask(" blink.Blink").value