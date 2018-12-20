package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.constant.Strings.LINE_BREAK_INDENT


/**
 * Created by 吴昊 on 2018/12/20.
 */
class ConditionGroup(val conditions: MutableList<Condition> = mutableListOf()) {

  fun add(condition: Condition) {
    this.conditions.add(condition)
  }

  fun isEmpty(): Boolean {
    return conditions.isEmpty()
  }

  fun isNotEmpty(): Boolean {
    return conditions.isNotEmpty()
  }

  fun toSql(mappings: FieldMappings): String {
    val list = conditions.map { it.toSql(mappings) }
    return when {
      conditions.size > 1 -> conditions.first().wrapWithTests(
          "(\n\t" + trimCondition(conditions.joinToString(LINE_BREAK_INDENT) { it.toSqlWithoutTest(mappings) }) + "\n)"
      )
      else                -> list.first()
    }
  }

  private fun trimCondition(sql: String): String {
    return sql.trim().trim(" AND").trim(" OR")
  }

}
