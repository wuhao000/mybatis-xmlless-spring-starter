package com.aegis.mybatis.xmlless.model.component

import com.aegis.kotlin.isNotNullAndNotBlank
import com.aegis.mybatis.xmlless.constant.Strings
import com.aegis.mybatis.xmlless.constant.WHERE
import com.aegis.mybatis.xmlless.model.QueryCriteria
import com.aegis.mybatis.xmlless.model.trim


/**
 *
 * @author 吴昊
 * @date 2023/12/6 10:23
 * @since v0.0.0
 * @version 1.0
 */
class WhereDeclaration(
    val criterion: List<List<QueryCriteria>>,
    private val groupBuilders: List<String>,
    val whereAppend: String?
) : ISqlPart {

  override fun toSql(): String {
    return when {
      criterion.isNotEmpty() || whereAppend.isNotNullAndNotBlank() ->
        String.format(
            WHERE, trimCondition(
            groupBuilders.joinToString(Strings.LINE_BREAK) +
                Strings.LINE_BREAK + (whereAppend ?: Strings.EMPTY)
        ).lines().joinToString(Strings.LINE_BREAK) {
          "\t".repeat(3) + it
        })

      else                                                         -> ""
    }
  }

  private fun trimCondition(sql: String): String {
    return sql.trim().trim(" AND").trim(" OR")
  }

}
