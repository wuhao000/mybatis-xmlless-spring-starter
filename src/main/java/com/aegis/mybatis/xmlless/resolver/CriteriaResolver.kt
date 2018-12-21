package com.aegis.mybatis.xmlless.resolver

import com.aegis.mybatis.xmlless.annotations.Criteria
import com.aegis.mybatis.xmlless.annotations.ResolvedName
import com.aegis.mybatis.xmlless.enums.Operations
import com.aegis.mybatis.xmlless.kotlin.split
import com.aegis.mybatis.xmlless.kotlin.toCamelCase
import com.aegis.mybatis.xmlless.model.MatchedParameter
import com.aegis.mybatis.xmlless.model.QueryCriteria
import com.baomidou.mybatisplus.core.toolkit.StringPool.DOT
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters

/**
 *
 * Created by 吴昊 on 2018-12-15.
 *
 * @author 吴昊
 * @since 0.0.8
 */
object CriteriaResolver {

  fun resolveConditions(allConditionWords: List<String>,
                        function: KFunction<*>): List<QueryCriteria> {
    val nameConditions = if (allConditionWords.isNotEmpty()) {
      allConditionWords.split("And").map { addPropertiesWords ->
        // 解析形如 nameEq 或者 nameLikeKeywords 的表达式
        // nameEq 解析为 name = #{name}
        // nameLikeKeywords 解析为 name  LIKE concat('%',#{keywords},'%')
        addPropertiesWords.split("Or").map { singleConditionWords ->
          resolveCriteria(singleConditionWords, function)
        }.apply {
          last().append = "And"
        }
      }.flatten()
    } else {
      listOf()
    }
    val parameterConditions = arrayListOf<QueryCriteria>()
    val paramNames = ParameterResolver.resolveNames(function)
    function.valueParameters.forEachIndexed { index, parameter ->
      val criteria = parameter.findAnnotation<Criteria>()
      if (criteria != null) {
        parameterConditions.add(resolveCriteria(criteria, parameter, paramNames[index], function))
      } else if (ParameterResolver.isComplexParameter(parameter)) {
        TypeResolver.resolveRealType(parameter.type).declaredMemberProperties.forEach { property ->
          val propertyCriteria = property.findAnnotation<Criteria>()
          if (propertyCriteria != null) {
            parameterConditions.add(
                resolveCriteriaFromProperty(propertyCriteria, property, paramNames[index], function)
            )
          }
        }
      }
    }
    return nameConditions + parameterConditions
  }

  private fun resolveCriteria(criteria: Criteria, parameter: KParameter, paramName: String, function: KFunction<*>): QueryCriteria {
    return QueryCriteria(paramName, criteria.operator, "And",
        paramName, parameter,
        function.findAnnotation<ResolvedName>()?.values?.firstOrNull {
          it.param == paramName
        })
  }

  private fun resolveCriteria(singleConditionWords: List<String>,
                              function: KFunction<*>): QueryCriteria {
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
    return QueryCriteria(property, op ?: Operations.EqDefault, "Or",
        finalParamName, parameter,
        function.findAnnotation<ResolvedName>()?.values?.firstOrNull {
          it.param == property
        })
  }

  private fun resolveCriteriaFromProperty(criteria: Criteria, property: KProperty1<out Any, Any?>,
                                          paramName: String, function: KFunction<*>): QueryCriteria {
    return QueryCriteria(property.name, criteria.operator, "And",
        paramName + "." + property.name, property,
        function.findAnnotation<ResolvedName>()?.values?.firstOrNull {
          it.param == paramName
        })
  }

}
