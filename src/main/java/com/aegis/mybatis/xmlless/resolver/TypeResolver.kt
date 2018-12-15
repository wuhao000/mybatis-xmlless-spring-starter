package com.aegis.mybatis.xmlless.resolver

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * 类型解析
 * @author 吴昊
 * @since 0.0.7
 */
object TypeResolver {

  /**
   * 解析带单个泛型参数的类型的泛型类型，如果没有泛型则返回参数类型
   */
  fun resolveRealType(type: Type?): Class<*>? {
    if (type == null) {
      return null
    }
    return when (type) {
      is Class<*>          -> type
      is ParameterizedType -> type.actualTypeArguments[0] as Class<*>
      else                 -> null
    }
  }

}
