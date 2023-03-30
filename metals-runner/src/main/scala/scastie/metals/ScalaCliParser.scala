package scastie.metals

import scala.jdk.CollectionConverters._
import com.virtuslab.using_directives.custom.utils.Source
import com.virtuslab.using_directives.config.Settings
import com.virtuslab.using_directives.Context
import com.virtuslab.using_directives.reporter.PersistentReporter
import com.virtuslab.using_directives.custom.Parser
import com.virtuslab.using_directives.custom.utils.ast.UsingDef
import com.virtuslab.using_directives.reporter.ConsoleReporter
import com.virtuslab.using_directives.custom.SimpleCommentExtractor
import com.olegych.scastie.api.ScalaTarget
import com.olegych.scastie.api.FailureType
import com.olegych.scastie.api.InvalidScalaVersion
import com.olegych.scastie.buildinfo.BuildInfo

object ScalaCliParser {

  def getScliDirectives(string: String) =
    SimpleCommentExtractor(string.toCharArray(), true).extractComments()

  def parse(string: String) =
    val source = new Source(getScliDirectives(string))
    val reporter = new PersistentReporter()
    val ctx = new Context(reporter)
    val parser = new Parser(source, ctx)
    
    val defs = parser.parse().getUsingDefs().asScala.toList 
    val allDefs = defs.flatMap(_.getSettingDefs().getSettings().asScala)

    println(s"defs $allDefs")
    println(s"defs type ${allDefs.map(_.getValue().getClass())}")
    allDefs

  def getScalaTarget(string: String): Either[ScalaTarget, FailureType] = {
    // TODO: get it correctly
    val defs = parse(string).groupMap(_.getKey())(_.getValue().toString())

    // get the scala version
    var scalaVersion = defs.get("scala").getOrElse(List(BuildInfo.latest3)).head

    if (scalaVersion == "3") then scalaVersion = BuildInfo.latest3
    else if (scalaVersion == "2") then scalaVersion = BuildInfo.latest213

    // now we have the scala version
    // get the target
    val scalaTarget: Either[ScalaTarget, FailureType] =
      if (scalaVersion.startsWith("3")) then
        Left(ScalaTarget.Scala3(scalaVersion = scalaVersion))
      else if (scalaVersion.startsWith("2")) then
        Left(ScalaTarget.Jvm(scalaVersion = scalaVersion))
      else
        Right(InvalidScalaVersion(s"Invalid provided Scala version ${scalaVersion}"))

    val dependencies = defs.get("dep").getOrElse(List()) ++ defs.get("lib").getOrElse(List())

    println(s"dependencies $dependencies")

    scalaTarget
  }

}
