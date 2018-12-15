package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.annotations.ValueAssign
import com.aegis.mybatis.xmlless.constant.Operations
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

  fun toSql(mappings: FieldMappings): String {
    val sqlBuilder = toSqlWithoutTest(mappings)
    return wrapWithTests(sqlBuilder)
  }

  fun toSqlWithoutTest(mappings: FieldMappings): String {
    val columnResult = mappings.resolveColumnByPropertyName(property)
    val value = resolveValue()
    return if (value != null) {
      String.format(operator.getValueTemplate(),
          columnResult, operator.operator, value) + " " + (append?.toUpperCase() ?: "")
    } else {
      String.format(operator.getTemplate(),
          columnResult, operator.operator, scriptParam()) + " " + (append?.toUpperCase() ?: "")
    }
  }

  override fun toString(): String {
    return String.format(
        operator.getTemplate(),
        property, operator.operator, scriptParam()
    )
  }

  fun wrapWithTests(sql: String): String {
    val tests = getTests()
    if (tests.isNotEmpty()) {
      return SqlScriptUtils.convertIf(sql, tests.joinToString(" &amp;&amp; "), true)
    }
    return sql
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

  private fun resolveValue(): String? {
    if (specificValue != null) {
      return if (specificValue.stringValue.isNotBlank()) {
        "'" + specificValue.stringValue + "'"
      } else {
        specificValue.nonStringValue
      }
    } else if (paramName != null) {
      if (paramName!!.matches("\\d+".toRegex())) {
        return paramName
      } else if (paramName == "true" || paramName == "true") {
        return paramName!!.toUpperCase()
      }
    }
    return null
  }

  private fun scriptParam(): String {
    val realParam = realParam()
    if (operator == Operations.In) {
      return String.format(FOREACH, realParam)
    }
    return realParam
  }

}
