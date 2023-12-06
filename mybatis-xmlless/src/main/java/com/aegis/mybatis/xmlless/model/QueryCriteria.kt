package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.annotations.Criteria
import com.aegis.mybatis.xmlless.annotations.TestExpression
import com.aegis.mybatis.xmlless.constant.Strings
import com.aegis.mybatis.xmlless.enums.Operations
import com.aegis.mybatis.xmlless.enums.TestType
import com.aegis.mybatis.xmlless.model.component.TestConditionDeclaration
import com.aegis.mybatis.xmlless.resolver.AnnotationResolver
import com.aegis.mybatis.xmlless.resolver.TypeResolver
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
import java.lang.reflect.Parameter
import kotlin.reflect.jvm.jvmErasure


class SpecificValue(
    val stringValue: String,
    val nonStringValue: String
)

/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
data class QueryCriteria(
    val property: String,
    val operator: Operations,
    var append: Append = Append.AND,
    var parameters: List<Pair<String, AnnotatedElement?>>,
    val specificValue: SpecificValue?,
    private val mappings: FieldMappings
) {

  /** 选中的列 */
  var columns: List<SelectColumn> = listOf()

  /** 额外他的test判断条件 */
  var extraTestConditions: List<TestConditionDeclaration> = listOf()

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
      val criteria = parameter.getAnnotation(Criteria::class.java)
      if (criteria != null && criteria.expression.isNotBlank()) {
        return criteria.expression.isNotBlank()
      }
    }
    return false
  }

  fun toSql(mappings: FieldMappings): String {
    val sqlBuilder = toSqlWithoutTest(mappings)
    val exp = wrapWithTests(sqlBuilder)
    if (operator == Operations.Between) {
      val realParams = realParams()
      val s1 = QueryCriteria(
          property,
          Operations.Gte,
          append,
          parameters.dropLast(1),
          specificValue,
          mappings
      ).apply {
        extraTestConditions = listOf(
            TestConditionDeclaration(
                realParams[1],
                TestType.IsNull
            )
        )
      }.toSql(mappings)
      val s2 = QueryCriteria(
          property,
          Operations.Lte,
          append,
          parameters.drop(1),
          specificValue,
          mappings
      ).apply {
        extraTestConditions = listOf(
            TestConditionDeclaration(
                realParams[0],
                TestType.IsNull
            )
        )
      }.toSql(mappings)
      return listOf(exp, s1, s2).joinToString("\n")
    }
    return exp
  }

  fun toSqlWithoutTest(mappings: FieldMappings): String {
    val parameter = getOnlyParameter()
    if (parameter != null) {
      val criteria = parameter.getAnnotation(Criteria::class.java)
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
      return "\"$str\""
    }
    return str
  }

  private fun getOnlyParameter(): AnnotatedElement? {
    return parameters.firstOrNull()?.second
  }

  private fun getOnlyParameterName(): String? {
    return parameters.firstOrNull()?.first
  }

  private fun getTests(): String {
    val parameter = getOnlyParameter() ?: return ""
    val parameterTest = AnnotationResolver.resolve(parameter)
      ?: AnnotationResolver.resolve<Criteria>(parameter)?.test
    if (parameterTest != null) {
      return resolveTests(parameterTest)
    }
    var tests = listOf<TestConditionDeclaration>()
    when (parameter) {
      is Parameter   -> tests = resolveTestsFromType(parameter.type)
      is Field -> tests = resolveTestsFromType(parameter.type)
    }
    return (tests + extraTestConditions).joinToString(Strings.TESTS_CONNECTOR) { it.toSql() }

  }

  private fun resolveTests(parameterTest: TestExpression): String {
    val parameter = getOnlyParameter()
    val realParams = realParams()
    val clazz = if (parameter is Parameter) {
      parameter.type
    } else {
      parameter as Field
      parameter.type
    }

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

  private fun realParams(): List<String> {
    if (parameters.isEmpty()) {
      return listOf(property)
    }
    return parameters.map {
      it.first
    }
  }

  private fun resolveTestsFromType(type: Class<*>): List<TestConditionDeclaration> {
    val realParams = realParams()
    val tests = ArrayList<TestConditionDeclaration>()
    if (!type.isPrimitive) {
      tests.addAll(realParams.map { TestConditionDeclaration(it, TestType.NotNull) })
    }
    if (type == String::class.java) {
      tests.addAll(realParams.map { TestConditionDeclaration("$it.length()", TestType.GtZero) })
    }
    if (Collection::class.java.isAssignableFrom(type)) {
      tests.addAll(realParams.map { TestConditionDeclaration("$it.size()", TestType.GtZero) })
    }
    return tests.toList()
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
