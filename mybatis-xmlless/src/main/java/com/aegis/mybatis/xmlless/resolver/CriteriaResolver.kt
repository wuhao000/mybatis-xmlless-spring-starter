package com.aegis.mybatis.xmlless.resolver

import com.aegis.mybatis.xmlless.annotations.Criteria
import com.aegis.mybatis.xmlless.annotations.Logic
import com.aegis.mybatis.xmlless.annotations.ResolvedName
import com.aegis.mybatis.xmlless.enums.Operations
import com.aegis.mybatis.xmlless.kotlin.split
import com.aegis.mybatis.xmlless.kotlin.toCamelCase
import com.aegis.mybatis.xmlless.model.*
import com.baomidou.mybatisplus.annotation.TableLogic
import com.baomidou.mybatisplus.core.toolkit.StringPool.DOT
import org.apache.ibatis.annotations.Param
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Parameter

/**
 *
 * Created by 吴昊 on 2018-12-15.
 *
 * @author 吴昊
 * @since 0.0.8
 */
object CriteriaResolver {

  fun resolveConditions(
      allConditionWords: List<String>,
      function: Method,
      mappings: FieldMappings,
      queryType: QueryType
  ): List<QueryCriteria> {
    val nameConditions = if (allConditionWords.isNotEmpty()) {
      val parameterOffsetHolder = ValueHolder(0)
      splitAndConditionKeywords(allConditionWords).map { addPropertiesWords ->
        // 解析形如 nameEq 或者 nameLikeKeywords 的表达式
        // nameEq 解析为 name = #{name}
        // nameLikeKeywords 解析为 name  LIKE concat('%',#{keywords},'%')
        addPropertiesWords.split("Or").map { singleConditionWords ->
          resolveCriteria(singleConditionWords, function, mappings, parameterOffsetHolder)
        }.apply {
          last().append = Append.AND
        }
      }.flatten()
    } else {
      listOf()
    }
    val parameterConditions = arrayListOf<QueryCriteria>()
    val paramNames = ParameterResolver.resolveNames(function)
    function.parameters.forEachIndexed { index, parameter ->
      val criteria = parameter.getAnnotation(Criteria::class.java)
      if (criteria != null) {
        parameterConditions.add(resolveCriteria(criteria, parameter, paramNames[index], function, mappings))
      } else if (ParameterResolver.isComplexParameter(parameter.type)) {
        TypeResolver.resolveRealType(parameter.type).declaredFields.forEach { property ->
          val propertyCriteria = property.getDeclaredAnnotation(Criteria::class.java)
            ?: property.getAnnotation(Criteria::class.java)
          if (propertyCriteria != null) {
            parameterConditions.add(
                resolveCriteriaFromProperty(propertyCriteria, property, paramNames[index], function, mappings)
            )
          }
        }
      }
    }
    if (function.isAnnotationPresent(Logic::class.java) && queryType == QueryType.Select) {
      val logic = function.getAnnotation(Logic::class.java)!!
      val mapper = mappings.mappings.find { it.field.isAnnotationPresent(TableLogic::class.java) }
        ?: throw IllegalStateException("缺少逻辑删除字段，请在字段上添加@TableLogic注解")
      parameterConditions.add(
          QueryCriteria(
              mapper.property, Operations.Eq,
              specificValue = SpecificValue(
                  stringValue = "",
                  nonStringValue = mappings.getLogicDelFlagValue(logic).toString()
              ),
              mappings = mappings,
              parameters = listOf()
          )
      )
    }
    return nameConditions + parameterConditions
  }

  private fun resolveCriteria(
      criteria: Criteria, parameter: Parameter, paramName: String,
      function: Method, mappings: FieldMappings
  ): QueryCriteria {
    val property = when {
      criteria.property.isNotBlank() -> criteria.property
      else                           -> paramName
    }
    return QueryCriteria(
        property, criteria.operator, Append.AND,
        listOf(Pair(paramName, parameter)),
        function.getAnnotation(ResolvedName::class.java)?.values?.firstOrNull {
          it.param == paramName
        }?.let {
          SpecificValue(it.stringValue, it.nonStringValue)
        },
        mappings
    )
  }

