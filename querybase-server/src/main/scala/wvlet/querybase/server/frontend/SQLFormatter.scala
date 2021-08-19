package wvlet.querybase.server.frontend

import com.github.vertical_blank.sqlformatter.core.FormatConfig

/** */
object SQLFormatter {

  private val formatter = {
    com.github.vertical_blank.sqlformatter.SqlFormatter
      .extend(
        // Add lambda -> operator
        _.plusOperators("->")
        // ARRAY[....]
          .plusOpenParens("[").plusCloseParens("]")
      )
  }

  private val formatConfig = FormatConfig
    .builder()
    .maxColumnLength(100)
    .build()

  def format(sql: String): String = {
    formatter.format(sql, formatConfig)
  }
}
