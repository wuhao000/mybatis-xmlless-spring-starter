package com.aegis.mybatis.xmlless.resolver

import com.aegis.kotlin.toCamelCase
import com.aegis.kotlin.toUnderlineCase
import com.aegis.mybatis.xmlless.constant.SQLKeywords
import com.aegis.mybatis.xmlless.exception.BuildSQLException
import com.aegis.mybatis.xmlless.model.*
import com.aegis.mybatis.xmlless.model.Properties
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Created by 吴昊 on 2018/12/12.
 */
object ColumnsResolver {

  private val LOG = LoggerFactory.getLogger(ColumnsResolver::class.java)

  /**
   * 构建查询的列
   */
  fun resolve(properties: Properties, methodInfo: MethodInfo): List<SelectColumn> {
    return resolveColumns(properties, methodInfo).sortedBy { it.toSql() }
  }

  fun resolveIncludedTables(properties: Properties, methodInfo: MethodInfo): List<String> {
    return resolveColumns(properties, methodInfo).mapNotNull {
      it.table
    }.map { it.getAliasOrName() }
  }

  fun wrapColumn(column: String): String {
    return when {
      column.uppercase(Locale.getDefault()) in SQLKeywords.getValues() -> "`$column`"
      else                                                             -> column
    }
  }

  private fun resolveColumns(
      properties: Properties,
      methodInfo: MethodInfo
  ): List<SelectColumn> {
    val mappings = methodInfo.mappings
    if (LOG.isDebugEnabled) {
      LOG.debug("Available properties for class ${mappings.modelClass}: ${mappings.mappings.map { it.property }}")
      LOG.debug("Fetch properties for class ${mappings.modelClass}: $properties")
    }
    return selectFields(properties, methodInfo)
  }


  /**
   * 获取select查询的列名称语句
   */
  private fun selectFields(properties: Properties, methodInfo: MethodInfo): List<SelectColumn> {
    if (properties.isIncludeNotEmpty()) {
      return properties.includes.map {
        resolveColumnByPropertyName(it, methodInfo = methodInfo)
      }.flatten()
    }
    return methodInfo.mappings.mappings.asSequence().filter {
      !it.selectIgnore && it.property !in properties.excludes
    }.map { mapping ->
      when {
        mapping.joinInfo != null -> mapping.joinInfo.selectFields(1)
        else                     -> listOf(SelectColumn(TableName(methodInfo.mappings.tableName), mapping.column))
      }
    }.flatten().filter { it.column.isNotBlank() }.distinctBy { it.toSql() }.toList()
  }


  /**
   * 根据属性名称获取数据库表的列名称，返回的列名称包含表名称
   */
  fun resolveColumnByPropertyName(
      property: String,
      validate: Boolean = true,
      methodInfo: MethodInfo
  ): List<SelectColumn> {
    val mappings = methodInfo.mappings
    // 如果属性中存在.则该属性表示一个关联对象的属性，例如student.subjectId
    // 目前仅支持一层关联关系
    if (property.contains(".")) {
      val joinedPropertyWords = property.split(".")
      when {
        joinedPropertyWords.size <= 2 -> {
          val objectProperty = joinedPropertyWords[0]
          val joinProperty = joinedPropertyWords[1]
          val col = findFromMappings(mappings.mappings, objectProperty, joinProperty)
          if (col != null) {
            return listOf(col)
          } else {
            val originCol = findFromMappings(methodInfo.modelMappings.mappings, objectProperty, joinProperty)
            if (originCol != null) {
              return listOf(originCol)
            }
          }
        }

        else                          -> error("暂不支持多级连接属性：$property")
      }
    }

    // 匹配持久化对象的属性查找列名
    val column = resolveFromFieldInfo(property, methodInfo)
    if (column != null) {
      return listOf(SelectColumn(TableName(mappings.tableName), column, null, null))
    }
    // 从关联属性中匹配
    val resolvedFromJoinProperty = resolveFromPropertyJoinInfo(mappings.mappings, property)
    if (resolvedFromJoinProperty != null) {
      return listOf(resolvedFromJoinProperty)
    }

    // 从关联对象中匹配
    val resolvedFromJoinObject = resolveFromObjectJoinInfo(mappings.mappings, property, methodInfo)
    if (resolvedFromJoinObject.isNotEmpty()) {
      return resolvedFromJoinObject
    }
    if (!validate) {
      return listOf(SelectColumn(TableName(mappings.tableName), property.toUnderlineCase(), null, null))
    } else {
      throw BuildSQLException("无法解析持久化类${mappings.modelClass.simpleName}的属性${property}对应的列名称, 持久化类或关联对象中不存在此属性")
    }
  }

