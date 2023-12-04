package com.aegis.mybatis.xmlless.model.component

import com.aegis.mybatis.xmlless.constant.JOIN
import com.aegis.mybatis.xmlless.model.TableName
import javax.persistence.criteria.JoinType

/**
 *
 * @author wuhao
 * @date 2023/12/4 22:43
 * @since v0.0.0
 * @version 1.0
 */
class JoinDeclaration(
    val type: JoinType,
    val joinTable: TableName,
    val joinCondition: String,
    val joinSelect: List<JoinDeclaration> = listOf()
) : ISqlPart {

  override fun toSql(): String {
    val sql = String.format(
        JOIN, type.name,
        joinTable.toSql(),
        joinCondition
    ).trim()
    if (joinSelect.isEmpty()) {
      return sql.trim()
    }
    return (sql + "\n" + joinSelect.joinToString("\n") { it.toSql() }).trim()
  }

}
