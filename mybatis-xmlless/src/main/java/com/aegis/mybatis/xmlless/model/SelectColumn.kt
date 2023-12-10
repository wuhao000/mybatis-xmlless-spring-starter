package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.resolver.ColumnsResolver
import java.lang.reflect.Type


/**
 * Created by 吴昊 on 2018/12/12.
 */
class SelectColumn(
    val table: TableName?,
    val column: String,
    alias: String?,
    val type: Type?
) {

  val alias = alias?.replace('.', '_')

  constructor(table: TableName, column: String) : this(table, column, null, null)

  fun toSql(): String {
    return when {
      table != null -> when {
        alias != null -> String.format(
            "%s.%s AS %s", table.getAliasOrName(),
            ColumnsResolver.wrapColumn(column), alias
        )

        else          -> String.format("%s.%s", table.getAliasOrName(), ColumnsResolver.wrapColumn(column))
      }

      else          -> when {
        alias != null -> String.format("%s AS %s", ColumnsResolver.wrapColumn(column), alias)
        else          -> ColumnsResolver.wrapColumn(column)
      }
    }
  }

  override fun toString(): String {
    return toSql()
  }
}
