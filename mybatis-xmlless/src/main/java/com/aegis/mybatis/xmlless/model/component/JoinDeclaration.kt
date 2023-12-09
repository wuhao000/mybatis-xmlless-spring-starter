package com.aegis.mybatis.xmlless.model.component

import com.aegis.mybatis.xmlless.constant.JOIN
import com.aegis.mybatis.xmlless.model.TableName
import jakarta.persistence.criteria.JoinType

/**
 *
 * @author 吴昊
 * @date 2023/12/4 22:43
 * @since v0.0.0
 * @version 1.0
 */
class JoinDeclaration(
    private val type: JoinType,
    val joinTable: TableName,
    val joinCondition: JoinConditionDeclaration,
    private val joinSelect: List<JoinDeclaration> = listOf()
) : ISqlPart {

  override fun toSql(): String {
    val sql = String.format(
        JOIN, type.name,
        joinTable.toSql(),
        joinCondition.toSql()
    ).trim()
    if (joinSelect.isEmpty()) {
      return sql.trim()
    }
    return (sql + "\n" + joinSelect.joinToString("\n") { it.toSql() }).trim()
  }

}
