package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.annotations.ResolvedName
import com.aegis.mybatis.xmlless.constant.*
import com.aegis.mybatis.xmlless.enums.Operations
import com.aegis.mybatis.xmlless.exception.BuildSQLException
import com.aegis.mybatis.xmlless.resolver.ColumnsResolver
import com.baomidou.mybatisplus.annotation.FieldFill
import com.baomidou.mybatisplus.annotation.FieldStrategy
import com.baomidou.mybatisplus.core.toolkit.StringPool
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils
import org.springframework.data.domain.Sort
import kotlin.reflect.KFunction

/**
 * 当结尾以指定字符串结束时，返回去掉结尾指定字符串的新字符串，否则返回当前字符串
 * @param end 指定结尾的字符串
 * @return
 */
private fun String.trim(end: String): String {
  return if (this.endsWith(end)) {
    this.substring(0, this.length - end.length)
  } else {
    this
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
    private val properties: List<String> = listOf(),
    /**  查询条件信息 */
    val conditions: List<Condition> = listOf(),
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

  private fun buildDeleteSql(): String {
    return String.format(DELETE, tableName(), resolveWhere())
  }

  private fun buildExistsSql(): String {
    return String.format(SELECT_COUNT, tableName(), resolveWhere())
  }

  private fun buildInsertSql(): String {
    val columnsString = mappings.insertFields()
    return when {
      function.name.endsWith("All")                          -> {
        val valueString = mappings.insertProperties("item.")
        String.format(BATCH_INSERT, mappings.tableInfo.tableName, columnsString, valueString)
      }
      this.properties.isEmpty() && this.conditions.isEmpty() -> {
        val valueString = mappings.insertProperties()
        String.format(INSERT, mappings.tableInfo.tableName, columnsString, valueString)
      }
      else                                                   -> throw BuildSQLException("无法解析${this.function}")
    }
  }

  /**
   * 构建select查询语句
   */
  private fun buildSelectSql(): String {
    // 构建select的列
    val buildColsResult = ColumnsResolver.resolve(mappings, properties)
    val whereSqlResult = resolveWhere()
    val order = resolveOrder()
    val limit = resolveLimit()
    val limitInSubQuery = limitInSubQuery()
    val from = resolveFrom(limitInSubQuery, whereSqlResult, limit)
    return if (limitInSubQuery) {
      String.format(SELECT, buildColsResult, from,
          resolvedNameAnnotation?.joinAppend ?: "",
          "", order, ""
      )
    } else {
      String.format(SELECT, buildColsResult, from,
          resolvedNameAnnotation?.joinAppend ?: "",
          whereSqlResult, order, limit
      )
    }
  }

  private fun buildUpdateSql(): String {
    return String.format(UPDATE, tableName(), resolveUpdateProperties(),
        resolveUpdateWhere())
  }

  private fun convertIf(sqlScript: String, property: String, mapping: FieldMapping): String {
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
  private fun getSqlSet(prefix: String?, mapping: FieldMapping): String {
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

  private fun hasCollectionJoinProperty(): Boolean {
    return when {
      this.properties.isNotEmpty() -> this.mappings.mappings.filter { it.property in this.properties }
      else                         -> this.mappings.mappings
    }.filter {
      it.joinInfo != null
    }.any {
      Collection::class.java.isAssignableFrom(it.tableFieldInfo.propertyType)
    }
  }

  private fun includeJoins(): Boolean {
    return properties.isEmpty() || properties.any { !it.startsWith(mappings.tableInfo.tableName) }
  }

  private fun limitInSubQuery(): Boolean {
    return hasCollectionJoinProperty() && limitation != null
  }

  private fun resolveFrom(limitInSubQuery: Boolean, whereSqlResult: String, limit: String): String {
    val defaultFrom = mappings.fromDeclaration()
    return when {
      limitInSubQuery -> String.format(SUB_QUERY, tableName(), whereSqlResult, limit, defaultFrom)
      includeJoins()  -> defaultFrom
      else            -> tableName()
    }
  }

  /**
   * 对查询条件进行分组，类似findByNameOrDescriptionLikeKeywords的表达式，
   * 由于name未指明条件类型，自动将name和description归并为一组，并且设置条件类型和description一样为like
   * 同组条件如果超过一个在构建查询语句时添加括号
   */
  private fun resolveGroups(): List<List<Condition>> {
    val result = arrayListOf<ArrayList<Condition>>()
    var tmp = arrayListOf<Condition>()
    conditions.forEachIndexed { _, condition ->
      when {
        tmp.isEmpty() -> tmp.add(condition)
        tmp.map { it.operator }.toSet().let {
          it.size == 1 && it.first() == Operations.EqDefault && condition.operator == Operations.EqDefault
        }             -> tmp.add(condition)
        tmp.map { it.operator }.toSet().let {
          it.size == 1 && it.first() == Operations.EqDefault
        }             -> {
          tmp.forEach {
            it.apply {
              operator = condition.operator
              paramName = condition.paramName
              parameter = condition.parameter
            }
          }
          tmp.add(condition)
          result.add(tmp)
          tmp = arrayListOf()
        }
        else          -> {
          result.add(tmp)
          tmp = arrayListOf(condition)
        }
      }
    }
    if (tmp.isNotEmpty()) {
      result.add(tmp)
    }
    return result.filter { it.isNotEmpty() }
  }

  private fun resolveLimit(): String {
    if (limitation != null) {
      return String.format(LIMIT, limitation?.offsetParam, limitation?.sizeParam)
    }
    return ""
  }

  private fun resolveOrder(): String {
    when {
      this.sorts.isNotEmpty() -> return """
      <trim suffixOverrides="ORDER BY">
      ORDER BY
      <trim suffixOverrides=",">
      <trim>
      ${this.sorts.joinToString(", ") {
        """${it.property} ${it.direction}"""
      } + "," + extraSortScript}
    </trim>
</trim>
</trim>
"""
      extraSortScript != null -> return """
      <trim suffixOverrides="ORDER BY">
      ORDER BY
      $extraSortScript
</trim>
"""
      else                    -> return ""
    }
  }

  private fun resolveUpdateProperties(): String {
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

  private fun resolveUpdateWhere(): String {
    return String.format("""
        WHERE %s = #{%s}
      """, mappings.tableInfo.keyColumn, mappings.tableInfo.keyProperty)
  }

  private fun resolveWhere(): String {
    val groups = resolveGroups()
    val groupBuilders = groups.map {
      toGroupSqlBuild(it)
    }
    return when {
      conditions.isNotEmpty() -> String.format(WHERE, trimCondition(groupBuilders.joinToString("\n")
          + "\n" + (resolvedNameAnnotation?.whereAppend ?: "")))
      else                    -> ""
    }
  }

  private fun tableName(): String {
    return mappings.tableInfo.tableName
  }

  private fun toGroupSqlBuild(it: List<Condition>): String {
    val list = it.map { it.toSql(mappings) }
    return if (it.size > 1) {
      it.first().wrapWithTests(
          "(" + trimCondition(it.joinToString(" ") { it.toSqlWithoutTest(mappings) }) + ")"
      )
    } else {
      list.first()
    }
  }

  private fun trimCondition(sql: String): String {
    return sql.trim().trim(" AND").trim(" OR")
  }

  private fun wrapSetScript(sql: String): String {
    return SqlScriptUtils.convertTrim(sql, "SET", null, null, ",")
  }

}
