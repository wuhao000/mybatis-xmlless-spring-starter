package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.constant.Strings.LINE_BREAK_INDENT


/**
 * Created by 吴昊 on 2018/12/20.
 */
data class QueryCriteriaGroup(private val criterion: List<QueryCriteria> = mutableListOf()) {

  companion object {
    const val CONDITION_GROUP_TEMPLATE = "(\n\t%s\n\t) AND"
  }

  fun isEmpty(): Boolean {
    return criterion.isEmpty()
  }

  fun isNotEmpty(): Boolean {
    return criterion.isNotEmpty()
  }


  fun toSql(wrapWithTest: Boolean): String {
    return when {
      criterion.size > 1 -> {
        val c = criterion.first()
        val criteriaInfoList = c.getCriteriaList()
        if (criteriaInfoList.isNotEmpty()) {
          criteriaInfoList.joinToString("\n") { criteriaInfo ->
            createConditionGroupSql(c, criteriaInfo, wrapWithTest)
          }
        } else {
          createConditionGroupSql(c, null, wrapWithTest)
        }
      }

      else               -> {
        if (wrapWithTest) {
          criterion.first().toSql()
        } else {
          criterion.first().toSqlWithoutTest(null)
        }
      }
    }
  }

  private fun createConditionGroupSql(
      criteria: QueryCriteria,
      criteriaInfo: CriteriaInfo?,
      wrapWithTest: Boolean
  ): String {
    val sql = CONDITION_GROUP_TEMPLATE.format(
        trimCondition(criterion.joinToString(LINE_BREAK_INDENT) {
          it.toSqlWithoutTest(criteriaInfo)
        })
    )
    if (wrapWithTest) {
      return criteria.wrapWithTests(sql, criteriaInfo)
    }
    return sql
  }

  private fun trimCondition(sql: String): String {
    return sql.trim().trim(" AND").trim(" OR")
  }

}
