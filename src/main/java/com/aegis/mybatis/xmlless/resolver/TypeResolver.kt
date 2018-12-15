package com.aegis.mybatis.xmlless.resolver

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

object TypeResolver {

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