  private fun findFromMappings(
      mappings: List<FieldMapping>,
      objectProperty: String,
      joinProperty: String,
  ): SelectColumn? {
    val it = mappings.firstOrNull {
      it.joinInfo is ObjectJoinInfo && it.property == objectProperty
    } ?: return null
    return SelectColumn(
        it.joinInfo!!.joinTable,
        joinProperty.toUnderlineCase().lowercase(Locale.getDefault()),
        if (it.joinInfo is ObjectJoinInfo && it.joinInfo.associationPrefix.isNotBlank()) {
          it.joinInfo.associationPrefix + joinProperty.toUnderlineCase().lowercase(Locale.getDefault())
        } else {
          null
        },
        it.joinInfo.javaType
    )
  }


  /**
   * 从表信息中解析对象属性名称对应的数据库表的列名称
   */
  private fun resolveFromFieldInfo(property: String, methodInfo: MethodInfo): String? {
    val column = methodInfo.mappings.mappings.firstOrNull { it.joinInfo == null && it.property == property }?.column
    if (column != null) {
      return column
    }
    val originColumn = methodInfo.modelMappings.mappings.firstOrNull {
      it.joinInfo == null && it.property == property
    }?.column
    if (originColumn != null) {
      return originColumn
    }
    return null
  }

  private fun resolveFromObjectJoinInfo(mappings: List<FieldMapping>, property: String, methodInfo: MethodInfo): List<SelectColumn> {
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
        return listOf(
            SelectColumn(
                bestObjectJoinMapping.joinInfo!!.joinTable, bestObjectJoinMapping.joinInfo
                .resolveColumnProperty(maybeJoinProperty), null, null
            )
        )
      }
    }
    // 如果持久化对象中找不到这个属性，在关联的对象中匹配同名属性，但是该属性名称必须唯一，即不能在多个关联对象中都存在该名称的属性
    // 例如，当前的model是Student，有一个名为scores的属性类型为List<Score>,表示该学生的成绩列表,
    // Score对象中有一个属性subjectId，那么subjectId可以匹配到关联对象的subjectId属性上
    val matchedJoinInfos = mappings.filter {
      it.joinInfo is ObjectJoinInfo
    }.filter { mapping ->
      mapping.joinInfo?.getJoinTableInfo(methodInfo)?.fieldList?.firstOrNull { it.property == property } != null
    }.map { it.joinInfo as ObjectJoinInfo }
    return when {
      matchedJoinInfos.size > 1  -> throw BuildSQLException(
          "在${matchedJoinInfos.joinToString(",") { it.realType().simpleName }}发现了相同的属性$property, " +
              "无法确定属性对应的表及字段"
      )

      matchedJoinInfos.size == 1 -> listOf(
          SelectColumn(
              matchedJoinInfos.first().joinTable,
              matchedJoinInfos.first().resolveColumnProperty(property), null, null
          )
      )

      else                       -> listOf()
    }
  }

  /**
   * 从关联属性中解析对应的关联表的列名称
   */
  private fun resolveFromPropertyJoinInfo(mappings: List<FieldMapping>, property: String): SelectColumn? {
    val matchedMapping = mappings.firstOrNull {
      it.joinInfo != null && it.joinInfo is PropertyJoinInfo
          && it.property == property
    }
    if (matchedMapping?.joinInfo != null) {
      val joinInfo = matchedMapping.joinInfo as PropertyJoinInfo
      return SelectColumn(
          joinInfo.joinTable,
          joinInfo.propertyColumn.name, null, joinInfo.javaType
      )
    }
    return null
  }
}
