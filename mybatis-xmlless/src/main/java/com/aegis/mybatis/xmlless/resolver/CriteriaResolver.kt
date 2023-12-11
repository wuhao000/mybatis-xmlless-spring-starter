package com.aegis.mybatis.xmlless.resolver

import cn.hutool.core.util.ReflectUtil
import com.aegis.kotlin.toCamelCase
import com.aegis.mybatis.xmlless.enums.Operations
import com.aegis.mybatis.xmlless.kotlin.split
import com.aegis.mybatis.xmlless.model.*
import com.aegis.mybatis.xmlless.util.FieldUtil
import org.apache.ibatis.annotations.Param
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
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
      methodInfo: MethodInfo
  ): List<QueryCriteria> {
    val nameConditions = if (allConditionWords.isNotEmpty()) {
      val parameterOffsetHolder = ValueHolder(0)
      splitAndConditionKeywords(allConditionWords).map { addPropertiesWords ->
        // 解析形如 nameEq 或者 nameLikeKeywords 的表达式
        // nameEq 解析为 name = #{name}
        // nameLikeKeywords 解析为 name  LIKE concat('%',#{keywords},'%')
        addPropertiesWords.split("Or").map { singleConditionWords ->
          resolveCriteria(singleConditionWords, methodInfo, parameterOffsetHolder)
        }.apply {
          last().append = Append.AND
        }
      }.flatten()
    } else {
      listOf()
    }
    val parameterConditions = arrayListOf<QueryCriteria>()
    val paramNames = methodInfo.paramNames
    methodInfo.parameters.forEachIndexed { index, parameter ->
      if (parameter.criteria.isNotEmpty()) {
        parameterConditions.addAll(resolveCriteria(parameter, paramNames[index], methodInfo))
      }
    }
    return nameConditions + parameterConditions
  }


  fun createComplexParameterCondition(methodInfo: MethodInfo, mappings: FieldMappings): List<List<QueryCriteria>> {
    val parameterConditions = arrayListOf<List<QueryCriteria>>()
    val paramNames = methodInfo.paramNames
    methodInfo.parameters.forEachIndexed { index, parameter ->
      if (ParameterResolver.isComplexType(parameter.type)) {
        ReflectUtil.getFields(TypeResolver.resolveRealType(parameter.type)).forEach { field ->
          val criteriaInfo = FieldUtil.getCriteriaInfo(field, methodInfo)
          if (criteriaInfo.isNotEmpty()) {
            parameterConditions.add(
                listOf(resolveCriteriaFromProperty(field, paramNames[index], methodInfo))
            )
          }
          parameterConditions.add(FieldUtil.getCriteria(field, methodInfo))
        }
      }
    }
    return parameterConditions
  }

  private fun resolveCriteria(
      parameter: ParameterInfo,
      paramName: String,
      methodInfo: MethodInfo
  ): List<QueryCriteria> {
    val criteriaList = parameter.criteria
    return criteriaList.map {
      QueryCriteria(
          paramName, Operations.EqDefault, listOf(CriteriaParameter(paramName, parameter.parameter)),
          methodInfo.resolvedName?.values?.firstOrNull {
            it.param == paramName
          }?.let {
            SpecificValue(it.stringValue, it.nonStringValue)
          },
          methodInfo,
          Append.AND
      )
    }
  }

  private fun resolveCriteria(
      singleConditionWords: List<String>,
      methodInfo: MethodInfo,
      parameterOffsetHolder: ValueHolder<Int>
  ): QueryCriteria {
    // 获取表示条件表达式操作符的单词
    val singleConditionString = singleConditionWords.joinToString("").toCamelCase()
    val opWordsList = Operations.nameWords().filter {
      singleConditionWords.containsAll(it)
          && singleConditionWords.joinToString("").contains(it.joinToString(""))
    }.sortedByDescending { it.size }
    val maxOpWordCount = opWordsList.maxOfOrNull { it.size }
    val opWords = opWordsList.filter { it.size == maxOpWordCount }
        .minByOrNull {
          singleConditionString.indexOf(it.joinToString(""))
        }
    // 解析条件表达式的二元操作符 = > < >= <= != in like 等
    val op = when {
      opWords != null -> Operations.valueOf(opWords)
      else            -> Operations.Eq
    }
    val props = when {
      opWords != null -> singleConditionWords.split(opWords).map { it.split("") }.flatten()
      else            -> listOf(singleConditionWords)
    }.toMutableList()

    if (props.size !in 1..3) {
      throw IllegalStateException("无法从${singleConditionWords.joinToString("")}中解析查询条件")
    }

    val property = when {
      op != null -> props.first().joinToString("").toCamelCase()
      else       -> singleConditionWords.joinToString("").toCamelCase()
    }
    val specificValue = getSpecificValue(methodInfo, property)
    val paramNames = if (specificValue == null) {
      getParamNames(props, property, op, methodInfo, parameterOffsetHolder)
    } else {
      listOf()
    }
    val parameters = paramNames.map { paramName ->
      if (paramName.matches("\\d+".toRegex()) || paramName in listOf("true", "false")) {
        CriteriaParameter(paramName, null, true)
      } else {
        val parameterData = ParameterResolver.resolve(paramName, methodInfo)
          ?: error("无法识别${methodInfo.method}的参数【$paramName】")
        val parameter = parameterData.property ?: parameterData.parameter?.parameter
        CriteriaParameter(paramName, parameter)
      }
    }

    return QueryCriteria(
        property, op ?: Operations.EqDefault, parameters,
        specificValue,
        methodInfo,
        Append.OR
    )
  }

  private fun getSpecificValue(methodInfo: MethodInfo, property: String): SpecificValue? {
    return methodInfo.resolvedName?.values?.firstOrNull {
      it.param == property
    }?.let {
      SpecificValue(it.stringValue, it.nonStringValue)
    }
  }

  /**
   * 获取条件内所有参数名称（包含条件字段）
   */
  private fun getParamNames(
      props: MutableList<List<String>>,
      property: String,
      op: Operations?,
      methodInfo: MethodInfo,
      parameterOffsetHolder: ValueHolder<Int>
  ): List<String> {
    val paramCount = op?.parameterCount ?: 0

    val paramNames = when (paramCount) {
      2 -> {
        listOf(
            if (props.size > 1) {
              chooseFromParameter(methodInfo, props[1].joinToString("").toCamelCase(), parameterOffsetHolder)
            } else {
              chooseFromParameter(methodInfo, property, parameterOffsetHolder)
            },
            if (props.size > 2) {
              chooseFromParameter(methodInfo, props[2].joinToString("").toCamelCase(), parameterOffsetHolder)
            } else {
              chooseFromParameter(methodInfo, property, parameterOffsetHolder)
            }
        )
      }

      1 -> {
        listOf(
            if (props.size > 1) {
              // 解决剩余的名称为aInB的情况
              val parts = props[1].split("In")
              chooseFromParameter(
                  methodInfo, if (parts.size == 2) {
                parts[1].joinToString("").toCamelCase()
              } else {
                props[1].joinToString("").toCamelCase()
              }, parameterOffsetHolder
              )
            } else {
              chooseFromParameter(methodInfo, property, parameterOffsetHolder)
            }
        )
      }

      else -> listOf(property)
    }
    return paramNames
  }

  fun chooseFromParameter(
      methodInfo: MethodInfo,
      property: String,
      parameterOffsetHolder: ValueHolder<Int>
  ): String {
    if (property.matches("\\d+".toRegex()) || property in listOf("true", "false")) {
      return property
    }
    val optionalParam = methodInfo.findOptionalParam(property)
    return if (optionalParam != null) {
      optionalParam.name()
    } else {
      if (parameterOffsetHolder.value > methodInfo.parameters.size - 1) {
        error("无法从方法参数中解析出【${property}】所需参数名称")
      }
      val parameterName = resolveParameterName(methodInfo.parameters[parameterOffsetHolder.value].parameter)
      parameterOffsetHolder.value++
      parameterName
    }
  }

  private fun resolveParameterName(kParameter: AnnotatedElement): String {
    return when {
      kParameter.isAnnotationPresent(Param::class.java) -> {
        kParameter.getAnnotation(Param::class.java)!!.value
      }

      kParameter is Parameter                           -> {
        kParameter.name!!
      }

      kParameter is Field                               -> {
        kParameter.name
      }

      else                                              -> {
        error("无法解析的参数类型")
      }
    }
  }

  private fun resolveCriteriaFromProperty(
      property: Field,
      paramName: String,
      methodInfo: MethodInfo
  ): QueryCriteria {
    return QueryCriteria(
        property.name, Operations.EqDefault, listOf(CriteriaParameter(paramName + "." + property.name, property)),
        methodInfo.resolvedName?.values?.firstOrNull {
          it.param == paramName
        }?.let {
          SpecificValue(it.stringValue, it.nonStringValue)
        },
        methodInfo, Append.AND
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
