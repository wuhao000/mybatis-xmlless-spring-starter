package com.aegis.mybatis.xmlless.model

import com.aegis.kotlin.isNotNullAndNotBlank
import com.aegis.mybatis.xmlless.enums.Operations
import com.aegis.mybatis.xmlless.enums.TestType
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
    val property: String,
    val expression: String,
    val operator: Operations,
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
    val value: Array<TestType>,
    private val exp: String,
    private val field: AnnotatedElement
) {

  fun hasExpression(): Boolean {
    return exp.isNotBlank()
  }

  fun getExpression(parameters: List<Pair<String, AnnotatedElement>>): String {
    val name = parameters.find { it.second == field }?.first
    return preHandleTestExpression(exp, field, name)
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
