package com.aegis.mybatis.xmlless.model.component

import com.aegis.mybatis.xmlless.constant.SUB_QUERY
import com.aegis.mybatis.xmlless.model.TableName

/**
 *
 * @author 吴昊
 * @date 2023/12/4 23:20
 * @since v0.0.0
 * @version 1.0
 */
class SubQueryDeclaration(
    private val tableName: TableName,
    private val where: WhereDeclaration,
    private val limit: String,
    val defaultFrom: FromDeclaration)
  : ISqlPart {

  override fun toSql(): String {
    return String.format(
        SUB_QUERY, tableName.name,
        where.toSql(), limit, getFromSql()
    )
  }

  private fun getFromSql(): String {
    if (defaultFrom.joins.isEmpty()) {
      return defaultFrom.tableName.getAliasOrName()
    }
    return defaultFrom.tableName.getAliasOrName() + "\n" + defaultFrom.joins.joinToString("\n") { it.toSql() }
  }

}
