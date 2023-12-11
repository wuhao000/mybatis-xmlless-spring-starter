package com.aegis.mybatis.xmlless.model

import com.baomidou.mybatisplus.core.metadata.TableInfo
import jakarta.persistence.criteria.JoinType


/**
 *
 * @author 吴昊
 * @date 2023/12/11 18:55
 * @since v0.0.0
 * @version 1.0
 */
class HiddenJoinInfo(
    table: TableName,
    type: JoinType,
    joinProperty: String,
    targetColumn: String,
    javaType: Class<*>
) : JoinInfo(table, type, joinProperty, targetColumn, javaType) {

  override fun getJoinTableInfo(methodInfo: MethodInfo): TableInfo? {
    return null
  }

  override fun selectFields(level: Int, prefix: String?): List<SelectColumn> {
    return listOf()
  }

}
