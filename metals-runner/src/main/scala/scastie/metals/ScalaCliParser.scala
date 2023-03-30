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

object ScalaCliParser {

  def getScliDirectives(string: String) =
    SimpleCommentExtractor(string.toCharArray(), true).extractComments()

  def parse(string: String) =
    val source = new Source(getScliDirectives(string))
    val reporter = new PersistentReporter()
    val ctx = new Context(reporter)
    val parser = new Parser(source, ctx)
    
    val defs = parser.parse()
    println(s"defs $defs")
    defs.getUsingDefs().asScala.toList    

}
