package com.aegis.mybatis.xmlless.constant

/**
 *
 * Created by 吴昊 on 2018/12/19.
 *
 * @author 吴昊
 * @since 1.4.3
 */
object SQLKeywords {

  fun getValues(): List<String> {
    return listOf(
        "ADD", "ALTER", "AND", "AS",
        "BACKUP", "BETWEEN", "BY",
        "CASE", "COMMENT", "DEFAULT", "DESC",
        "EXISTS", "FROM", "GROUP", "INNER",
        "JOIN", "KEY", "LEFT", "LIMIT",
        "NEW", "OR", "ORDER",
        "PRIMARY", "RIGHT", "ROW",
        "SELECT", "SET", "SHARE",
        "TABLE", "TEXT", "TIME", "USER",
        "VIEW", "WHEN", "WHERE"
    )
  }

}
