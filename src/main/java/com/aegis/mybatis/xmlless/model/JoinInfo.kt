package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.kotlin.toUnderlineCase
import com.aegis.mybatis.xmlless.enums.JoinPropertyType
import com.aegis.mybatis.xmlless.enums.JoinType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 *
 * Created by 吴昊 on 2018-12-06.
 *
 * @author 吴昊
 * @since 0.0.1
 */
class JoinInfo(val selectColumns: List<String>,
               private val joinTable: String,
               private val joinTableAlias: String?,
               val type: JoinType,
               val joinProperty: String,
               val targetColumn: String,
               val joinPropertyType: JoinPropertyType) {

  var associationPrefix: String? = null
  var javaType: Type? = null

  fun joinTable(): String {
    return joinTableAlias ?: joinTable
  }

  fun joinTableDeclaration(): String {
    return if (joinTableAlias != null) {
      "$joinTable AS $joinTableAlias"
    } else {
      joinTable
    }
  }

  fun rawType(): Class<*>? {
    val type = javaType
    return when (type) {
      is Class<*>          -> type
      is ParameterizedType -> type.rawType as Class<*>
      else                 -> null
    }
  }

  fun realType(): Class<*>? {
    val type = javaType
    return when (type) {
      is Class<*>          -> type
      is ParameterizedType -> type.actualTypeArguments[0] as Class<*>
      else                 -> null
    }
  }

  fun resolveColumnProperty(property: String): Any? {
    return property.toUnderlineCase().toLowerCase()
  }

}
