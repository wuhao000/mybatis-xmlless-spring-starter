package com.aegis.mybatis.xmlless.resolver

import com.aegis.mybatis.xmlless.model.MatchedParameter
import org.apache.ibatis.reflection.ParamNameResolver
import org.apache.ibatis.session.Configuration
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.javaType

/**
 *
 * Created by 吴昊 on 2018/12/19.
 *
 * @author 吴昊
 * @since 1.4.3
 */
object ParameterResolver {

  private val PARAMETER_NAMES_CACHE = HashMap<KFunction<*>, Array<String>>()

  fun isComplexParameter(parameter: KParameter): Boolean {
    val realType = TypeResolver.resolveRealType(parameter.type.javaType)
    return (!realType.name.startsWith("java.")
        && !realType.isPrimitive
        && !realType.isArray
        && !realType.isEnum && !Pageable::class.java.isAssignableFrom(realType)
        && realType != Sort::class.java && realType != Sort.Order::class.java)
  }

  /**
   * 解析参数名称对象的参数或者对象属性
   */
  fun resolve(paramName: String, function: KFunction<*>): MatchedParameter? {
    val paramNames = resolveNames(function)
    val paramIndex = paramNames.indexOf(paramName)
    // 当参数名称在方法的直接参数列表中
    if (paramIndex >= 0) {
      return MatchedParameter(function.valueParameters[paramIndex], paramNames[paramIndex])
    } else {
      // 当参数名称不再方法的直接参数列表中时， 参数可能是被包装成对象，因此在复杂对象中查找
      // 复杂对象是排除了 基本类型、java包下的类型、数组类型、枚举类型、及Pageable、Sort和Order等spring data类型的剩余参数
      val complexParameterMap = HashMap<Int, KParameter>()
      function.valueParameters.forEachIndexed { index, parameter ->
        if (isComplexParameter(parameter)) {
          complexParameterMap[index] = parameter
        }
      }
      complexParameterMap.forEach {
        val realType = TypeResolver.resolveRealType(it.value.type)
        val matchedProperty = realType.declaredMemberProperties.firstOrNull { property -> property.name == paramName }
        if (matchedProperty != null) {
          return MatchedParameter(it.value, paramNames[it.key], matchedProperty)
        }
      }
    }
    return null
  }

  fun resolveNames(function: KFunction<*>): Array<String> {
    return if (PARAMETER_NAMES_CACHE[function] != null) {
      PARAMETER_NAMES_CACHE[function]!!
    } else {
      val names = ParamNameResolver(
          Configuration().apply {
            this.isUseActualParamName = true
          }, function.javaMethod
      ).names
      PARAMETER_NAMES_CACHE[function] = names
      names
    }
  }

}
