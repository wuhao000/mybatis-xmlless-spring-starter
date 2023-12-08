package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.annotations.JoinObject
import com.aegis.mybatis.xmlless.annotations.JoinProperty
import com.aegis.mybatis.xmlless.annotations.MyBatisIgnore
import com.aegis.mybatis.xmlless.config.MappingResolver
import com.aegis.mybatis.xmlless.exception.BuildSQLException
import com.aegis.mybatis.xmlless.kotlin.toUnderlineCase
import com.aegis.mybatis.xmlless.model.component.JoinDeclaration
import com.aegis.mybatis.xmlless.util.FieldUtil
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper
import jakarta.persistence.Transient
import jakarta.persistence.criteria.JoinType
import java.lang.reflect.Type
import java.util.*

/**
 * Created by 吴昊 on 2018/12/17.
 */
class ObjectJoinInfo(
    val selectProperties: Properties,
    joinTable: TableName,
    type: JoinType,
    joinProperty: String,
    targetColumn: String,
    /**  关联表查询的列的别名前缀 */
    val associationPrefix: String,
    /**  join的对象或者属性的类型 */
    javaType: Type
) : JoinInfo(joinTable, type, joinProperty, targetColumn, javaType) {

  override fun getJoinTableInfo(): TableInfo? {
    return TableInfoHelper.getTableInfo(realType())
  }

  override fun selectFields(level: Int, prefix: String?): List<SelectColumn> {
    if (level >= 3) {
      return listOf()
    }
    // 列名称添加前缀防止多表连接的字段名称冲突问题
    val list = wrappedColumns(prefix).map {
      SelectColumn(joinTable, it.column, it.alias, it.type)
    }
    if (hasJoinedProperty()) {
      val realType = this.realType()
      val mappings = MappingResolver.getMappingCache(realType)
      if (mappings != null) {
        val mappingList = if (selectProperties.isIncludeNotEmpty()) {
          mappings.mappings.filter {
            it.joinInfo != null && it.joinInfo is ObjectJoinInfo
                && it.property in selectProperties
          }
        } else {
          mappings.mappings.filter {
            it.joinInfo != null && it.joinInfo is ObjectJoinInfo
                && it.property !in selectProperties.excludes
          }
        }
        return mappingList.map { it.joinInfo!!.selectFields(level + 1, associationPrefix) }.flatten() + list
      }
    }
    return list
  }

  /**
   * 获取层级join语句
   * @param level 当前join的层数
   */
  fun selectJoins(level: Int): List<JoinDeclaration> {
    // 最多支持3级关系
    if (level >= 3) {
      return listOf()
    }
    val realType = this.realType()
    val mappings = MappingResolver.getMappingCache(realType)
    return mappings?.selectJoins(
        level + 1,
        selectProperties,
        listOf(), this.joinTable
    ) ?: listOf()
  }

  private fun hasJoinedProperty(): Boolean {
    val mappings = MappingResolver.getMappingCache(realType()) ?: throw BuildSQLException("无法正确解析join信息：$this")
    return selectProperties.includes.mapNotNull { property ->
      mappings.mappings.firstOrNull { it.property == property }
    }.any { it.joinInfo != null }
  }

  private fun resolveJoinColumns(): List<SelectColumn> {
    val mappings = MappingResolver.getMappingCache(realType()) ?: throw BuildSQLException("无法正确解析join信息：$this")
    val columns = selectProperties.includes.mapNotNull { property ->
      mappings.mappings.firstOrNull { it.property == property }
    }.filter { it.joinInfo == null }.map { it.column }
    return when {
      columns.isNotEmpty() -> columns.map {
        SelectColumn(
            null, it, null, null
        )
      }

      else                 -> TableInfoHelper.getAllFields(realType()).filter {
        FieldUtil.canBeSelect(it)
      }.map {
        val columnName = it.name.toUnderlineCase().lowercase(Locale.getDefault())
        SelectColumn(null, columnName, associationPrefix + columnName, null)
      }
    }
  }

  private fun wrappedColumns(prefix: String? = null): List<SelectColumn> {
    val fullPrefix = listOf(prefix, associationPrefix).filter { !it.isNullOrBlank() }
        .joinToString("")
    return resolveJoinColumns().map {
      when {
        it.alias != null -> if (prefix != null) {
          SelectColumn(
              it.table, it.column, prefix + it.alias, javaType
          )
        } else {
          it
        }

        else             -> SelectColumn(
            it.table, it.column, fullPrefix + it.column, javaType
        )
      }
    }
  }

}
