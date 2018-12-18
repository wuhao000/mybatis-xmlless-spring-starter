package com.aegis.mybatis.xmlless.model

import com.baomidou.mybatisplus.core.metadata.TableInfo
import javax.persistence.criteria.JoinType

/**
 * Created by 吴昊 on 2018/12/17.
 */
class PropertyJoinInfo(val propertyColumn: ColumnName,
                       joinTable: TableName,
                       type: JoinType,
                       joinProperty: String,
                       targetColumn: String,
                       val groupBy: String? = null) : JoinInfo(
    joinTable, type, joinProperty, targetColumn) {

  override fun getJoinTableInfo(): TableInfo? {
    return null
  }

  override fun selectFields(level: Int, prefix: String?): List<String> {
    return listOf(propertyColumn.toSql())
  }

}
