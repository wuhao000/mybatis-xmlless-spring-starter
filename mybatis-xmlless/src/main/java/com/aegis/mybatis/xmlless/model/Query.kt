package com.aegis.mybatis.xmlless.model

import com.aegis.kotlin.endsWithAny
import com.aegis.mybatis.xmlless.constant.*
import com.aegis.mybatis.xmlless.constant.Strings.SCRIPT_START
import com.aegis.mybatis.xmlless.exception.BuildSQLException
import com.aegis.mybatis.xmlless.model.component.ISqlPart
import com.aegis.mybatis.xmlless.model.component.SubQueryDeclaration
import com.aegis.mybatis.xmlless.model.component.WhereDeclaration
import com.aegis.mybatis.xmlless.resolver.ColumnsResolver
import com.baomidou.mybatisplus.annotation.FieldFill
import com.baomidou.mybatisplus.annotation.FieldStrategy
import com.baomidou.mybatisplus.core.toolkit.StringPool
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils.convertTrim
import org.springframework.data.domain.Sort

/**
 * 当结尾以指定字符串结束时，返回去掉结尾指定字符串的新字符串，否则返回当前字符串
 * @param end 指定结尾的字符串
 * @return
 */
fun String.trim(end: String): String {
  return when {
    this.endsWith(end) -> this.substring(0, this.length - end.length)
    else               -> this
  }
}

