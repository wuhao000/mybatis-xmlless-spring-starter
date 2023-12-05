package com.aegis.mybatis.xmlless.model.component

import com.aegis.mybatis.xmlless.model.TableName

/**
 *
 * @author wuhao
 * @date 2023/12/5 9:59
 * @since v0.0.0
 * @version 1.0
 */
class JoinConditionDeclaration(
    var originTable: TableName,
    private val originColumn: String,
    val targetTable: TableName,
    private val targetColumn: String,
    private val isJson: Boolean = false
) : ISqlPart {

  override fun toSql(): String {
    if (isJson) {
      return "JSON_CONTAINS(${originTable.getAliasOrName()}.${originColumn}, CAST(${
        targetTable.getAliasOrName()
      }.${targetColumn} AS JSON), '$')"
    }
    return "${targetTable.getAliasOrName()}.${targetColumn} = ${
      originTable.getAliasOrName()
    }.${originColumn}"
  }

}
