package com.aegis.mybatis.xmlless.model


/**
 * Created by 吴昊 on 2018/12/12.
 */
data class SelectColumn(val table: String, val column: String, val alias: String? = null) {

  fun toSql(): String {
    if (alias != null) {
      return String.format("%s.%s AS %s", table, column, alias)
    } else {
      return String.format("%s.%s", table, column)
    }
  }

}
