package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.exception.BuildSQLException
import com.aegis.mybatis.xmlless.kotlin.toUnderlineCase
import com.baomidou.mybatisplus.core.metadata.TableInfo
import javax.persistence.criteria.JoinType

/**
 *
 * Created by 吴昊 on 2018-12-06.
 *
 * @author 吴昊
 * @since 0.0.1
 */
abstract class JoinInfo(private val joinTable: String,
                        private val joinTableAlias: String?,
                        val type: JoinType,
                        private val joinProperty: String,
                        val targetColumn: String) {

  /**
   * 返回用于关联表的属性名称
   * 如果属性为空，则默认使用主键属性
   */
  fun getJoinProperty(tableInfo: TableInfo): String {
    return when {
      joinProperty.isEmpty() -> tableInfo.keyProperty ?: throw BuildSQLException("无法解析${tableInfo.clazz}的主键属性")
      else                   -> joinProperty
    }
  }

  /**
   * 获取连接的表的表信息
   */
  abstract fun getJoinTableInfo(): TableInfo?

  /**
   * 获取连接的表的别名
   */
  fun joinTable(): String {
    return joinTableAlias ?: joinTable
  }

  fun joinTableDeclaration(): TableName {
    val tableNameSplits = joinTable.split("\\s+")
    return when {
      tableNameSplits.size == 1                                             -> TableName(joinTable, joinTable)
      tableNameSplits.size == 3 && tableNameSplits[1].toUpperCase() == "AS" -> TableName(tableNameSplits[0], tableNameSplits[1])
      else                                                                  -> throw BuildSQLException("非法的数据库表名称$joinTable")
    }
  }

  fun resolveColumnProperty(property: String): Any? {
    return property.toUnderlineCase().toLowerCase()
  }

  abstract fun selectFields(level: Int, prefix: String? = null): List<String>




}