/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
data class Query(
    /**  sql类型 */
    val type: QueryType,
    /**  更细或者查询的属性列表 */
    val properties: Properties = Properties(),
    /**  查询条件信息 */
    val criterion: List<List<QueryCriteria>> = listOf(),
    /**  排序信息 */
    val sorts: List<Sort.Order> = listOf(),
    /** 待解析的方法 */
    val methodInfo: MethodInfo,
    /**  limit信息 */
    var limitation: Limitation? = null
) {

  /**  数据对象的与数据库的映射管理 */
  val mappings: FieldMappings = methodInfo.mappings

  /**  来自Page参数的排序条件 */
  var extraSortScript: String? = null

  fun buildCountSql(): String {
    // 必须先解析查询条件才能确定条件中用于判断的字段来自哪张表
    val where = resolveWhere()
    val from = resolveFrom(false, where, "", true)
    return String.format(SELECT_COUNT, from.toSql(),
        where.toSql(), methodInfo.scriptAppend)
  }

  private fun buildUpdateSql(): String {
    return String.format(
        UPDATE, tableName().name, resolveUpdateProperties(false),
        resolveUpdateWhere(), methodInfo.scriptAppend
    )
  }

  fun containedTables(): List<String> {
    return (this.criterion.flatten().map {
      it.toSqlWithoutTest(null).split(".")[0]
    } + ColumnsResolver.resolveIncludedTables(properties, methodInfo)).distinct()
  }

  private fun convertIf(sqlScript: String, property: String, mapping: FieldMapping): String {
    if (mapping.tableFieldInfo.whereStrategy == FieldStrategy.IGNORED) {
      return sqlScript
    }
    return when {
      mapping.tableFieldInfo.whereStrategy == FieldStrategy.NOT_EMPTY
          && mapping.tableFieldInfo.isCharSequence -> SqlScriptUtils.convertIf(
          sqlScript, String.format("%s != null and %s != ''", property, property), false
      )

      else                                         -> SqlScriptUtils.convertIf(
          sqlScript, String.format("%s != null", property), false
      )
    }
  }

  /**
   * 获取 set sql 片段
   *
   * @return sql 脚本片段
   * @author 吴昊
   */
  private fun getSqlSet(mapping: FieldMapping, insertUpdate: Boolean): String {
    val column = mapping.tableFieldInfo.column
    // 默认: column=
    var sqlSet = ColumnsResolver.wrapColumn(column) + StringPool.EQUALS
    sqlSet += if (insertUpdate) {
      "(if(VALUES($column) = null, $column, VALUES($column)))"
    } else {
      when {
        !mapping.tableFieldInfo.update.isNullOrBlank() -> String.format(mapping.tableFieldInfo.update, column)
        else                                           -> SqlScriptUtils.safeParam(
            mapping.getPropertyExpression(
                null, false
            )
        )
      }
    }
    sqlSet += StringPool.COMMA
    if (insertUpdate) {
      return sqlSet
    }
    return when (mapping.tableFieldInfo.fieldFill) {
      FieldFill.UPDATE, FieldFill.INSERT_UPDATE -> sqlSet

      else                                      -> convertIf(
          sqlSet, mapping.tableFieldInfo.property, mapping
      )
    }
  }

  fun includeJoins(): Boolean {
    return properties.includes.isEmpty() || properties.includes.any { !it.startsWith(mappings.tableName) } || containedTables().distinct()
        .any { it != mappings.tableName }
  }

  private fun limitInSubQuery(): Boolean {
    return hasCollectionJoinProperty() && limitation != null
  }

  /**
   * 对查询条件进行分组，类似findByNameOrDescriptionLikeKeywords的表达式，
   * 由于name未指明条件类型，自动将name和description归并为一组，并且设置条件类型和description一样为like
   * 同组条件如果超过一个在构建查询语句时添加括号
   */
  fun resolveGroups(): List<QueryCriteriaGroup> {
    return criterion.map { QueryCriteriaGroup(it) }.filter { it.isNotEmpty() }
  }

  private fun resolveLimit(): String {
    if (limitation != null) {
      return String.format(LIMIT, limitation?.offsetParam, limitation?.sizeParam)
    }
    return ""
  }

  private fun resolveOrder(): String {
    return when {
      this.sorts.isNotEmpty() -> {
        val string = this.sorts.joinToString(", ") {
          "${
            ColumnsResolver.resolveColumnByPropertyName(it.property, false, methodInfo).first().toSql()
          } " + "${it.direction}"
        }
        when {
          extraSortScript.isNullOrBlank() -> String.format(ORDER_BY, string)
          else                            -> String.format(ORDER_BY, "$string, $extraSortScript")
        }
      }

      extraSortScript != null -> String.format(ORDER_BY, extraSortScript)
      else                    -> ""
    }
  }

  private fun resolveUpdateWhere(): String {
    return when {
      criterion.isEmpty() -> String.format(
          """
        WHERE %s = #{%s}
      """, mappings.tableInfo.keyColumn, mappings.tableInfo.keyProperty
      )

      else                -> resolveWhere().toSql()
    }
  }

  fun toSql(): String {
    return when (type) {
      QueryType.Delete      -> buildDeleteSql()
      QueryType.Insert      -> buildInsertSql()
      QueryType.Select      -> buildSelectSql()
      QueryType.Update      -> buildUpdateSql()
      QueryType.Count       -> buildCountSql()
      QueryType.Exists      -> buildExistsSql()
      QueryType.LogicDelete -> buildLogicDeleteSql()
    }
  }

  private fun buildDeleteSql(): String {
    return String.format(DELETE, tableName().name,
        resolveWhere().toSql(), methodInfo.scriptAppend)
  }

  private fun buildExistsSql(): String {
    return buildCountSql()
  }

  private fun buildInsertSql(): String {
    val columns = mappings.insertFields(this.properties)
    val template: String
    val values = when {
      methodInfo.name.endsWithAny("All", "Batch") -> {
        template = if (methodInfo.name.endsWithAny("OrUpdateAll", "OrUpdateBatch")) {
          BATCH_INSERT_OR_UPDATE
        } else {
          BATCH_INSERT
        }
        mappings.insertProperties("item.", this.properties)
      }

      this.properties.isNotEmpty()                -> {
        template = if (methodInfo.name.endsWith("OrUpdate")) {
          INSERT_OR_UPDATE
        } else {
          INSERT
        }
        mappings.insertProperties(insertProperties = this.properties)
      }

      this.properties.isIncludeEmpty()
          && this.criterion.isEmpty()             -> {
        template = if (methodInfo.name.endsWith("OrUpdate")) {
          INSERT_OR_UPDATE
        } else {
          INSERT
        }
        mappings.insertProperties()
      }

      else                                        -> throw BuildSQLException(
          "无法解析${
            this.methodInfo
                .method
          }"
      )
    }
    if (columns.size != values.size) {
      throw BuildSQLException("解析方法[${methodInfo.method}]失败，插入的字段\n$columns\n与插入的值\n$values\n数量不一致")
    }
    return String.format(
        template,
        mappings.tableName,
        columns.joinToString(Strings.COLUMN_SEPARATOR),
        values.joinToString(Strings.COLUMN_SEPARATOR),
        resolveUpdateProperties(true)
    )
  }

  private fun buildLogicDeleteSql(): String {
    val mapper = this.mappings.mappings.find { it.tableFieldInfo.isLogicDelete }
      ?: throw IllegalStateException("缺少逻辑删除字段，请在字段上添加@TableLogic注解")
    val logicType = methodInfo.getLogicType()!!
    val value = this.mappings.getLogicDelFlagValue(logicType)
    return "<script>UPDATE ${tableName().name} SET ${mapper.column} = $value " +
        resolveWhere().toSql() + " ${methodInfo.scriptAppend ?: ""}</script>"
  }

  private fun buildScript(vararg args: String): String {
    val formattedSql = String.format(SELECT, *args).trim()
    return when {
      formattedSql.startsWith(SCRIPT_START) -> formattedSql
      else                                  -> SCRIPT_TEMPLATE.format(formattedSql.trim(), methodInfo.scriptAppend)
    }
  }

  /**
   * 构建select查询语句
   */
  private fun buildSelectSql(): String {
    // 构建select的列
    val groupBy = resolveGroupBy(mappings)
    val where = resolveWhere()
    val order = resolveOrder()
    val limit = resolveLimit()
    val limitInSubQuery = limitInSubQuery() && where.whereAppend.isNullOrBlank() && where.criterion.isEmpty()
    val from = resolveFrom(limitInSubQuery, where, limit)
    val buildColsResult = ColumnsResolver.resolve(properties, methodInfo)
    return when {
      limitInSubQuery -> buildScript(
          buildColsResult.joinToString(",\n\t") { it.toSql() },
          from.toSql(),
          methodInfo.joinAppend,
          "",
          groupBy,
          order,
          ""
      )

      else            -> buildScript(
          buildColsResult.joinToString(",\n\t") { it.toSql() },
          from.toSql(),
          methodInfo.joinAppend,
          where.toSql(),
          groupBy,
          order,
          limit
      )
    }
  }

  private fun hasCollectionJoinProperty(): Boolean {
    return when {
      this.properties.isIncludeNotEmpty() -> this.mappings.mappings.filter { it.property in this.properties }

      else                                -> this.mappings.mappings.filter {
        it.property !in this.properties.excludes
      }
    }.filter {
      it.joinInfo != null
    }.any {
      Collection::class.java.isAssignableFrom(it.tableFieldInfo.propertyType)
    }
  }

  /**
   * @param isCount 移除没有被条件引用的join表
   */
  private fun resolveFrom(
      limitInSubQuery: Boolean, where: WhereDeclaration, limit: String, isCount: Boolean = false
  ): ISqlPart {
    val onlyIncludesTables = if (isCount) {
      criterion.flatten().map {
        it.getColumns().mapNotNull { column -> column.table }
      }.flatten().distinct()
    } else {
      null
    }
    val defaultFrom = mappings.fromDeclaration(properties, containedTables(), onlyIncludesTables, limitInSubQuery)

    return when {
      limitInSubQuery -> SubQueryDeclaration(tableName(), where, limit, defaultFrom)
      includeJoins()  -> defaultFrom
      else            -> tableName()
    }
  }

  private fun resolveGroupBy(mappings: FieldMappings): String {
    val mappingList = when {
      properties.isIncludeNotEmpty() -> mappings.mappings.filter { it.property in properties }

      else                           -> mappings.mappings.filter { it.property !in properties.excludes }
    }
    val groupProperties = mappingList.mapNotNull { it.joinInfo }.filterIsInstance<PropertyJoinInfo>()
        .mapNotNull { it.groupBy }
    return when {
      groupProperties.isNotEmpty() -> "GROUP BY\n\t${groupProperties.joinToString(", ")}"
      else                         -> ""
    }
  }

  private fun resolveUpdateProperties(isInsertUpdate: Boolean): String {
    if (this.properties.isIncludeEmpty()) {
      return wrapSetScript(mappings.mappings.filter {
        it.joinInfo == null && !it.updateIgnore && it.property !in this.properties.excludes && it.property !in this.properties.updateExcludeProperties && it.property != mappings.tableInfo.keyProperty
      }.joinToString(StringPool.NEWLINE) {
        getSqlSet(it, isInsertUpdate)
      }, isInsertUpdate)
    }
    val builders = this.properties.includes.map { property ->
      val mapping = this.mappings.mappings.firstOrNull { it.property == property }
      if (mapping == null) {
        throw BuildSQLException("无法解析待更新属性：$property, 方法：${methodInfo.method}")
      } else {
        getSqlSet(mapping, isInsertUpdate)
      }
    }
    return wrapSetScript(builders.joinToString(StringPool.NEWLINE), isInsertUpdate)
  }

  private fun resolveWhere(): WhereDeclaration {
    val groups = resolveGroups()
    val groupBuilders = groups.map {
      it.toSql(true)
    }
    val whereAppend = methodInfo.whereAppend
    return WhereDeclaration(criterion, groupBuilders, whereAppend)
  }

  private fun tableName(): TableName {
    return TableName(mappings.tableName, mappings.tableName.replace('.', '_'))
  }

  private fun wrapSetScript(sql: String, noSetPrefix: Boolean): String {
    val prefix = when {
      noSetPrefix -> ""
      else        -> "SET"
    }
    return convertTrim(sql, prefix, null, null, ",")
  }

}
