package com.aegis.mybatis.xmlless.resolver

import com.aegis.mybatis.xmlless.constant.Operations
import com.aegis.mybatis.xmlless.kotlin.toCamelCase
import com.aegis.mybatis.xmlless.kotlin.toWords
import com.aegis.mybatis.xmlless.model.Condition
import org.apache.ibatis.reflection.ParamNameResolver
import org.apache.ibatis.session.Configuration
import org.junit.Test
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.javaMethod

/**
 * 测试条件解析
 *
 * @author 吴昊
 * @since 0.0.8
 */
class ConditionResolverTest {

  fun r(condition: String) {
    val stack = Stack<String>()
    condition.toWords().reversed().forEach {
      stack.push(it)
    }
    val groups = ArrayList<ArrayList<String>>()
    var group = ArrayList<String>()
    val opw = Operations.nameWords()
    val maxOpWordCount = opw.map { it.size }.max()!!
    var param: String? = null
    var op: Operations? = null
    var values = arrayListOf<String>()
    val conditions = ArrayList<Condition>()
    while (stack.isNotEmpty()) {
      val currentItem = stack.pop()
      when (currentItem) {
        in listOf("And", "Or") -> {

        }
        else                   -> {
          if (group.isEmpty()) {
            group.add(currentItem)
          } else {
            val mayBeOpWords = listOf(currentItem) + (when {
              stack.size >= maxOpWordCount - 1 -> (0..maxOpWordCount).map { stack.pop() }
              else                             -> (0 until stack.size).map { stack.pop() }
            })
            val opWords = opw.filter { mayBeOpWords.subList(0, it.size) == it }
                .sortedByDescending { it.size }.firstOrNull()
            if (opWords != null) {
              param = group.joinToString("").toCamelCase()
              op = Operations.valueOf(opWords.joinToString(""))
              group.addAll(opWords)
              mayBeOpWords.subList(0, opWords.size).reversed().forEach {
                stack.push(it)
              }
            } else {
              group.add(currentItem)
              mayBeOpWords.subList(1, mayBeOpWords.size).reversed().forEach {
                stack.push(it)
              }
            }
          }
        }
      }

    }
  }

  @Test
  fun resolveConditions() {
    TestDAO::class.declaredFunctions.forEach {
      val a = it.name.replace("findBy", "")
      val conditions = resolveConditions(a, it)
      println("${it.name} *********************")
      conditions.forEach {
        println(it)
      }
    }
  }

  private fun resolveConditions(conditionExpression: String, function: KFunction<*>): List<Condition> {
    val paramNames = ParamNameResolver(
        Configuration().apply {
          this.isUseActualParamName = true
        }, function.javaMethod
    ).names
    return ConditionResolver.resolveConditions(
        conditionExpression.toWords(), function,
        paramNames
    )
  }

}

class TestDAO {

  fun findByAgeBetweenMinAndMax() {
  }

  fun findByNameEqValueWuhao() {
  }

  fun findByNameLikeKeywords() {
  }

}
