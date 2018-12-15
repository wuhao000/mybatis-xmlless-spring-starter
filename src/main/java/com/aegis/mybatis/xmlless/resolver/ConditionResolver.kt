package com.aegis.mybatis.xmlless.resolver

import com.aegis.mybatis.xmlless.annotations.ResolvedName
import com.aegis.mybatis.xmlless.enums.Operations
import com.aegis.mybatis.xmlless.kotlin.split
import com.aegis.mybatis.xmlless.kotlin.toCamelCase
import com.aegis.mybatis.xmlless.model.Condition
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters

/**
 *
 * Created by 吴昊 on 2018-12-15.
 *
 * @author 吴昊
 * @since 0.0.8
 */
object ConditionResolver {

  fun resolveConditions(allConditionWords: List<String>,
                        function: KFunction<*>, paramNames: Array<String>): List<Condition> {
    return if (allConditionWords.isNotEmpty()) {
      allConditionWords.split("And").map { addPropertiesWords ->
        // 解析形如 nameEq 或者 nameLikeKeywords 的表达式
        // nameEq 解析为 name = #{name}
        // nameLikeKeywords 解析为 name  LIKE concat('%',#{keywords},'%')
        addPropertiesWords.split("Or").map { singleConditionWords ->
          // 获取表示条件表达式操作符的单词
          val opWords = Operations.nameWords().filter {
            singleConditionWords.containsAll(it)
                && singleConditionWords.joinToString("").contains(it.joinToString(""))
          }.sortedByDescending { it.size }.firstOrNull()
          val props = when {
            opWords != null -> singleConditionWords.split(opWords)
            else            -> listOf(singleConditionWords)
          }
          if (props.size !in 1..2) {
            throw IllegalStateException("Cannot resolve query conditions from ${allConditionWords.joinToString().toCamelCase()}")
          }
          // 解析条件表达式的二元操作符 = > < >= <= != in like 等
          val op = when {
            opWords != null -> Operations.valueOf(opWords)
            else            -> null
          }
          val property = when {
            op != null -> props.first().joinToString("").toCamelCase()
            else       -> singleConditionWords.joinToString("").toCamelCase()
          }
          val paramName = when {
            props.size == 2 -> props[1].joinToString("").toCamelCase()
            else            -> null
          } ?: property
          var parameter: KParameter? = null
          val paramIndex = paramNames.indexOf(paramName)
          if (paramIndex >= 0) {
            parameter = function.valueParameters[paramIndex]
          }
          Condition(property, op ?: Operations.EqDefault, "Or", paramName, parameter,
              function.findAnnotation<ResolvedName>()?.values?.firstOrNull {
                it.param == property
              })
        }.apply {
          last().append = "And"
        }
      }.flatten()
    } else {
      listOf()
    }
  }

}
