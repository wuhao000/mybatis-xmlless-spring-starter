package com.aegis.mybatis.xmlless.model

import cn.hutool.core.util.ReflectUtil
import com.aegis.kotlin.isNotNullAndNotBlank
import com.aegis.mybatis.xmlless.annotations.*
import com.aegis.mybatis.xmlless.resolver.ParameterResolver
import com.aegis.mybatis.xmlless.util.FieldUtil
import org.apache.ibatis.annotations.Param
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method

/**
 * 方法信息
 *
 * @author 吴昊
 * @date 2023/12/7 11:37
 * @since v4.0.0
 * @version 1.0
 */
class MethodInfo(
    val method: Method,
    val modelClass: Class<*>,
    val mappings: FieldMappings,
    val modelMappings: FieldMappings
) {

  val name: String = method.name

  /** 方法参数列表 */
  val parameters: List<ParameterInfo> = method.parameters.map {
    ParameterInfo(it, it.name, it.type, methodInfo = this)
  }

  /** 可选参数列表 */
  private val optionalParameters: List<OptionalParam> = createOptionalParameters()

  /** ResolvedName注解 */
  val resolvedName: ResolvedName? = method.getAnnotation(ResolvedName::class.java)

  val paramNames: Array<String> = ParameterResolver.resolveNames(method)

  fun getLogicType(): DeleteValue? {
    val flag = method.getAnnotation(Logic::class.java)?.flag
    if (flag != null) {
      return flag
    }
    if (method.getAnnotation(Deleted::class.java) != null) {
      return DeleteValue.Deleted
    }
    if (method.getAnnotation(NotDeleted::class.java) != null) {
      return DeleteValue.NotDeleted
    }
    return null
  }

  fun findOptionalParam(property: String): OptionalParam? {
    return optionalParameters.firstOrNull { it.name == property }
  }

  private fun createOptionalParameters(): MutableList<OptionalParam> {
    val list = mutableListOf<OptionalParam>()
    parameters.forEach {
      if (it.isComplex) {
        list.addAll(
            it.createOptionalParameters(
                parameters.size > 1 || parameters.first().specificParamName != null
            )
        )
      } else if (it.specificParamName != null) {
        list.add(OptionalParam(null, it.specificParamName))
      } else {
        list.add(OptionalParam(null, it.name))
      }
    }
    return list
  }

}

/**
 * 参数信息
 *
 * @author 吴昊
 * @date 2023/12/06
 * @version 1.0
 * @since v4.0.0
 */
data class ParameterInfo(
    val parameter: AnnotatedElement,
    private val elementName: String,
    val type: Class<*>,
    val isComplex: Boolean = ParameterResolver.isComplexType(type),
    private val methodInfo: MethodInfo
) {

  /** 条件注解 */
  val criteria: List<CriteriaInfo> = FieldUtil.getCriteriaInfo(parameter, methodInfo)


  /** Param注解 */
  val specificParamName: String? = parameter.getAnnotation(Param::class.java)?.value

  val name = specificParamName ?: elementName


  fun createOptionalParameters(forcePrefix: Boolean): List<OptionalParam> {
    val list = mutableListOf<OptionalParam>()
    val prefix = specificParamName ?: name
    ReflectUtil.getFields(type).forEach { field ->
      val fieldParam = field.getAnnotation(Param::class.java)
      val paramName = fieldParam?.value
      val name = paramName ?: field.name
      if (forcePrefix) {
        list.add(OptionalParam(prefix, name))
      } else {
        list.add(OptionalParam(specificParamName ?: paramName, name))
      }
    }
    return list.toList()
  }

}

/**
 * 可选参数
 *
 * @author 吴昊
 * @date 2023/12/06
 * @version 1.0
 * @since v4.0.0
 */
class OptionalParam(
    private val prefix: String?,
    internal val name: String
) {

  fun name(): String {
    if (prefix.isNotNullAndNotBlank()) {
      return "$prefix.$name"
    }
    return name
  }

}
