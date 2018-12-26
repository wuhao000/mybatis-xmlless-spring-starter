package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.constant.Strings.LINE_BREAK_INDENT
import com.aegis.mybatis.xmlless.enums.Operations


/**
 * Created by 吴昊 on 2018/12/20.
 */
data class QueryCriteriaGroup(val criterion: MutableList<QueryCriteria> = mutableListOf()) {

  fun add(condition: QueryCriteria) {
    this.criterion.add(condition)
  }

  fun isEmpty(): Boolean {
    return criterion.isEmpty()
  }

  fun isNotEmpty(): Boolean {
    return criterion.isNotEmpty()
  }

  fun onlyDefaultEq(): Boolean {
    return criterion.map { it.operator }.toSet().onlyOrNull() == Operations.EqDefault
  }

  fun toSql(mappings: FieldMappings): String {
    val list = criterion.map { it.toSql(mappings) }
    return when {
      criterion.size > 1 -> criterion.first().wrapWithTests(
          "(\n\t" + trimCondition(criterion.joinToString(LINE_BREAK_INDENT) { it.toSqlWithoutTest(mappings) }) + "\n)"
      ) + " AND"
      else               -> list.first()
    }
  }

  private fun trimCondition(sql: String): String {
    return sql.trim().trim(" AND").trim(" OR")
  }

}
