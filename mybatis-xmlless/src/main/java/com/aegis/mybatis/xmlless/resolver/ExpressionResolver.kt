package com.aegis.mybatis.xmlless.resolver

import com.aegis.kotlin.only
import com.aegis.mybatis.xmlless.model.MethodInfo
import java.util.*

/**
 * Created by 吴昊 on 2023/12/11.
 */
object ExpressionResolver {

  private val SPLIT_CHARS = listOf('(', ')', ',', ' ', '+', '-', '*', '/')
  private val SPLIT_STRINGS = SPLIT_CHARS.map { it.toString() }

  fun parseExpression(expression: String, methodInfo: MethodInfo): String {
    val stack = parseExpression(expression)
    val list = mutableListOf<String>()
    while (stack.isNotEmpty()) {
      val item = stack.pop()
      if (list.lastOrNull() == "(" || item.matches("^'.*'$".toRegex())
          || item in SPLIT_STRINGS || item.matches("\\d+".toRegex())
      ) {
        list.add(item)
      } else {
        val refColumns = ColumnsResolver.resolveColumnByPropertyName(item, methodInfo, true)
        if (refColumns.size != 1) {
          error("无法解析表达式【${expression}】中的属性${item}")
        }
        list.add(refColumns.only().toSql())
      }
    }
    return list.reversed().joinToString("")
  }

  fun parseExpression(expression: String): Stack<String> {
    val stack = Stack<String>()
    var inString = false
    expression.replace("\\s+".toRegex(), " ").toCharArray().forEach { c ->
      when {
        stack.isEmpty()                                   -> {
          stack.push(c + "")
        }

        c == '\'' && !inString                            -> {
          inString = true
          stack.push(c + "")
        }

        c == '\'' && inString                             -> {
          inString = false
          stack.push(stack.pop() + c)
        }

        inString                                          -> {
          stack.push(stack.pop() + c)
        }

        stack.last() in SPLIT_STRINGS || c in SPLIT_CHARS -> {
          stack.push(c + "")
        }

        else                                              -> {
          stack.push(stack.pop() + c)
        }
      }
    }
    return stack
  }

}
