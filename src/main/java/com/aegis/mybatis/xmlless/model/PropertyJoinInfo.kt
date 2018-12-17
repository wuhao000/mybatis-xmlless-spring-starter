package com.aegis.mybatis.xmlless.model

import com.baomidou.mybatisplus.core.metadata.TableInfo
import java.lang.reflect.Field
import javax.persistence.criteria.JoinType

/**
 * Created by 吴昊 on 2018/12/17.
 */
class PropertyJoinInfo(val propertyColumn: String,
                       joinTable: String,
                       joinTableAlias: String?,
                       type: JoinType,
                       joinProperty: String,
                       targetColumn: String,
                       val field: Field) : JoinInfo(
    joinTable, joinTableAlias, type, joinProperty, targetColumn) {

  override fun getJoinTableInfo(): TableInfo? {
    return null
  }

  override fun selectFields(level: Int, prefix: String?): List<String> {
    return listOf(when {
      propertyColumn.toUpperCase().contains(" AS ") -> propertyColumn
      else                                          -> "$propertyColumn AS ${field.name}"
    })
  }

}
