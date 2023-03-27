package scastie.metals

import scala.jdk.CollectionConverters._
import com.virtuslab.using_directives.custom.utils.Source
import com.virtuslab.using_directives.config.Settings
import com.virtuslab.using_directives.Context
import com.virtuslab.using_directives.reporter.PersistentReporter
import com.virtuslab.using_directives.custom.Parser
import com.virtuslab.using_directives.custom.utils.ast.UsingDef

object ScalaCliParser {

  def parse(string: String) =
    val source = new Source(string.toCharArray())
    val reporter = new PersistentReporter()
    val ctx = new Context(reporter)
    val parser = new Parser(source, ctx)
    
    val defs = parser.parse()
    defs.getUsingDefs().asScala.toList    

}
