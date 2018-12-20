package com.aegis.mybatis.xmlless.model

import java.lang.reflect.Type


/**
 * Created by 吴昊 on 2018/12/12.
 */
data class SelectColumn(val table: String?,
                        val column: String,
                        val alias: String?,
                        val type: Type?) {

  constructor(table: String, column: String) : this(table, column, null, null)

  fun toSql(): String {
    return when {
      table != null -> when {
        alias != null -> String.format("%s.%s AS %s", table, column, alias)
        else          -> String.format("%s.%s", table, column)
      }
      else          -> when {
        alias != null -> String.format("%s AS %s", column, alias)
        else          -> column
      }
    }
  }

}
