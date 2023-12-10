package com.aegis.mybatis.xmlless.resolver

import cn.hutool.core.util.ReflectUtil
import com.aegis.mybatis.xmlless.model.MatchedParameter
import com.aegis.mybatis.xmlless.model.MethodInfo
import com.aegis.mybatis.xmlless.model.ParameterInfo
import org.apache.ibatis.reflection.ParamNameResolver
import org.apache.ibatis.session.Configuration
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.lang.reflect.Method

/**
 *
 * Created by 吴昊 on 2018/12/19.
 *
 * @author 吴昊
 * @since 1.4.3
 */
object ParameterResolver {

  private val PARAMETER_NAMES_CACHE = HashMap<Method, Array<String>>()

  fun isComplexType(type: Class<*>): Boolean {
    val realType = TypeResolver.resolveRealType(type)
    return (!realType.name.startsWith("java.")
        && realType != Void.TYPE
        && !realType.isPrimitive
        && !realType.isArray
        && !realType.isEnum
        && !Pageable::class.java.isAssignableFrom(realType)
        && !Page::class.java.isAssignableFrom(realType)
        && realType != Sort::class.java && realType != Sort.Order::class.java)
  }

  /**
   * 解析参数名称对象的参数或者对象属性
   */
  fun resolve(paramName: String, methodInfo: MethodInfo): MatchedParameter? {
    val paramNames = methodInfo.paramNames
    val paramIndex = paramNames.indexOf(paramName)
    // 当参数名称在方法的直接参数列表中
    if (paramIndex >= 0) {
      return MatchedParameter(methodInfo.parameters[paramIndex], paramNames[paramIndex])
    } else {
      // 当参数名称不再方法的直接参数列表中时， 参数可能是被包装成对象，因此在复杂对象中查找
      // 复杂对象是排除了 基本类型、java包下的类型、数组类型、枚举类型、及Pageable、Sort和Order等spring data类型的剩余参数
      val complexParameterMap = HashMap<Int, ParameterInfo>()
      methodInfo.parameters.forEachIndexed { index, parameter ->
        if (isComplexType(parameter.type)) {
          complexParameterMap[index] = parameter
        }
      }
      complexParameterMap.forEach { complexParameterPair ->
        val realType = TypeResolver.resolveRealType(complexParameterPair.value.type)
        val matchedProperty = ReflectUtil.getFields(realType).firstOrNull { property ->
          property.name == paramName ||
              "${complexParameterPair.value.name}.${property.name}" == paramName
        }
        if (matchedProperty != null) {
          return MatchedParameter(complexParameterPair.value, paramNames[complexParameterPair.key], matchedProperty)
        }
      }
    }
    ReflectUtil.getFields(methodInfo.modelClass).forEach { field ->
      if (field.name == paramName) {
        return MatchedParameter(
            ParameterInfo(
                field,
                field.name,
                field.type,
                methodInfo = methodInfo
            ),
            paramName,
            field
        )
      }
    }
    return null
  }

  fun resolveNames(method: Method): Array<String> {
    return if (PARAMETER_NAMES_CACHE[method] != null) {
      PARAMETER_NAMES_CACHE[method]!!
    } else {
      val names = ParamNameResolver(
          Configuration().apply {
            this.isUseActualParamName = true
          }, method
      ).names
      PARAMETER_NAMES_CACHE[method] = names
      names
    }
  }

}
