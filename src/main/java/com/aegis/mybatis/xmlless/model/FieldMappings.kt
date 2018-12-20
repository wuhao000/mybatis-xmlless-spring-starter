package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.constant.JOIN
import com.aegis.mybatis.xmlless.exception.BuildSQLException
import com.aegis.mybatis.xmlless.kotlin.toCamelCase
import com.aegis.mybatis.xmlless.kotlin.toUnderlineCase
import com.baomidou.mybatisplus.core.metadata.TableInfo


/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
@Suppress("ArrayInDataClass")
data class FieldMappings(val mappings: List<FieldMapping>,
                         val tableInfo: TableInfo,
                         var modelClass: Class<*>) {

  /**
   * 获取from后的表名称及join信息的语句
   * 例如： t_student LEFT JOIN t_score ON t_score.student_id = t_student.id
   */
  fun fromDeclaration(properties: List<String>, includedTableAlias: List<String>): String {
    val joins = selectJoins(1, properties, includedTableAlias)
    return if (joins.isNotBlank()) {
      tableInfo.tableName + "\n" + selectJoins(1, properties, includedTableAlias)
    } else {
      tableInfo.tableName
    }
  }

  /**
   * 获取插入的列名称语句, join属性自动被过滤
   * 例如： t_student.id, t_student.name
   */
  fun insertFields(): String {
    return mappings.filter { !it.insertIgnore }.joinToString(",") {
      it.column
    }
  }

  /**
   * 获取插入的对象属性语句，例如：
   * #{name}, #{age}, #{details,typeHandler=com.aegis.project.mybatis.handler.DetailsHandler}
   */
  fun insertProperties(prefix: String? = null, insertProperties: List<String>? = null): String {
    return when {
      insertProperties != null && insertProperties.isNotEmpty() -> mappings.filter { !it.insertIgnore && it.property in insertProperties }.joinToString(",") {
        it.getPropertyExpression(prefix)
      }
      else                                                      -> mappings.filter { !it.insertIgnore }.joinToString(",") {
        it.getPropertyExpression(prefix)
      }
    }
  }

  /**
   * 根据属性名称获取数据库表的列名称，返回的列名称包含表名称
   */
  fun resolveColumnByPropertyName(property: String, validate: Boolean = true): List<SelectColumn> {
    // 如果属性中存在.则该属性表示一个关联对象的属性，例如student.subjectId
    // 目前仅支持一层关联关系
    if (property.contains(".")) {
      val joinedPropertyWords = property.split(".")
      when {
        joinedPropertyWords.size <= 2 -> {
          val objectProperty = joinedPropertyWords[0]
          val joinProperty = joinedPropertyWords[1]
          mappings.firstOrNull {
            it.joinInfo is ObjectJoinInfo && it.property == objectProperty
          }?.let {
            return listOf(SelectColumn(it.joinInfo!!.joinTable.alias,
                joinProperty.toUnderlineCase().toLowerCase(),
                null,
                it.joinInfo.javaType))
          }
        }
        else                          -> throw IllegalStateException("暂不支持多级连接属性：$property")
      }
    }
    // 匹配持久化对象的属性查找列名
    val column = resolveFromFieldInfo(property)
    if (column != null) {
      // 非关联表属性
      if (mappings.none { it.joinInfo == null && it.column == column } && validate) {
        throw IllegalStateException("无法解析属性类${modelClass.simpleName}的属性${property}对应的列名称, 持久化类或关联对象中不存在此属性")
      }
      return listOf(SelectColumn(tableInfo.tableName, column, null, null))
    } else {
      // 从关联属性中匹配
      val resolvedFromJoinProperty = resolveFromPropertyJoinInfo(property)
      if (resolvedFromJoinProperty != null) {
        return listOf(resolvedFromJoinProperty)
      }
      // 从关联对象中匹配
      val resolvedFromJoinObject = resolveFromObjectJoinInfo(property)
      if (resolvedFromJoinObject.isNotEmpty()) {
        return resolvedFromJoinObject
      }
    }
    if (validate) {
      throw BuildSQLException("无法解析属性类${modelClass.simpleName}的属性${property}对应的列名称, 持久化类或关联对象中不存在此属性")
    } else {
      return listOf(SelectColumn(null, column ?: "", null, null))
    }
  }

  /**
   * 获取select查询的列名称语句
   */
  fun selectFields(): List<SelectColumn> {
    return mappings.filter { !it.selectIgnore }.map { mapping ->
      when {
        mapping.joinInfo != null -> mapping.joinInfo.selectFields(1)
        else                     -> listOf(SelectColumn(tableInfo.tableName, mapping.column))
      }
    }.flatten().filter { it.column.isNotBlank() }.distinctBy { it.toSql() }
  }

  /**
   * select查询中的join语句
   */
  fun selectJoins(level: Int, selectedProperties: List<String>? = null,
                  includedTableAlias: List<String> = listOf()): String {
    return mappings.filter {
      !it.selectIgnore && it.joinInfo != null && (
          when {
            selectedProperties != null && selectedProperties.isNotEmpty() -> it.property in selectedProperties
            else                                                          -> true
          }) || (it.joinInfo != null && it.joinInfo.joinTable.alias in includedTableAlias)
    }.mapNotNull { it.joinInfo }
        .distinctBy { it.joinTable.alias }
        .joinToString("\n") { joinInfo ->
          val joinProperty = joinInfo.getJoinProperty(tableInfo)
          val col = mappings.firstOrNull { it.property == joinProperty }?.column
              ?: throw BuildSQLException("无法解析join属性$joinProperty")
          val joinTable = joinInfo.joinTable
          when (joinInfo) {
            is PropertyJoinInfo -> (String.format(
                JOIN, joinInfo.type.name,
                joinTable.toSql(),
                joinInfo.joinTable.alias,
                joinInfo.targetColumn,
                tableInfo.tableName, col
            )).trim()
            is ObjectJoinInfo   -> (String.format(
                JOIN, joinInfo.type.name,
                joinTable.toSql(),
                joinInfo.joinTable.alias,
                joinInfo.targetColumn,
                tableInfo.tableName, col
            ) + "\n" + joinInfo.selectJoins(level)).trim()
            else                -> ""
          }

        }
  }

  /**
   * 从表信息中解析对象属性名称对应的数据库表的列名称
   */
  private fun resolveFromFieldInfo(property: String): String? {
    return mappings.firstOrNull { it.joinInfo == null && it.property == property }?.column
  }

  private fun resolveFromObjectJoinInfo(property: String): List<SelectColumn> {
    // 如果持久化对象中找不到这个属性，在关联的对象中进行匹配，匹配的规则是，关联对象的字段名称+属性名称
    // 例如，当前的model是Student，有一个名为scores的属性类型为List<Score>,表示该学生的成绩列表,
    // Score对象中有一个属性subjectId，那么scoresSubjectId可以匹配到关联对象的subjectId属性上
    // 这个适用于主对象和关联对象中存在同名属性，而需要查询关联对象属性或以关联对象属性作为条件判断的情况
    val joinMapping = mappings.firstOrNull {
      it.joinInfo != null && it.joinInfo is ObjectJoinInfo
          && it.property == property
    }
    if (joinMapping != null) {
      return joinMapping.joinInfo!!.selectFields(1, null)
    }
    val bestObjectJoinMapping = mappings.filter {
      it.joinInfo != null && it.joinInfo is ObjectJoinInfo
    }.firstOrNull { property.startsWith(it.property) }
    if (bestObjectJoinMapping != null) {
      val maybeJoinPropertyPascal = property.replaceFirst(bestObjectJoinMapping.property, "")
      if (maybeJoinPropertyPascal.isNotBlank() && maybeJoinPropertyPascal.first() in 'A'..'Z') {
        val maybeJoinProperty = maybeJoinPropertyPascal.toCamelCase()
        return listOf(SelectColumn(bestObjectJoinMapping.joinInfo!!.joinTable.alias, bestObjectJoinMapping.joinInfo
            .resolveColumnProperty(maybeJoinProperty), null, null))
      }
    }
    // 如果持久化对象中找不到这个属性，在关联的对象中匹配同名属性，但是该属性名称必须唯一，即不能在多个关联对象中都存在该名称的属性
    // 例如，当前的model是Student，有一个名为scores的属性类型为List<Score>,表示该学生的成绩列表,
    // Score对象中有一个属性subjectId，那么subjectId可以匹配到关联对象的subjectId属性上
    val matchedJoinInfos = mappings.filter {
      it.joinInfo is ObjectJoinInfo
    }.filter { mapping ->
      mapping.joinInfo?.getJoinTableInfo()?.fieldList?.firstOrNull { it.property == property } != null
    }.map { it.joinInfo as ObjectJoinInfo }
    return when {
      matchedJoinInfos.size > 1  -> throw BuildSQLException("在${matchedJoinInfos.joinToString(",") { it.realType().simpleName }}发现了相同的属性$property, " +
          "无法确定属性对应的表及字段")
      matchedJoinInfos.size == 1 -> listOf(SelectColumn(matchedJoinInfos.first().joinTable.alias,
          matchedJoinInfos.first().resolveColumnProperty(property), null, null))
      else                       -> listOf()
    }
  }

  /**
   * 从关联属性中解析对应的关联表的列名称
   */
  private fun resolveFromPropertyJoinInfo(property: String): SelectColumn? {
    val underlineProperty = property.toUnderlineCase().toLowerCase()
    return mappings.mapNotNull { it.joinInfo }.filter {
      it is PropertyJoinInfo
    }.map { it as PropertyJoinInfo }
        .firstOrNull { it.propertyColumn.alias == underlineProperty }
        ?.let {
          SelectColumn(it.joinTable.alias, it.propertyColumn.name, it.propertyColumn.alias, it.javaType)
        }
  }

}
