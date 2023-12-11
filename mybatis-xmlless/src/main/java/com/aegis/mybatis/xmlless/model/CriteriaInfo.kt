package com.aegis.mybatis.xmlless.model

import com.aegis.kotlin.isNotNullAndNotBlank
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
import java.lang.reflect.Parameter

/**
 *
 * @author 吴昊
 * @date 2023/12/8 11:09
 * @since v4.0.0
 * @version 1.0
 */
data class CriteriaInfo(
    val group: QueryCriteriaGroup,
    val testInfo: TestInfo
)

/**
 *
 * @author 吴昊
 * @date 2023/12/8 11:09
 * @since v4.0.0
 * @version 1.0
 */
class TestInfo(
    private val expression: String,
    private val field: AnnotatedElement
) {

  fun getExpression(parameters: List<CriteriaParameter>): String {
    val name = parameters.find { it.element == field }?.name
    return preHandleTestExpression(expression, field, name)
  }

  companion object {
    private val testOperators = listOf(
        ">", "&lt;", "&gt;", "&lt;=", "&gt;=", "<", ">=", "<=", "!=", "=", "=="
    )
    private val connectOperators = listOf(
        "and", "or", "&&", "||"
    )
  }

  private fun preHandleTestExpression(expression: String, field: AnnotatedElement, specificName: String?): String {
    val name = when {
      specificName.isNotNullAndNotBlank() -> specificName
      field is Field                      -> field.name
      field is Parameter                  -> field.name
      else                                -> error("无法解析的参数类型")
    }
    val list = mutableListOf<String>()
    val parts = expression.split("\\s+".toRegex())
    parts.forEach {
      if (it in testOperators && (list.isEmpty() || list.last().lowercase() in connectOperators
              || list.last().endsWith(")"))
      ) {
        list.add(name)
      }
      list.add(it)
    }
    return list.joinToString(" ").replace(">", "&gt;")
        .replace("<", "&lt;")
  }
}
