package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.constant.Strings
import com.aegis.mybatis.xmlless.enums.Operations
import com.aegis.mybatis.xmlless.enums.TestType
import com.aegis.mybatis.xmlless.model.component.TestConditionDeclaration
import com.aegis.mybatis.xmlless.resolver.ColumnsResolver
import com.aegis.mybatis.xmlless.resolver.TypeResolver
import com.aegis.mybatis.xmlless.util.FieldUtil
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
import java.lang.reflect.Parameter
import java.util.*


/**
 * 条件参数
 *
 * @author 吴昊
 * @date 2023/12/11
 * @version 1.0
 * @since v4.0.0
 */
data class CriteriaParameter(
    val name: String,
    val element: AnnotatedElement?,
    val specificValue: Boolean = false
)

/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
data class QueryCriteria(
    val property: String,
    val operator: Operations,
    var parameters: List<CriteriaParameter>,
    val specificValue: SpecificValue?,
    private val methodInfo: MethodInfo,
    var append: Append = Append.AND
) {

  /** 额外他的test判断条件 */
  private var extraTestConditions: List<TestConditionDeclaration> = listOf()


  companion object {
    /**  foreach模板 */
    const val FOREACH = """<foreach collection="%s" item="%s" separator="%s" open="(" close=")">
  %s
</foreach>"""
  }

  /** 选中的列 */
  fun getColumns(): List<SelectColumn> {
    return ColumnsResolver.resolveColumnByPropertyName(
        property, methodInfo, false
    )
  }

  fun toSql(): String {
    val parameter = getOnlyParameter()
    if (parameter != null) {
      val criteriaList = FieldUtil.getCriteriaInfo(parameter, methodInfo)
      if (criteriaList.isNotEmpty()) {
        return criteriaList.joinToString("\n") {
          createSql(it)
        }
      }
    }
    return createSql(null)
  }

  fun toSqlWithoutTest(criteriaInfo: CriteriaInfo?): String {
    if (criteriaInfo != null && criteriaInfo.group.isNotEmpty()) {
      return criteriaInfo.group.toSql(false)
    }
    val mapping = methodInfo.mappings.mappings.firstOrNull { it.property == property }

    val columnResult = getColumns().joinToString(",\n\t") { it.toSql() }
    val value = resolveValue()

    return when {
      // 条件变量为确定的值时
      value != null                                                      -> String.format(
          operator.getValueTemplate(), columnResult, operator.operator, value
      ) + " " + append

      mapping != null && mapping.isJsonArray                             -> buildJsonQuery(
          columnResult, operator, mapping
      )

      operator == Operations.In && mapping?.joinInfo is PropertyJoinInfo -> String.format(
          operator.getTemplate(),
          methodInfo.mappings.tableName + "." + methodInfo.mappings.tableInfo.keyColumn,
          operator.operator,
          *scriptParams(mapping).toTypedArray()
      ) + " " + append

      else                                                               -> {
        String.format(
            operator.getTemplate(), columnResult, operator.operator, *scriptParams().toTypedArray()
        ) + " " + append
      }
    }
  }

  override fun toString(): String {
    return String.format(
        operator.getTemplate(), property, operator.operator, *scriptParams().toTypedArray()
    )
  }

  fun wrapWithTests(sql: String, criteriaInfo: CriteriaInfo?): String {
    val tests = getTests(criteriaInfo?.testInfo)
    if (tests.isNotBlank()) {
      return SqlScriptUtils.convertIf("\t" + sql, tests, true)
    }
    return sql
  }

  fun getCriteriaList(): List<CriteriaInfo> {
    val parameter = getOnlyParameter()
    if (parameter != null) {
      return FieldUtil.getCriteriaInfo(parameter, methodInfo)
    }
    return listOf()
  }

  fun getTests(parameterTest: TestInfo?): String {
    if (parameterTest != null) {
      return resolveTests(parameterTest)
    }
    var tests = listOf<TestConditionDeclaration>()
    when (val parameter = getOnlyParameter()) {
      is Parameter -> tests = resolveTestsFromType(parameter.type)
      is Field     -> tests = resolveTestsFromType(parameter.type)
    }
    return (tests + extraTestConditions).joinToString(Strings.TESTS_CONNECTOR) { it.toSql() }
  }

  private fun createSql(
      criteriaInfo: CriteriaInfo? = null
  ): String {
    val sqlBuilder = toSqlWithoutTest(criteriaInfo)
    val exp = wrapWithTests(sqlBuilder, criteriaInfo)
    if (operator == Operations.Between) {
      val realParams = realParams()
      val s1 = QueryCriteria(
          property, Operations.Gte, parameters.dropLast(1), specificValue, methodInfo, append
      ).apply {
        extraTestConditions = listOf(
            TestConditionDeclaration(
                realParams[1].toString(), TestType.IsNull
            )
        )
      }.toSql()
      val s2 = QueryCriteria(
          property, Operations.Lte, parameters.drop(1), specificValue, methodInfo, append
      ).apply {
        extraTestConditions = listOf(
            TestConditionDeclaration(
                realParams[0].toString(), TestType.IsNull
            )
        )
      }.toSql()
      return listOf(exp, s1, s2).joinToString("\n")
    }
    return exp
  }

  private fun buildJsonQuery(
      columnResult: String, operator: Operations, mapping: FieldMapping
  ): String {
    val type = TypeResolver.resolveRealType(mapping.type)
    val quote = type == String::class.java
    if (operator in listOf(Operations.Eq, Operations.EqDefault)) {
      return String.format(
          """JSON_CONTAINS(%s -> '$[*]', '${quote("\${%s}", quote)}', '$')""",
          columnResult,
          *scriptParams(wrap = false).toTypedArray()
      )
    } else if (operator in listOf(Operations.In)) {
      return String.format(
          FOREACH,
          realParams()[0],
          "item",
          " OR ",
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
    return parameters.firstOrNull()?.element
  }

  private fun getOnlyParameterName(): String? {
    return parameters.firstOrNull()?.name
  }

  private fun resolveTests(parameterTest: TestInfo): String {
    return parameterTest.getExpression(parameters.filter { it.element != null })
  }

  private fun realParams(): List<ParamHolder> {
    if (parameters.isEmpty()) {
      return listOf(ParamHolder(property, null))
    }
    return parameters.map {
      if (it.specificValue) {
        ParamHolder(null, it.name)
      } else {
        ParamHolder(it.name, null)
      }
    }
  }

  private fun resolveTestsFromType(type: Class<*>): List<TestConditionDeclaration> {
    val realParams = realParams()
    val tests = ArrayList<TestConditionDeclaration>()
    if (!type.isPrimitive) {
      tests.addAll(realParams.map { TestConditionDeclaration(it.toString(), TestType.NotNull) })
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
        paramName == "true"                 -> paramName.uppercase(Locale.getDefault())
        else                                -> null
      }

      else                  -> null
    }
  }

  private fun scriptParams(propertyMapping: FieldMapping? = null, wrap: Boolean = true): List<String> {
    val realParams = realParams()
    // 如果存在realParam未 xxInXx的情况
    if (operator == Operations.In) {
      if (propertyMapping != null && propertyMapping.joinInfo is PropertyJoinInfo) {
        return listOf(
            "(SELECT ${propertyMapping.joinInfo.targetColumn} FROM ${propertyMapping.joinInfo.joinTable.name} WHERE " + "${propertyMapping.joinInfo.propertyColumn.name} = #{${realParams[0]}})"
        )
      }
      return listOf(String.format(FOREACH, realParams[0], "item", ", ", "#{item}"))
    }
    return realParams.map {
      it.toExpression(wrap)
    }
  }

}

/**
 * 特殊值
 *
 * @author 吴昊
 * @date 2023/12/07
 * @version 1.0
 * @since v4.0.0
 */
class SpecificValue(
    val stringValue: String, val nonStringValue: String
)

/**
 * @author 吴昊
 * @date 2023/12/11
 * @version 1.0
 * @since v4.0.0
 */
class ParamHolder(val name: String?, val value: String?) {

  override fun toString(): String {
    return name ?: value ?: ""
  }

  fun toExpression(wrap: Boolean): String {
    return if (wrap && name != null) {
      "#{$name}"
    } else {
      name ?: value ?: ""
    }
  }

}

/**
 * 条件连接后缀类型
 *
 * @author 吴昊
 * @date 2023/12/07
 * @version 1.0
 * @since v4.0.0
 */
enum class Append {

  AND,
  OR;

}
