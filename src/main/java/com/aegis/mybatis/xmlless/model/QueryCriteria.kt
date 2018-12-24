package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.annotations.Criteria
import com.aegis.mybatis.xmlless.annotations.TestExpression
import com.aegis.mybatis.xmlless.annotations.ValueAssign
import com.aegis.mybatis.xmlless.constant.Strings
import com.aegis.mybatis.xmlless.enums.Operations
import com.aegis.mybatis.xmlless.enums.TestType
import com.aegis.mybatis.xmlless.resolver.AnnotationResolver
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure

/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
data class QueryCriteria(val property: String,
                         val operator: Operations,
                         var append: String? = null,
                         val paramName: String?,
                         val parameter: KAnnotatedElement?,
                         val specificValue: ValueAssign?) {

  companion object {
    /**  foreach模板 */
    const val FOREACH = """<foreach collection="%s" item="%s" separator=", " open="(" close=")">
  %s
</foreach>"""
  }

  fun toSql(mappings: FieldMappings): String {
    val sqlBuilder = toSqlWithoutTest(mappings)
    return wrapWithTests(sqlBuilder)
  }

  fun hasExpression(): Boolean {
    if (parameter != null) {
      val criteria = parameter.findAnnotation<Criteria>()
      if (criteria != null && criteria.expression.isNotBlank()) {
        return criteria.expression.isNotBlank()
      }
    }
    return false
  }

  fun toSqlWithoutTest(mappings: FieldMappings): String {
    if (parameter != null) {
      val criteria = parameter.findAnnotation<Criteria>()
      if (criteria != null && criteria.expression.isNotBlank()) {
        return criteria.expression
      }
    }
    val columnResult = mappings.resolveColumnByPropertyName(property, false).joinToString(",\n\t") { it.toSql() }
    val value = resolveValue()
    val mapping = mappings.mappings.firstOrNull { it.property == property }
    return when {
      value != null -> String.format(operator.getValueTemplate(),
          columnResult, operator.operator, value) + " " + (append?.toUpperCase() ?: "")
      operator == Operations.In
          && mapping?.joinInfo is PropertyJoinInfo
                    -> String.format(operator.getTemplate(),
          mappings.tableInfo.tableName + "." + mappings.tableInfo.keyColumn,
          operator.operator, scriptParam(mapping)) + " " + (append?.toUpperCase() ?: "")
      else          -> {
        String.format(operator.getTemplate(),
            columnResult, operator.operator, scriptParam()) + " " + (append?.toUpperCase()
            ?: "")
      }
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
    if (tests.isNotBlank()) {
      return SqlScriptUtils.convertIf("\t" + sql, tests, true)
    }
    return sql
  }

  private fun getTests(): String {
    if (parameter != null) {
      val parameterTest = AnnotationResolver.resolve(parameter)
          ?: AnnotationResolver.resolve<Criteria>(parameter)?.test
      if (parameterTest != null) {
        return resolveTests(parameterTest)
      }
      var tests = listOf<String>()
      when (parameter) {
        is KParameter   -> tests = resolveTestsFromType(parameter.type)
        is KProperty<*> -> tests = resolveTestsFromType(parameter.returnType)
      }
      return tests.joinToString(Strings.TESTS_CONNECTOR)
    } else {
      return ""
    }
  }

  private fun realParam(): String {
    return paramName ?: property
  }

  private fun resolveTests(parameterTest: TestExpression): String {
    val realParam = realParam()
    val clazz = if (parameter is KParameter) {
      parameter.type.jvmErasure
    } else {
      parameter as KProperty<*>
      parameter.returnType.jvmErasure
    }.java

    val tests = parameterTest.value.joinToString(Strings.TESTS_CONNECTOR) {
      realParam + if (it != TestType.NotEmpty) {
        it.expression
      } else {
        when {
          Collection::class.java.isAssignableFrom(clazz) -> ".size > 0"
          clazz == String::class                         -> ".length() > 0"
          clazz.isArray                                  -> ".length > 0"
          else                                           -> ".size > 0"
        }
      }
    }
    return when {
      tests.isNotEmpty() -> when {
        parameterTest.expression.isNotBlank() -> parameterTest.expression + Strings.TESTS_CONNECTOR + tests
        else                                  -> tests
      }
      else               -> parameterTest.expression
    }
  }

  private fun resolveTestsFromType(type: KType): List<String> {
    val realParam = realParam()
    val tests = ArrayList<String>()
    if (type.isMarkedNullable) {
      tests.add("$realParam != null")
    }
    if (type.jvmErasure == String::class) {
      tests.add("$realParam.length() > 0")
    }
    if (Collection::class.java.isAssignableFrom(type.jvmErasure.java)) {
      tests.add("$realParam.size() > 0")
    }
    return tests
  }

  private fun resolveValue(): String? {
    return when {
      specificValue != null -> when {
        specificValue.stringValue.isNotBlank() -> "'" + specificValue.stringValue + "'"
        else                                   -> specificValue.nonStringValue
      }
      paramName != null     -> when {
        paramName.matches("\\d+".toRegex())        -> paramName
        paramName == "true" || paramName == "true" -> paramName.toUpperCase()
        else                                       -> null
      }
      else                  -> null
    }
  }

  private fun scriptParam(propertyMapping: FieldMapping? = null): String {
    val realParam = realParam()
    // 如果存在realParam未 xxInXx的情况
    if (operator == Operations.In) {
      if (propertyMapping != null && propertyMapping.joinInfo is PropertyJoinInfo) {
        return "(SELECT ${propertyMapping.joinInfo.targetColumn} FROM ${propertyMapping.joinInfo.joinTable.name} WHERE " +
            "${propertyMapping.joinInfo.propertyColumn.name} = #{$realParam})"
      }
      return String.format(FOREACH, realParam, "item", "#{item}")
    }
    return realParam
  }

}
