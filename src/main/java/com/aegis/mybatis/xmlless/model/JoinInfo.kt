package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.enums.JoinPropertyType
import com.aegis.mybatis.xmlless.enums.JoinType
import com.aegis.mybatis.xmlless.kotlin.toUnderlineCase
import com.aegis.mybatis.xmlless.resolver.TypeResolver
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.toolkit.TableInfoHelper
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

  fun getJoinTableInfo(): TableInfo? {
    val type = realType()
    return when {
      type != null -> TableInfoHelper.getTableInfo(type)
      else         -> null
    }
  }

  fun joinTable(): String {
    return joinTableAlias ?: joinTable
  }

  fun joinTableDeclaration(): String {
    return when {
      joinTableAlias != null -> "$joinTable AS $joinTableAlias"
      else                   -> joinTable
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
    return TypeResolver.resolveRealType(javaType)
  }

  fun resolveColumnProperty(property: String): Any? {
    return property.toUnderlineCase().toLowerCase()
  }

}
