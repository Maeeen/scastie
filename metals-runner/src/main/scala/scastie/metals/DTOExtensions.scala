package scastie.metals

import java.nio.file.Path
import scala.meta.internal.metals.CompilerOffsetParams
import scala.reflect.internal.util.NoSourceFile

import com.olegych.scastie.api.ScastieOffsetParams

object DTOExtensions {

  extension (offsetParams: ScastieOffsetParams)

    def toOffsetParams: CompilerOffsetParams = {
      val noSourceFilePath = Path.of(NoSourceFile.path)

      val ident = "  "
      val wrapperObject = s"""|object worksheet {
                              |$ident""".stripMargin

      val (content, position) =
        if offsetParams.isWorksheetMode then
          val (userDirectives, userCode) = offsetParams.content.split("\n").span(_.startsWith("//>"))

          val userDirectivesEndingWithLR = if (userDirectives.size == 0) then "" else userDirectives.mkString("", "\n", "\n")

          val adjustedContent = s"""$userDirectivesEndingWithLR$wrapperObject${userCode.mkString("\n" + ident)}}"""

          val userDirectivesLength = userDirectives.map(_.length + 1).sum
          if (offsetParams.offset < userDirectivesLength) then
            // cursor is in directives
            (adjustedContent, offsetParams.offset)
          else

            val adjustedPosition = wrapperObject.length + offsetParams.offset + (userCode.length - 1) * ident.length
            // cursor is in code
            (adjustedContent, adjustedPosition)

        else (offsetParams.content, offsetParams.offset)

      new CompilerOffsetParams(noSourceFilePath.toUri, content, position)
    }

}
