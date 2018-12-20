package com.aegis.mybatis.xmlless.resolver

import com.aegis.mybatis.xmlless.annotations.ResolvedName
import com.aegis.mybatis.xmlless.enums.Operations
import com.aegis.mybatis.xmlless.kotlin.split
import com.aegis.mybatis.xmlless.kotlin.toCamelCase
import com.aegis.mybatis.xmlless.model.Condition
import com.aegis.mybatis.xmlless.model.MatchedParameter
import com.baomidou.mybatisplus.core.toolkit.StringPool.DOT
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

/**
 *
 * Created by 吴昊 on 2018-12-15.
 *
 * @author 吴昊
 * @since 0.0.8
 */
object ConditionResolver {

  fun resolveConditions(allConditionWords: List<String>,
                        function: KFunction<*>): List<Condition> {
    return if (allConditionWords.isNotEmpty()) {
      allConditionWords.split("And").map { addPropertiesWords ->
        // 解析形如 nameEq 或者 nameLikeKeywords 的表达式
        // nameEq 解析为 name = #{name}
        // nameLikeKeywords 解析为 name  LIKE concat('%',#{keywords},'%')
        addPropertiesWords.split("Or").map { singleConditionWords ->
          resolveCondition(singleConditionWords, function)
        }.apply {
          last().append = "And"
        }
      }.flatten()
    } else {
      listOf()
    }
  }

  private fun resolveCondition(singleConditionWords: List<String>,
                               function: KFunction<*>): Condition {
// 获取表示条件表达式操作符的单词
    val singleConditionString = singleConditionWords.joinToString("").toCamelCase()
    val opWordsList = Operations.nameWords().filter {
      singleConditionWords.containsAll(it)
          && singleConditionWords.joinToString("").contains(it.joinToString(""))
    }.sortedByDescending { it.size }

    val maxOpWordCount = opWordsList.map { it.size }.max()
    val opWords = opWordsList.filter { it.size == maxOpWordCount }
        .sortedBy {
          singleConditionString.indexOf(it.joinToString(""))
        }
        .firstOrNull()
    val props = when {
      opWords != null -> singleConditionWords.split(opWords)
      else            -> listOf(singleConditionWords)
    }
    if (props.size !in 1..2) {
      throw IllegalStateException("无法从${singleConditionWords.joinToString("")}中解析查询条件")
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
      props.size == 2 -> {
        // 解决剩余的名称为aInB的情况
        val parts = props[1].split("In")
        when {
          parts.size == 2 -> parts[1].joinToString("").toCamelCase()
          else            -> props[1].joinToString("").toCamelCase()
        }
      }
      else            -> null
    } ?: property
    val parameterData: MatchedParameter? = ParameterResolver.resolve(paramName, function)
    val parameter = when {
      parameterData != null -> parameterData.property ?: parameterData.parameter
      else                  -> null
    }
    // 如果条件参数是方法参数中的属性，则需要加上方法参数名称前缀
    val finalParamName = when {
      parameterData?.property != null -> parameterData.paramName + DOT + paramName
      else                            -> paramName
    }
    return Condition(property, op ?: Operations.EqDefault, "Or",
        finalParamName, parameter,
        function.findAnnotation<ResolvedName>()?.values?.firstOrNull {
          it.param == property
        })
  }

}
