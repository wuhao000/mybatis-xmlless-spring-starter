package com.aegis.mybatis.xmlless.model

import com.aegis.kotlin.toCamelCase
import com.aegis.kotlin.toUnderlineCase
import com.aegis.mybatis.xmlless.enums.JoinPropertyType
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.toolkit.StringPool.DOT


/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
data class FieldMappings(val mappings: List<FieldMapping>,
                         val tableInfo: TableInfo,
                         var modelClass: Class<*>,
                         var defaultSelectedProperties: Array<String>?,
                         var defaultIgnoredProperties: Array<String>?) {

  fun fromDeclaration(): String {
    return tableInfo.tableName + " " + selectJoins()
  }

  fun insertFields(): String {
    return mappings.filter { !it.insertIgnore }.joinToString(",") {
      it.column
    }
  }

  fun insertProperties(prefix: String? = null): String {
    return mappings.filter { !it.insertIgnore }.joinToString(",") {
      it.getPropertyExpression(prefix)
    }
  }

  fun resolveColumnByPropertyName(property: String, isSelectProperty: Boolean = false): BuildSqlResult {
    if (property.contains(".")) {
      val joinedPropertyWords = property.split(".")
      if (joinedPropertyWords.size > 2) {
        throw IllegalStateException("暂不支持多级连接属性：$property")
      } else {
        val objectProperty = joinedPropertyWords[0]
        val joinProperty = joinedPropertyWords[1]
        mappings.firstOrNull {
          it.joinInfo != null && it.joinInfo.joinPropertyType == JoinPropertyType.Object
              && it.property == objectProperty
        }?.let {
          return BuildSqlResult(SelectColumn(it.joinInfo!!.joinTable(), joinProperty.toUnderlineCase().toLowerCase())
              .toSql())
        }
      }
    }
    val column = resolveFromFieldInfo(property)
    if (column == null) {
      resolveFromPropertyJoinInfo(property)?.let {
        return it
      }
    }
    if (column == null) {
      resolveFromObjectJoinInfo(property)?.let {
        return it
      }
    }
    if (column != null) {
      val mapping = mappings.firstOrNull { it.column == column }
          ?: throw IllegalStateException("Cannot resolved property $property of class $modelClass")
      val tableName = if (mapping.joinInfo != null) {
        mapping.joinInfo.joinTable()
      } else {
        tableInfo.tableName
      }
      val realColumn = when (mapping.joinInfo?.joinPropertyType) {
        JoinPropertyType.Object         ->
          mapping.joinInfo.selectColumns.firstOrNull() ?: throw IllegalStateException("Cannot resolved column of " +
              "property $property")
        JoinPropertyType.SingleProperty -> mapping.joinInfo.selectColumns.firstOrNull()
            ?: throw IllegalStateException("Cannot resolved column of " +
                "property $property")
        null                            -> column
      }
      return if (tableName != tableInfo.tableName && isSelectProperty) {
        BuildSqlResult(String.format("%s.%s", tableName, realColumn, column))
      } else {
        BuildSqlResult(String.format("%s.%s", tableName, realColumn))
      }
    }
    return BuildSqlResult(null, listOf("Cannot recognize property $property while building sql"))
  }

  fun selectFields(properties: List<String>, propertyMap: Map<String, TableFieldInfo>, tableName: String): String {
    return (properties.filter { !it.contains(".") }.map {
      propertyMap[it]!!
    }.map {
      SelectColumn(tableName, it.column)
    } + properties.filter { it.contains(".") }.map { property ->
      if (property.contains(".")) {
        val joinedPropertyWords = property.split(".")
        if (joinedPropertyWords.size > 2) {
          throw IllegalStateException("暂不支持多级连接属性：$property")
        } else {
          val objectProperty = joinedPropertyWords[0]
          val joinProperty = joinedPropertyWords[1]
          mappings.firstOrNull {
            it.joinInfo != null && it.joinInfo.joinPropertyType == JoinPropertyType.Object
                && it.property == objectProperty
          }?.let {
            SelectColumn(it.joinInfo!!.joinTable(), joinProperty.toUnderlineCase().toLowerCase())
          }
        }
      } else {
        null
      }
    }).filterNotNull().joinToString(", ") { it.toSql() }
  }

  fun selectFields(): String {
    return mappings.filter { !it.selectIgnore }.map { mapping ->
      if (mapping.joinInfo != null) {
        mapping.joinInfo.selectColumns.joinToString(", ") {
          mapping.joinInfo.joinTable() + '.' + it
        }
      } else {
        tableInfo.tableName + "." + mapping.column
      }
    }.filter { !it.isBlank() }.joinToString(", ")
  }

  fun updateSql(): String {
    return mappings.filter { !it.updateIgnore }.joinToString(",") {
      "${it.column} = ${it.getUpdateExpression()}"
    }
  }

  private fun resolveFromFieldInfo(property: String): String? {
    return if (property == tableInfo.keyProperty) {
      tableInfo.keyColumn
    } else {
      tableInfo.fieldList.firstOrNull { it.property == property }?.column
    }
  }

  private fun resolveFromObjectJoinInfo(property: String): BuildSqlResult? {
    val bestObjectJoinMapping = mappings.filter {
      it.joinInfo != null && it.joinInfo.joinPropertyType == JoinPropertyType.Object
    }.firstOrNull { property.startsWith(it.property) }
    if (bestObjectJoinMapping != null) {
      val maybeJoinPropertyPascal = property.replaceFirst(bestObjectJoinMapping.property, "")
      if (maybeJoinPropertyPascal.isNotBlank() && maybeJoinPropertyPascal.first() in 'A'..'Z') {
        val maybeJoinProperty = maybeJoinPropertyPascal.toCamelCase()
        return BuildSqlResult(bestObjectJoinMapping.joinInfo!!.joinTable() + DOT + bestObjectJoinMapping.joinInfo
            .resolveColumnProperty(maybeJoinProperty))
      }
    }
    return null
  }

  private fun resolveFromPropertyJoinInfo(property: String): BuildSqlResult? {
    val underlineProperty = property.toUnderlineCase().toLowerCase()
    val joinInfo = mappings.mapNotNull { it.joinInfo }.filter {
      it.joinPropertyType == JoinPropertyType.SingleProperty
    }.firstOrNull { it.selectColumns.firstOrNull() == underlineProperty }
    if (joinInfo != null) {
      return BuildSqlResult(joinInfo.joinTable() + DOT + joinInfo.selectColumns.first())
    }
    return null
  }

  private fun selectJoins(): String {
    return mappings.filter { !it.selectIgnore && it.joinInfo != null }
        .distinctBy { it.joinInfo!!.joinTable() }
        .joinToString(" ") { fieldMapping ->
          val col = mappings.firstOrNull() { it.property == fieldMapping.joinInfo?.joinProperty }?.column
              ?: throw IllegalStateException("Cannot resolve join property ${fieldMapping.joinInfo?.joinProperty}")
          """
	${fieldMapping.joinInfo!!.type.name.toUpperCase()} JOIN
		${fieldMapping.joinInfo.joinTableDeclaration()}
	ON
		${fieldMapping.joinInfo.joinTable()}.${fieldMapping
              .joinInfo.targetColumn} = ${tableInfo.tableName}.$col"""
        }
  }

}
