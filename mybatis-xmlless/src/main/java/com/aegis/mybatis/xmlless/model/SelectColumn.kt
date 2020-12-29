package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.constant.SQLKeywords
import com.aegis.mybatis.xmlless.resolver.ColumnsResolver
import java.lang.reflect.Type
import javax.persistence.ColumnResult


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
        alias != null -> String.format("%s.%s AS %s", table, ColumnsResolver.wrapColumn(column), alias)
        else          -> String.format("%s.%s", table, ColumnsResolver.wrapColumn(column))
      }
      else          -> when {
        alias != null -> String.format("%s AS %s", ColumnsResolver.wrapColumn(column), alias)
        else          -> ColumnsResolver.wrapColumn(column)
      }
    }
  }

}
