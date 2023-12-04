package com.aegis.mybatis.xmlless.model.component

import com.aegis.mybatis.xmlless.model.TableName


/**
 *
 * @author wuhao
 * @date 2023/12/4 22:39
 * @since v0.0.0
 * @version 1.0
 */
class FromDeclaration : ISqlPart {

  var tableName: TableName = TableName("", "", null)
  var joins: List<JoinDeclaration> = listOf()

  override fun toSql(): String {
    if (joins.isEmpty()) {
      return tableName.toSql()
    }
    return tableName.toSql() + "\n" + joins.joinToString("\n") { it.toSql() }
  }

  fun getTables(): List<TableName> {
    return listOf(tableName) + joins.map { it.joinTable }
  }

}
