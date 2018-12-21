package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.annotations.ResolvedName
import com.aegis.mybatis.xmlless.constant.*
import com.aegis.mybatis.xmlless.constant.Strings.EMPTY
import com.aegis.mybatis.xmlless.constant.Strings.LINE_BREAK
import com.aegis.mybatis.xmlless.constant.Strings.SCRIPT_END
import com.aegis.mybatis.xmlless.constant.Strings.SCRIPT_START
import com.aegis.mybatis.xmlless.enums.Operations
import com.aegis.mybatis.xmlless.exception.BuildSQLException
import com.aegis.mybatis.xmlless.resolver.ColumnsResolver
import com.baomidou.mybatisplus.annotation.FieldFill
import com.baomidou.mybatisplus.annotation.FieldStrategy
import com.baomidou.mybatisplus.core.toolkit.StringPool
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils.convertTrim
import org.springframework.data.domain.Sort
import kotlin.reflect.KFunction

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
    val properties: List<String> = listOf(),
    /**  查询条件信息 */
    val conditions: List<QueryCriteria> = listOf(),
    /**  排序信息 */
    val sorts: List<Sort.Order> = listOf(),
    /** 待解析的方法 */
    val function: KFunction<*>,
    /**  数据对象的与数据库的映射管理 */
    val mappings: FieldMappings,
    /**  limit信息 */
    var limitation: Limitation? = null,
    val resolvedNameAnnotation: ResolvedName?
) {

  /**  方法指定忽略的字段 */
//  val ignoredProperties: List<String>? = function.findAnnotation<IgnoredProperties>()?.properties?.toList()

  var extraSortScript: String? = null

  fun buildCountSql(): String {
    return String.format(SELECT_COUNT, tableName(), resolveWhere())
  }

  fun buildDeleteSql(): String {
    return String.format(DELETE, tableName(), resolveWhere())
  }

  fun buildExistsSql(): String {
    return String.format(SELECT_COUNT, tableName(), resolveWhere())
  }

  fun buildInsertSql(): String {
    val columns = mappings.insertFields(this.properties)
    val template: String
    val values = when {
      function.name.endsWith("All")    -> {
        template = BATCH_INSERT
        mappings.insertProperties("item.")
      }
      this.properties.isNotEmpty()     -> {
        template = INSERT
        mappings.insertProperties(insertProperties = this.properties)
      }
      this.properties.isEmpty()
          && this.conditions.isEmpty() -> {
        template = INSERT
        mappings.insertProperties()
      }
      else                             -> throw BuildSQLException("无法解析${this.function}")
    }
    if (columns.size != values.size) {
      throw BuildSQLException("插入的字段\n$columns\n与插入的值\n$values\n数量不一致")
    }
    return String.format(template, mappings.tableInfo.tableName,
        columns.joinToString(Strings.COLUMN_SEPERATOR),
        values.joinToString(Strings.COLUMN_SEPERATOR))
  }

  /**
   * 构建select查询语句
   */
  fun buildSelectSql(): String {
    // 构建select的列
    val buildColsResult = ColumnsResolver.resolve(mappings, properties)
    val groupBy = resolveGroupBy(mappings)
    val whereSqlResult = resolveWhere()
    val order = resolveOrder()
    val limit = resolveLimit()
    val limitInSubQuery = limitInSubQuery()
    val from = resolveFrom(limitInSubQuery, whereSqlResult, limit)
    return when {
      limitInSubQuery -> buildScript(SELECT, buildColsResult.joinToString(",\n\t") { it.toSql() }, from,
          resolvedNameAnnotation?.joinAppend ?: "",
          "", groupBy, order, "")
      else            -> buildScript(SELECT, buildColsResult.joinToString(",\n\t") { it.toSql() }, from,
          resolvedNameAnnotation?.joinAppend ?: "",
          whereSqlResult, groupBy, order, limit
      )
    }
  }

  fun buildUpdateSql(): String {
    return String.format(UPDATE, tableName(), resolveUpdateProperties(),
        resolveUpdateWhere())
  }

  fun containedTables(): List<String> {
    return (this.conditions.map {
      it.toSqlWithoutTest(mappings).split(".")[0]
    } + ColumnsResolver.resolveIncludedTables(mappings, properties)).distinct()
  }

  fun convertIf(sqlScript: String, property: String, mapping: FieldMapping): String {
    if (mapping.tableFieldInfo.fieldStrategy == FieldStrategy.IGNORED) {
      return sqlScript
    }
    return when {
      mapping.tableFieldInfo.fieldStrategy == FieldStrategy.NOT_EMPTY
          && mapping.tableFieldInfo.isCharSequence ->
        SqlScriptUtils.convertIf(sqlScript,
            String.format("%s != null and %s != ''", property, property), false)
      else                                         -> SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", property), false)
    }
  }

  /**
   * 获取 set sql 片段
   *
   * @param prefix 前缀
   * @return sql 脚本片段
   */
  fun getSqlSet(prefix: String?, mapping: FieldMapping): String {
    val newPrefix = prefix ?: StringPool.EMPTY
    // 默认: column=
    var sqlSet = mapping.tableFieldInfo.column + StringPool.EQUALS

    sqlSet += when {
      mapping.tableFieldInfo.update != null
          && mapping.tableFieldInfo.update.isNotEmpty() ->
        String.format(mapping.tableFieldInfo.update, mapping.tableFieldInfo.column)
      else                                              -> SqlScriptUtils.safeParam(newPrefix + mapping.getPropertyExpression(null, false))
    }
    sqlSet += StringPool.COMMA
    return when {
      mapping.tableFieldInfo.fieldFill == FieldFill.UPDATE
          || mapping.tableFieldInfo.fieldFill == FieldFill.INSERT_UPDATE -> sqlSet
      else                                                               -> convertIf(sqlSet, newPrefix + mapping.tableFieldInfo.property, mapping)
    }
  }

  fun hasCollectionJoinProperty(): Boolean {
    return when {
      this.properties.isNotEmpty() -> this.mappings.mappings.filter { it.property in this.properties }
      else                         -> this.mappings.mappings
    }.filter {
      it.joinInfo != null
    }.any {
      Collection::class.java.isAssignableFrom(it.tableFieldInfo.propertyType)
    }
  }

  fun includeJoins(): Boolean {
    return properties.isEmpty() || properties.any { !it.startsWith(mappings.tableInfo.tableName) }
        || containedTables().distinct().any { it != mappings.tableInfo.tableName }
  }

  fun limitInSubQuery(): Boolean {
    return hasCollectionJoinProperty() && limitation != null
  }

  fun resolveFrom(limitInSubQuery: Boolean, whereSqlResult: String, limit: String): String {
    val defaultFrom = mappings.fromDeclaration(properties, containedTables())
    return when {
      limitInSubQuery -> String.format(SUB_QUERY, tableName(), whereSqlResult, limit, defaultFrom)
      includeJoins()  -> defaultFrom
      else            -> tableName()
    }
  }

  fun resolveGroupBy(mappings: FieldMappings): String {
    val mappingList = when {
      properties.isNotEmpty() -> mappings.mappings.filter { it.property in properties }
      else                    -> mappings.mappings
    }
    val groupProperties = mappingList.mapNotNull { it.joinInfo }
        .filter { it is PropertyJoinInfo }
        .mapNotNull { (it as PropertyJoinInfo).groupBy }
    return when {
      groupProperties.isNotEmpty() -> "GROUP BY\n\t${groupProperties.joinToString(", ")}"
      else                         -> ""
    }
  }

  /**
   * 对查询条件进行分组，类似findByNameOrDescriptionLikeKeywords的表达式，
   * 由于name未指明条件类型，自动将name和description归并为一组，并且设置条件类型和description一样为like
   * 同组条件如果超过一个在构建查询语句时添加括号
   */
  fun resolveGroups(): List<QueryCriteriaGroup> {
    val result = arrayListOf<QueryCriteriaGroup>()
    var tmp = QueryCriteriaGroup()
    conditions.forEachIndexed { _, condition ->
      when {
        tmp.isEmpty() -> tmp.add(condition)
        tmp.conditions.map { it.operator }.toSet().let {
          it.size == 1 && it.first() == Operations.EqDefault && condition.operator == Operations.EqDefault
        }             -> tmp.add(condition)
        tmp.conditions.map { it.operator }.toSet().let {
          it.size == 1 && it.first() == Operations.EqDefault
        }             -> {
          result.add(QueryCriteriaGroup(
              (tmp.conditions.map {
                QueryCriteria(it.property, condition.operator, it.append, condition.paramName, condition.parameter, it
                    .specificValue)
              }.toMutableList() + condition).toMutableList()
          ))
          tmp = QueryCriteriaGroup()
        }
        else          -> {
          result.add(tmp)
          tmp = QueryCriteriaGroup(mutableListOf(condition))
        }
      }
    }
    if (tmp.isNotEmpty()) {
      result.add(tmp)
    }
    return result.filter { it.isNotEmpty() }
  }

  fun resolveLimit(): String {
    if (limitation != null) {
      return String.format(LIMIT, limitation?.offsetParam, limitation?.sizeParam)
    }
    return ""
  }

  fun resolveOrder(): String {
    return when {
      this.sorts.isNotEmpty() -> {
        val string = this.sorts.joinToString(", ") {
          "${mappings.resolveColumnByPropertyName(it.property, false).first().toSql()} ${it.direction}"
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

  fun resolveUpdateProperties(): String {
    if (this.properties.isEmpty()) {
      return wrapSetScript(mappings.mappings.filter {
        it.joinInfo == null && !it.updateIgnore
            && it.property != mappings.tableInfo.keyProperty
      }.joinToString(StringPool.NEWLINE) {
        getSqlSet(null, it)
      })
    }
    val builders = this.properties.map { property ->
      val mapping = this.mappings.mappings.firstOrNull { it.property == property }
      if (mapping == null) {
        throw BuildSQLException("无法解析待更新属性：$property, 方法：$function")
      } else {
        getSqlSet(null, mapping)
      }
    }
    return wrapSetScript(builders.joinToString(StringPool.NEWLINE))
  }

  fun resolveUpdateWhere(): String {
    return String.format("""
        WHERE %s = #{%s}
      """, mappings.tableInfo.keyColumn, mappings.tableInfo.keyProperty)
  }

  fun resolveWhere(): String {
    val groups = resolveGroups()
    val groupBuilders = groups.map {
      it.toSql(mappings)
    }
    val whereAppend = resolvedNameAnnotation?.whereAppend
    return when {
      conditions.isNotEmpty() || (whereAppend != null && whereAppend.isNotBlank()) ->
        String.format(WHERE, trimCondition(groupBuilders.joinToString(LINE_BREAK) +
            LINE_BREAK + (whereAppend ?: EMPTY)))
      else                                                                         -> ""
    }
  }

  fun toSql(): String {
    return when (type) {
      QueryType.Delete -> {
        buildDeleteSql()
      }
      QueryType.Insert -> {
        buildInsertSql()
      }
      QueryType.Select -> {
        buildSelectSql()
      }
      QueryType.Update -> {
        buildUpdateSql()
      }
      QueryType.Count  -> buildCountSql()
      QueryType.Exists -> buildExistsSql()
    }
  }

  private fun buildScript(template: String, vararg args: String): String {
    val formattedSql = String.format(template, *args).trim()
    return when {
      formattedSql.startsWith(SCRIPT_START) -> formattedSql
      else                                  -> SCRIPT_START + LINE_BREAK + formattedSql.trim() + LINE_BREAK + SCRIPT_END
    }
  }

  private fun tableName(): String {
    return mappings.tableInfo.tableName
  }

  private fun trimCondition(sql: String): String {
    return sql.trim().trim(" AND").trim(" OR")
  }

  private fun wrapSetScript(sql: String): String {
    return convertTrim(sql, SQLKeywords.SET, null, null, ",")
  }

}
