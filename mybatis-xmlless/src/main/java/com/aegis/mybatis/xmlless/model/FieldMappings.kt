package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.annotations.DeleteValue
import com.aegis.mybatis.xmlless.exception.BuildSQLException
import com.aegis.mybatis.xmlless.model.component.FromDeclaration
import com.aegis.mybatis.xmlless.model.component.JoinConditionDeclaration
import com.aegis.mybatis.xmlless.model.component.JoinDeclaration
import com.aegis.mybatis.xmlless.resolver.ColumnsResolver
import com.baomidou.mybatisplus.core.metadata.TableResolver
import com.baomidou.mybatisplus.core.metadata.TableInfo


/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
data class FieldMappings(
    val mappings: List<FieldMapping>,
    val tableInfo: TableInfo,
    val modelClass: Class<*>
) {

  val tableName = TableResolver.getTableName(tableInfo)

  /**
   * 获取from后的表名称及join信息的语句
   * 例如： t_student LEFT JOIN t_score ON t_score.student_id = t_student.id
   */
  fun fromDeclaration(
      properties: Properties,
      includedTableAlias: List<String>,
      onlyIncludesTables: List<TableName>?,
      limitInSubQuery: Boolean
  ): FromDeclaration {
    val tableName = if (limitInSubQuery) {
      TableName(tableName, tableName.replace('.', '_'))
    } else {
      TableName(tableName, "")
    }
    val declaration = FromDeclaration()
    declaration.tableName = tableName
    val joins = selectJoins(1, properties, includedTableAlias, null, onlyIncludesTables)
    joins.forEach {
      if (it.joinCondition.originTable.name == tableName.name) {
        it.joinCondition.originTable = tableName
      }
    }
    declaration.joins = joins
    return declaration
  }

  /**
   * 获取插入的列名称语句, join属性自动被过滤
   * 例如： t_student.id, t_student.name
   */
  fun insertFields(selectedProperties: Properties): List<String> {
    return if (selectedProperties.isIncludeNotEmpty()) {
      mappings.filter {
        !it.insertIgnore
            && it.property in selectedProperties
      }.map {
        ColumnsResolver.wrapColumn(it.column)
      }
    } else {
      mappings.filter {
        !it.insertIgnore
            && it.property !in selectedProperties.excludes
      }.map {
        ColumnsResolver.wrapColumn(it.column)
      }
    }
  }

  /**
   * 获取插入的对象属性语句，例如：
   * #{name}, #{age}, #{details,typeHandler=com.aegis.project.mybatis.handler.DetailsHandler}
   */
  fun insertProperties(prefix: String? = null, insertProperties: Properties = Properties()): List<String> {
    return when {
      insertProperties.isIncludeNotEmpty() ->
        mappings.filter { !it.insertIgnore && it.property in insertProperties.includes }.map {
          it.getInsertPropertyExpression(prefix)
        }

      else                                 ->
        mappings.filter { !it.insertIgnore && it.property !in insertProperties.excludes }.map {
          it.getInsertPropertyExpression(prefix)
        }
    }
  }


  /**
   * select查询中的join语句
   */
  fun selectJoins(
      level: Int, selectedProperties: Properties = Properties(),
      includedTableAlias: List<String> = listOf(),
      joinTableName: TableName? = null,
      onlyIncludesTables: List<TableName>? = null
  ): List<JoinDeclaration> {
    return mappings.asSequence().filter {
      !it.selectIgnore && it.joinInfo != null && (
          when {
            selectedProperties.isIncludeNotEmpty() -> it.property in selectedProperties
            else                                   -> true
          }) || (it.joinInfo != null && it.joinInfo.joinTable.alias in includedTableAlias)
    }.mapNotNull { it.joinInfo }
        .distinctBy { it.joinTable.alias }
        .filter {
          onlyIncludesTables == null || onlyIncludesTables.map { tableName -> tableName.name }
              .contains(it.joinTable.name)
        }
        .mapNotNull { joinInfo ->
          val joinPropertyName = joinInfo.getJoinProperty(tableInfo)
          val joinProperty = mappings.firstOrNull { it.property == joinPropertyName }
            ?: throw BuildSQLException("无法解析join属性$joinPropertyName")
          val joinTable = joinInfo.joinTable
          when (joinInfo) {
            is PropertyJoinInfo -> {
              JoinDeclaration(
                  joinInfo.type,
                  joinTable,
                  createJoinCondition(joinInfo, joinTableName, joinProperty)
              )
            }

            is ObjectJoinInfo   -> {
              JoinDeclaration(
                  joinInfo.type, joinTable,
                  createJoinCondition(joinInfo, joinTableName, joinProperty),
                  joinInfo.selectJoins(level)
              )
            }

            else                -> null
          }
        }.toList()
  }

  fun getLogicDelFlagValue(logicalType: DeleteValue): Any? {
    val logicDelMapping = this.mappings.find { it.tableFieldInfo.isLogicDelete }
    return if (logicDelMapping != null) {
      when (logicalType) {
        DeleteValue.Deleted -> logicDelMapping.logicDelValue
        else                -> logicDelMapping.logicNotDelValue
      }
    } else {
      when (logicalType) {
        DeleteValue.Deleted -> 1
        else                -> 0
      }
    }
  }

  private fun createJoinCondition(
      joinInfo: JoinInfo,
      joinTableName: TableName?,
      joinProperty: FieldMapping
  ): JoinConditionDeclaration {
    return JoinConditionDeclaration(
        joinTableName ?: TableName(tableName), joinProperty.column,
        joinInfo.joinTable, joinInfo.targetColumn, joinProperty.isJsonArray
    )
  }


}
