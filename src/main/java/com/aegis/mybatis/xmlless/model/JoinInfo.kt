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
 * @param joinTable 连接的表名称
 * @param type 连接类型： LEFT,RIGHT,INNER
 * @param joinProperty 主表对应的持久化类中用于连接的属性名称
 * @param targetColumn 连接表中用于连接的数据库字段名称
 */
abstract class JoinInfo(
    val joinTable: TableName,
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

  fun resolveColumnProperty(property: String): Any? {
    return property.toUnderlineCase().toLowerCase()
  }

  abstract fun selectFields(level: Int, prefix: String? = null): List<String>

}