  private fun resolveCriteria(
      singleConditionWords: List<String>,
      function: Method,
      mappings: FieldMappings,
      parameterOffsetHolder: ValueHolder<Int>
  ): QueryCriteria {
    // 获取表示条件表达式操作符的单词
    val singleConditionString = singleConditionWords.joinToString("").toCamelCase()
    val opWordsList = Operations.nameWords().filter {
      singleConditionWords.containsAll(it)
          && singleConditionWords.joinToString("").contains(it.joinToString(""))
    }.sortedByDescending { it.size }
    val maxOpWordCount = opWordsList.map { it.size }.maxOrNull()
    val opWords = opWordsList.filter { it.size == maxOpWordCount }
        .minByOrNull {
          singleConditionString.indexOf(it.joinToString(""))
        }
    // 解析条件表达式的二元操作符 = > < >= <= != in like 等
    val op = when {
      opWords != null -> Operations.valueOf(opWords)
      else            -> null
    }
    val props = when {
      opWords != null -> singleConditionWords.split(opWords).map { it.split("") }.flatten()
      else            -> listOf(singleConditionWords)
    }.toMutableList()
    if (props.size == 1 && (op?.parameterCount ?: 0) > 0) {
      (1..(op?.parameterCount?:0)).forEach {
        val parameterName = resolveParameterName(function.parameters[parameterOffsetHolder.value])
        props.add(listOf(parameterName))
        parameterOffsetHolder.value++
      }
    }
    if (props.size !in 1..3) {
      throw IllegalStateException("无法从${singleConditionWords.joinToString("")}中解析查询条件")
    }

    val property = when {
      op != null -> props.first().joinToString("").toCamelCase()
      else       -> singleConditionWords.joinToString("").toCamelCase()
    }
    val paramNames = when {
      props.size == 3 -> {
        listOf(
            props[1].joinToString("").toCamelCase(),
            props[2].joinToString("").toCamelCase()
        )
      }

      props.size == 2 -> {
        // 解决剩余的名称为aInB的情况
        val parts = props[1].split("In")
        when (parts.size) {
          2    -> listOf(parts[1].joinToString("").toCamelCase())
          else -> listOf(props[1].joinToString("").toCamelCase())
        }
      }

      else            -> listOf(property)
    }
    val parameters = paramNames.map { paramName ->
      val parameterData: MatchedParameter? = ParameterResolver.resolve(paramName, function)
      val parameter = when {
        parameterData != null -> parameterData.property ?: parameterData.parameter
        else                  -> null
      }
      // 如果条件参数是方法参数中的属性，则需要加上方法参数名称前缀
      val finalParamName = when {
        parameterData?.property != null && parameterData.parameter.isAnnotationPresent(Param::class.java)
             -> parameterData.paramName + DOT + paramName

        else -> paramName
      }
      Pair(finalParamName, parameter)
    }

    return QueryCriteria(
        property, op ?: Operations.EqDefault, Append.OR,
        parameters,
        function.getAnnotation(ResolvedName::class.java)?.values?.firstOrNull {
          it.param == property
        }?.let {
          SpecificValue(it.stringValue, it.nonStringValue)
        },
        mappings
    )
  }

  private fun resolveParameterName(kParameter: Parameter): String {
    return if (kParameter.isAnnotationPresent(Param::class.java)) {
      kParameter.getAnnotation(Param::class.java)!!.value
    } else {
      kParameter.name!!
    }
  }

  private fun resolveCriteriaFromProperty(
      criteria: Criteria, property: Field,
      paramName: String, function: Method, mappings: FieldMappings
  ): QueryCriteria {
    val propertyName = when {
      criteria.property.isNotBlank() -> criteria.property
      else                           -> property.name
    }
    return QueryCriteria(
        propertyName, criteria.operator, Append.AND,
        listOf(Pair(paramName + "." + property.name, property)),
        function.getAnnotation(ResolvedName::class.java)?.values?.firstOrNull {
          it.param == paramName
        }?.let {
          SpecificValue(it.stringValue, it.nonStringValue)
        }, mappings
    )
  }

  private fun splitAndConditionKeywords(allConditionWords: List<String>): List<List<String>> {
    val list = arrayListOf<List<String>>()
    allConditionWords.split("And").forEach {
      if (list.isNotEmpty() && list.last().contains(Operations.Between.name)) {
        list[list.size - 1] = list.last() + "" + it
      } else {
        list.add(it)
      }
    }
    return list
  }

}

class ValueHolder<T>(var value: T)
