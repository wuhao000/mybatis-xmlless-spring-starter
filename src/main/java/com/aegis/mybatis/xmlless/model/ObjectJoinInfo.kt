package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.annotations.JoinObject
import com.aegis.mybatis.xmlless.annotations.JoinProperty
import com.aegis.mybatis.xmlless.annotations.SelectIgnore
import com.aegis.mybatis.xmlless.config.MappingResolver
import com.aegis.mybatis.xmlless.exception.BuildSQLException
import com.aegis.mybatis.xmlless.kotlin.toUnderlineCase
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.toolkit.TableInfoHelper
import java.lang.reflect.Type
import javax.persistence.Transient
import javax.persistence.criteria.JoinType

/**
 * Created by 吴昊 on 2018/12/17.
 */
class ObjectJoinInfo(
    private val selectProperties: List<String>,
    joinTable: TableName,
    type: JoinType,
    joinProperty: String,
    targetColumn: String,
    /**  关联表查询的列的别名前缀 */
    val associationPrefix: String? = null,
    /**  join的对象或者属性的类型 */
    javaType: Type
) : JoinInfo(joinTable, type, joinProperty, targetColumn,javaType) {

  override fun getJoinTableInfo(): TableInfo? {
    return TableInfoHelper.getTableInfo(realType())
  }

  override fun selectFields(level: Int, prefix: String?): List<String> {
    if (level >= 3) {
      return listOf()
    }
    val list = wrappedColumns(prefix).map {
      joinTable.alias + '.' + it
    }
    if (hasJoinedProperty()) {
      val realType = this.realType()
      val mappings = MappingResolver.getMappingCache(realType)
      if (mappings != null) {
        return mappings.mappings.filter {
          it.joinInfo != null && it.joinInfo is ObjectJoinInfo
        }.map { it.joinInfo!!.selectFields(level + 1, associationPrefix) }.flatten() + list
      }
    }
    return list
  }

  /**
   * 获取层级join语句
   * @param level 当前join的层数
   */
  fun selectJoins(level: Int): String {
    // 最多支持3级关系
    if (level >= 3) {
      return ""
    }
    val realType = this.realType()
    val mappings = MappingResolver.getMappingCache(realType)
    return mappings?.selectJoins(level + 1, selectProperties.toList()) ?: ""
  }

  private fun hasJoinedProperty(): Boolean {
    val mappings = MappingResolver.getMappingCache(realType()) ?: throw BuildSQLException("无法正确解析join信息：$this")
    return selectProperties.mapNotNull { property ->
      mappings.mappings.firstOrNull { it.property == property }
    }.any { it.joinInfo != null }
  }

  private fun resolveJoinColumns(): List<String> {
    val mappings = MappingResolver.getMappingCache(realType()) ?: throw BuildSQLException("无法正确解析join信息：$this")
    val columns = selectProperties.mapNotNull { property ->
      mappings.mappings.firstOrNull { it.property == property }
    }.filter { it.joinInfo == null }.map { it.column }
    return when {
      columns.isNotEmpty() -> columns.toList()
      else                 -> TableInfoHelper.getAllFields(realType()).filter {
        !it.isAnnotationPresent(SelectIgnore::class.java)
            && !it.isAnnotationPresent(Transient::class.java)
            && !it.isAnnotationPresent(JoinObject::class.java)
            && !it.isAnnotationPresent(JoinProperty::class.java)
      }.map {
        val columnName = it.name.toUnderlineCase().toLowerCase()
        "$columnName AS $associationPrefix$columnName"
      }
    }
  }

  private fun wrappedColumns(prefix: String? = null): List<String> {
    val fullPrefix = listOf(prefix, associationPrefix).filter { !it.isNullOrBlank() }
        .joinToString("")
    return resolveJoinColumns().toList().map {
      when {
        it.toUpperCase().contains(" AS ") -> it
        else                              -> "$it AS $fullPrefix$it"
      }
    }
  }

}
