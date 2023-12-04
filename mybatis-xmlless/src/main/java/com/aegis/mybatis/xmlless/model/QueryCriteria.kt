package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.annotations.Criteria
import com.aegis.mybatis.xmlless.annotations.TestExpression
import com.aegis.mybatis.xmlless.constant.Strings
import com.aegis.mybatis.xmlless.enums.Operations
import com.aegis.mybatis.xmlless.enums.TestType
import com.aegis.mybatis.xmlless.resolver.AnnotationResolver
import com.aegis.mybatis.xmlless.resolver.TypeResolver
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure


class SpecificValue(val stringValue: String,
                    val nonStringValue: String)
/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
data class QueryCriteria(
    val property: String,
    val operator: Operations,
    var append: Append = Append.AND,
    var parameters: List<Pair<String, KAnnotatedElement?>>,
    val specificValue: SpecificValue?,
    private val mappings: FieldMappings
) {

  var columns: List<SelectColumn> = listOf()

  companion object {
    /**  foreach模板 */
    const val FOREACH = """<foreach collection="%s" item="%s" separator="%s" open="(" close=")">
  %s
</foreach>"""
  }

  init {
    columns = mappings.resolveColumnByPropertyName(property, false)
  }

  fun hasExpression(): Boolean {
    val parameter = getOnlyParameter()
    if (parameter != null) {
      val criteria = parameter.findAnnotation<Criteria>()
      if (criteria != null && criteria.expression.isNotBlank()) {
        return criteria.expression.isNotBlank()
      }
    }
    return false
  }

  fun toSql(mappings: FieldMappings): String {
    val sqlBuilder = toSqlWithoutTest(mappings)
    return wrapWithTests(sqlBuilder)
  }

  fun toSqlWithoutTest(mappings: FieldMappings): String {
    val parameter = getOnlyParameter()
    if (parameter != null) {
      val criteria = parameter.findAnnotation<Criteria>()
      if (criteria != null && criteria.expression.isNotBlank()) {
        return criteria.expression + " " + append
      }
    }
    val mapping = mappings.mappings.firstOrNull { it.property == property }

    val columnResult = columns.joinToString(",\n\t") { it.toSql() }
    val value = resolveValue()

    return when {
      // 条件变量为确定的值时
      value != null                                -> String.format(
          operator.getValueTemplate(),
          columnResult, operator.operator, value
      ) + " " + append
      mapping != null && mapping.isJsonArray       -> buildJsonQuery(columnResult, operator, mapping)
      operator == Operations.In
          && mapping?.joinInfo is PropertyJoinInfo -> String.format(
          operator.getTemplate(),
          mappings.tableInfo.tableName + "." + mappings.tableInfo.keyColumn,
          operator.operator, *scriptParams(mapping).toTypedArray()
      ) + " " + append
      else                                         -> {
        String.format(
            operator.getTemplate(),
            columnResult, operator.operator, *scriptParams().toTypedArray()
        ) + " " + append
      }
    }
  }

  override fun toString(): String {
    return String.format(
        operator.getTemplate(),
        property, operator.operator, *scriptParams().toTypedArray()
    )
  }

  fun wrapWithTests(sql: String): String {
    val tests = getTests()
    if (tests.isNotBlank()) {
      return SqlScriptUtils.convertIf("\t" + sql, tests, true)
    }
    return sql
  }

  private fun buildJsonQuery(
    columnResult: String,
    operator: Operations,
    mapping: FieldMapping
  ): String {
    val type = TypeResolver.resolveRealType(mapping.type)
    val quote = type == String::class.java
    if (operator in listOf(Operations.Eq, Operations.EqDefault)) {
      return String.format(
          """JSON_CONTAINS(%s -> '$[*]', '${quote("\${%s}", quote)}', '$')""",
          columnResult, *scriptParams().toTypedArray()
      )
    } else if (operator in listOf(Operations.In)) {
      return String.format(
          FOREACH, realParams()[0], "item", " OR ",
          "JSON_CONTAINS(${columnResult} -> '\$[*]', '${quote("\${item}", quote)}', '\$')"
      )
    }
    throw IllegalStateException("暂不支持${operator.operator}的json查询")
  }

  private fun quote(str: String, quote: Boolean): String {
    if (quote) {
      return "\"" + str + "\"";
    }
    return str
  }

  private fun getOnlyParameter(): KAnnotatedElement? {
    return parameters.firstOrNull()?.second
  }

  private fun getOnlyParameterName(): String? {
    return parameters.firstOrNull()?.first
  }

  private fun getTests(): String {
    val parameter = getOnlyParameter()
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

  private fun realParams(): List<String> {
    if (parameters.isEmpty()) {
      return listOf(property)
    }
    return parameters.map {
      it.first
    }
  }

  private fun resolveTests(parameterTest: TestExpression): String {
    val parameter = getOnlyParameter()
    val realParams = realParams()
    val clazz = if (parameter is KParameter) {
      parameter.type.jvmErasure
    } else {
      parameter as KProperty<*>
      parameter.returnType.jvmErasure
    }.java

    val tests = parameterTest.value.joinToString(Strings.TESTS_CONNECTOR) {
      realParams[0] + if (it != TestType.NotEmpty) {
        it.expression
      } else {
        when {
          Collection::class.java.isAssignableFrom(clazz) -> ".size " + TestType.GtZero.expression
          clazz == String::class
              || clazz == java.lang.String::class.java   -> ".length() " + TestType.GtZero.expression
          clazz.isArray                                  -> ".length " + TestType.GtZero.expression
          else                                           -> ".size " + TestType.GtZero.expression
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
    val realParams = realParams()
    val tests = ArrayList<String>()
    if (type.isMarkedNullable) {
      tests.addAll(realParams.map { "$it != null" })
    }
    if (type.jvmErasure == String::class) {
      tests.addAll(realParams.map { "$it.length() " + TestType.GtZero.expression })
    }
    if (Collection::class.java.isAssignableFrom(type.jvmErasure.java)) {
      tests.addAll(realParams.map { "$it.size() " + TestType.GtZero.expression })
    }
    return tests
  }

  private fun resolveValue(): String? {
    val paramName = getOnlyParameterName()
    return when {
      specificValue != null -> when {
        specificValue.stringValue.isNotBlank() -> "'" + specificValue.stringValue + "'"
        else                                   -> specificValue.nonStringValue
      }
      paramName != null     -> when {
        paramName.matches("\\d+".toRegex()) -> paramName
        paramName == "true"                 -> paramName.toUpperCase()
        else                                -> null
      }
      else                  -> null
    }
  }

  private fun scriptParams(propertyMapping: FieldMapping? = null): List<String> {
    val realParams = realParams()
    // 如果存在realParam未 xxInXx的情况
    if (operator == Operations.In) {
      if (propertyMapping != null && propertyMapping.joinInfo is PropertyJoinInfo) {
        return listOf(
            "(SELECT ${propertyMapping.joinInfo.targetColumn} FROM ${propertyMapping.joinInfo.joinTable.name} WHERE " +
                "${propertyMapping.joinInfo.propertyColumn.name} = #{${realParams[0]}})"
        )
      }
      return listOf(String.format(FOREACH, realParams[0], "item", ", ", "#{item}"))
    }
    return realParams
  }

}


enum class Append {

  AND,
  OR;

}
