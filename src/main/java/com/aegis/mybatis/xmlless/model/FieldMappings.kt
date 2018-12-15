package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.constant.JOIN
import com.aegis.mybatis.xmlless.enums.JoinPropertyType.Object
import com.aegis.mybatis.xmlless.enums.JoinPropertyType.SingleProperty
import com.aegis.mybatis.xmlless.exception.BuildSQLException
import com.aegis.mybatis.xmlless.kotlin.toCamelCase
import com.aegis.mybatis.xmlless.kotlin.toUnderlineCase
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.toolkit.StringPool.DOT


/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
@Suppress("ArrayInDataClass")
data class FieldMappings(val mappings: List<FieldMapping>,
                         val tableInfo: TableInfo,
                         var modelClass: Class<*>,
                         var defaultSelectedProperties: Array<String>?,
                         var defaultIgnoredProperties: Array<String>?) {

  /**
   * 获取from后的表名称及join信息的语句
   * 例如： t_student LEFT JOIN t_score ON t_score.student_id = t_student.id
   */
  fun fromDeclaration(): String {
    return tableInfo.tableName + " " + selectJoins()
  }

  /**
   * 获取插入的列名称语句, join属性自动被过滤
   * 例如： t_student.id, t_student.name
   */
  fun insertFields(): String {
    return mappings.filter { it.joinInfo == null && !it.insertIgnore }.joinToString(",") {
      it.column
    }
  }

  /**
   * 获取插入的对象属性语句，例如：
   * #{name}, #{age}, #{details,typeHandler=com.aegis.project.mybatis.handler.DetailsHandler}
   */
  fun insertProperties(prefix: String? = null): String {
    return mappings.filter { !it.insertIgnore }.joinToString(",") {
      it.getPropertyExpression(prefix)
    }
  }

  /**
   * 根据属性名称获取数据库表的列名称，返回的列名称包含表名称
   */
  fun resolveColumnByPropertyName(property: String, isSelectProperty: Boolean = false): String {
    // 如果属性中存在.，则该属性表示一个关联对象的属性，例如student.subjectId
    // 目前仅支持一层关联关系
    if (property.contains(".")) {
      val joinedPropertyWords = property.split(".")
      if (joinedPropertyWords.size > 2) {
        throw IllegalStateException("暂不支持多级连接属性：$property")
      } else {
        val objectProperty = joinedPropertyWords[0]
        val joinProperty = joinedPropertyWords[1]
        mappings.firstOrNull {
          it.joinInfo != null && it.joinInfo.joinPropertyType == Object
              && it.property == objectProperty
        }?.let {
          return SelectColumn(it.joinInfo!!.joinTable(), joinProperty.toUnderlineCase().toLowerCase())
              .toSql()
        }
      }
    }
    // 匹配持久化对象的属性查找列名
    val column = resolveFromFieldInfo(property)
    if (column == null) {
      // 从关联属性中匹配
      val joinColumn = resolveFromPropertyJoinInfo(property)
      if (joinColumn != null) {
        return joinColumn
      }
    }
    if (column == null) {
      // 从关联对象中匹配
      val joinColumn = resolveFromObjectJoinInfo(property)
      if (joinColumn != null) {
        return joinColumn
      }
    }
    if (column != null) {
      // 非关联表属性
      val mapping = mappings.firstOrNull { it.column == column }
          ?: throw IllegalStateException("无法解析属性类${modelClass.simpleName}的属性${property}对应的列名称")
      val tableName = when {
        mapping.joinInfo != null -> mapping.joinInfo.joinTable()
        else                     -> tableInfo.tableName
      }
      val realColumn = when (mapping.joinInfo?.joinPropertyType) {
        Object         ->
          mapping.joinInfo.selectColumns.firstOrNull()
              ?: throw BuildSQLException("无法解析属性类${modelClass.simpleName}的属性${property}对应的列名称")
        SingleProperty -> mapping.joinInfo.selectColumns.first()
        null           -> column
      }
      return when {
        tableName != tableInfo.tableName && isSelectProperty -> String.format("%s.%s", tableName, realColumn, column)
        else                                                 -> String.format("%s.%s", tableName, realColumn)
      }
    }
    throw BuildSQLException("无法解析属性类${modelClass.simpleName}的属性${property}对应的列名称")
  }

  /**
   * 获取select查询的列名称语句
   */
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

  /**
   * 从表信息中解析对象属性名称对应的数据库表的列名称
   */
  private fun resolveFromFieldInfo(property: String): String? {
    return when (property) {
      tableInfo.keyProperty -> tableInfo.keyColumn
      else                  -> tableInfo.fieldList.firstOrNull { it.property == property }?.column
    }
  }

  private fun resolveFromObjectJoinInfo(property: String): String? {
    // 如果持久化对象中找不到这个属性，在关联的对象中进行匹配，匹配的规则是，关联对象的字段名称+属性名称
    // 例如，当前的model是Student，有一个名为scores的属性类型为List<Score>,表示该学生的成绩列表,
    // Score对象中有一个属性subjectId，那么scoresSubjectId可以匹配到关联对象的subjectId属性上
    // 这个适用于主对象和关联对象中存在同名属性，而需要查询关联对象属性或以关联对象属性作为条件判断的情况
    val bestObjectJoinMapping = mappings.filter {
      it.joinInfo != null && it.joinInfo.joinPropertyType == Object
    }.firstOrNull { property.startsWith(it.property) }
    if (bestObjectJoinMapping != null) {
      val maybeJoinPropertyPascal = property.replaceFirst(bestObjectJoinMapping.property, "")
      if (maybeJoinPropertyPascal.isNotBlank() && maybeJoinPropertyPascal.first() in 'A'..'Z') {
        val maybeJoinProperty = maybeJoinPropertyPascal.toCamelCase()
        return bestObjectJoinMapping.joinInfo!!.joinTable() + DOT + bestObjectJoinMapping.joinInfo
            .resolveColumnProperty(maybeJoinProperty)
      }
    }
    // 如果持久化对象中找不到这个属性，在关联的对象中匹配同名属性，但是该属性名称必须唯一，即不能在多个关联对象中都存在该名称的属性
    // 例如，当前的model是Student，有一个名为scores的属性类型为List<Score>,表示该学生的成绩列表,
    // Score对象中有一个属性subjectId，那么subjectId可以匹配到关联对象的subjectId属性上
    val matchedJoinInfos = mappings.filter {
      it.joinInfo != null && it.joinInfo.joinPropertyType == Object
    }.filter {
      it.joinInfo?.getJoinTableInfo()?.fieldList?.firstOrNull { it.property == property } != null
    }.map { it.joinInfo!! }
    return when {
      matchedJoinInfos.size > 1  -> throw BuildSQLException("在${matchedJoinInfos
          .map { it.realType()?.simpleName }.joinToString(",")}发现了相同的属性$property, " +
          "无法确定属性对应的表及字段")
      matchedJoinInfos.size == 1 -> matchedJoinInfos.first().joinTable() + DOT +
          matchedJoinInfos.first().resolveColumnProperty(property)
      else                       -> null
    }
  }

  /**
   * 从关联属性中解析对应的关联表的列名称
   */
  private fun resolveFromPropertyJoinInfo(property: String): String? {
    val underlineProperty = property.toUnderlineCase().toLowerCase()
    val joinInfo = mappings.mapNotNull { it.joinInfo }.filter {
      it.joinPropertyType == SingleProperty
    }.firstOrNull { it.selectColumns.firstOrNull() == underlineProperty }
    if (joinInfo != null) {
      return joinInfo.joinTable() + DOT + joinInfo.selectColumns.first()
    }
    return null
  }

  /**
   * select查询中的join语句
   */
  private fun selectJoins(): String {
    return mappings.filter { !it.selectIgnore && it.joinInfo != null }
        .mapNotNull { it.joinInfo }
        .distinctBy { it.joinTable() }
        .joinToString(" ") { joinInfo ->
          val col = mappings.firstOrNull { it.property == joinInfo.joinProperty }?.column
              ?: throw IllegalStateException("Cannot resolve join property ${joinInfo.joinProperty}")
          String.format(
              JOIN, joinInfo.type.name,
              joinInfo.joinTableDeclaration(),
              joinInfo.joinTable(),
              joinInfo.targetColumn,
              tableInfo.tableName, col
          )
        }
  }

}
