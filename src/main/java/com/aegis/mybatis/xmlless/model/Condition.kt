package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.annotations.ValueAssign
import com.aegis.mybatis.xmlless.config.Operations
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure

/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
data class Condition(val property: String,
                     var operator: Operations,
                     var append: String? = null,
                     var paramName: String?,
                     var parameter: KParameter?,
                     val specificValue: ValueAssign?) {

  companion object {
    /**  foreach模板 */
    const val FOREACH = """<foreach collection="%s" item="item" separator=", " open="(" close=")">
  #{item}
</foreach>"""
  }

  fun toSql(mappings: FieldMappings): BuildSqlResult {
    val sqlBuilder = toSqlWithoutTest(mappings)
    if (sqlBuilder.sql == null) {
      return sqlBuilder
    }
    return wrapWithTests(sqlBuilder.sql)
  }

  fun toSqlWithoutTest(mappings: FieldMappings): BuildSqlResult {
    val columnResult = mappings.resolveColumnByPropertyName(property)
    if (columnResult.invalid()) {
      return columnResult
    }
    if (specificValue != null) {
      val value = if (specificValue.stringValue.isNotBlank()) {
        "'" + specificValue.stringValue + "'"
      } else {
        specificValue.numberValue
      }
      return BuildSqlResult(String.format(operator.getValueTemplate(),
          columnResult.sql, value) + " " + (append?.toUpperCase() ?: ""))
    }
    val scriptParam = scriptParam()
    return BuildSqlResult(String.format(operator.getTemplate(),
        columnResult.sql, scriptParam) + " " + (append?.toUpperCase() ?: ""))
  }

  fun wrapWithTests(sql: String): BuildSqlResult {
    val tests = getTests()
    if (tests.isNotEmpty()) {
      return BuildSqlResult(SqlScriptUtils.convertIf(sql, tests.joinToString(" &amp;&amp; "), true))
    }
    return BuildSqlResult(sql)
  }

  private fun getTests(): ArrayList<String> {
    val tests = arrayListOf<String>()
    val realParam = realParam()
    if (parameter?.type != null && parameter!!.type.isMarkedNullable) {
      tests.add("$realParam != null")
      if (parameter!!.type.jvmErasure == String::class) {
        tests.add("$realParam.length() > 0")
      }
      if (Collection::class.java.isAssignableFrom(parameter!!.type.jvmErasure.java)) {
        tests.add("$realParam.size() > 0")
      }
    }
    return tests
  }

  private fun realParam(): String {
    return paramName ?: property
  }

  private fun scriptParam(): String {
    val realParam = realParam()
    if (operator == Operations.In) {
      return String.format(FOREACH, realParam)
    }
    return realParam
  }

}
