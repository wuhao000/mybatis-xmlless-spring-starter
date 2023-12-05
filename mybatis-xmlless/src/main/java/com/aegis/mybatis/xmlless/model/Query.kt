package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.annotations.DeleteValue
import com.aegis.mybatis.xmlless.annotations.Logic
import com.aegis.mybatis.xmlless.annotations.ResolvedName
import com.aegis.mybatis.xmlless.constant.*
import com.aegis.mybatis.xmlless.constant.Strings.EMPTY
import com.aegis.mybatis.xmlless.constant.Strings.LINE_BREAK
import com.aegis.mybatis.xmlless.constant.Strings.SCRIPT_END
import com.aegis.mybatis.xmlless.constant.Strings.SCRIPT_START
import com.aegis.mybatis.xmlless.enums.Operations
import com.aegis.mybatis.xmlless.exception.BuildSQLException
import com.aegis.mybatis.xmlless.model.component.FromDeclaration
import com.aegis.mybatis.xmlless.model.component.ISqlPart
import com.aegis.mybatis.xmlless.model.component.SubQueryDeclaration
import com.aegis.mybatis.xmlless.resolver.ColumnsResolver
import com.baomidou.mybatisplus.annotation.FieldFill
import com.baomidou.mybatisplus.annotation.FieldStrategy
import com.baomidou.mybatisplus.annotation.TableLogic
import com.baomidou.mybatisplus.core.toolkit.StringPool
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils.convertTrim
import org.springframework.data.domain.Sort
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

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
    val criterion: List<QueryCriteria> = listOf(),
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

  /**  来自Page参数的排序条件 */
  var extraSortScript: String? = null

  fun buildCountSql(): String {
    // 必须先解析查询条件才能确定条件中用于判断的字段来自哪张表
    val where = resolveWhere()
    val from = resolveFrom(false, resolveWhere(), "", true)
    return String.format(SELECT_COUNT, from.toSql(), where)
  }

  fun buildUpdateSql(): String {
    return String.format(
        UPDATE, tableName().name, resolveUpdateProperties(false),
        resolveUpdateWhere()
    )
  }

  fun containedTables(): List<String> {
    return (this.criterion.map {
      it.toSqlWithoutTest(mappings).split(".")[0]
    } + ColumnsResolver.resolveIncludedTables(mappings, properties)).distinct()
  }

  fun convertIf(sqlScript: String, property: String, mapping: FieldMapping): String {
    if (mapping.tableFieldInfo.whereStrategy == FieldStrategy.IGNORED) {
      return sqlScript
    }
    return when {
      mapping.tableFieldInfo.whereStrategy == FieldStrategy.NOT_EMPTY
          && mapping.tableFieldInfo.isCharSequence ->
        SqlScriptUtils.convertIf(
            sqlScript,
            String.format("%s != null and %s != ''", property, property), false
        )

      else                                         -> SqlScriptUtils.convertIf(
          sqlScript,
          String.format("%s != null", property),
          false
      )
    }
  }

  /**
   * 获取 set sql 片段
   *
   * @param prefix 前缀
   * @return sql 脚本片段
   * @author 吴昊
   */
  fun getSqlSet(prefix: String?, mapping: FieldMapping, insertUpdate: Boolean): String {
    val column = mapping.tableFieldInfo.column
    val newPrefix = prefix ?: StringPool.EMPTY
    // 默认: column=
    var sqlSet = ColumnsResolver.wrapColumn(column) + StringPool.EQUALS
    sqlSet += if (insertUpdate) {
      "(if(VALUES($column) = null, $column, VALUES($column)))"
    } else {
      when {
        !mapping.tableFieldInfo.update.isNullOrBlank() -> String.format(mapping.tableFieldInfo.update, column)
        else                                           -> SqlScriptUtils.safeParam(
            newPrefix + mapping.getPropertyExpression(
                null,
                false
            )
        )
      }
    }
    sqlSet += StringPool.COMMA
    if (insertUpdate) {
      return sqlSet
    }
    return when (mapping.tableFieldInfo.fieldFill) {
      FieldFill.UPDATE,
      FieldFill.INSERT_UPDATE -> sqlSet

      else                    -> convertIf(sqlSet, newPrefix + mapping.tableFieldInfo.property, mapping)
    }
  }

  fun includeJoins(): Boolean {
    return properties.includes.isEmpty()
        || properties.includes.any { !it.startsWith(mappings.tableInfo.tableName) }
        || containedTables().distinct().any { it != mappings.tableInfo.tableName }
  }

  fun limitInSubQuery(): Boolean {
    return hasCollectionJoinProperty() && limitation != null
  }

  /**
   * 对查询条件进行分组，类似findByNameOrDescriptionLikeKeywords的表达式，
   * 由于name未指明条件类型，自动将name和description归并为一组，并且设置条件类型和description一样为like
   * 同组条件如果超过一个在构建查询语句时添加括号
   */
  fun resolveGroups(): List<QueryCriteriaGroup> {
    val result = arrayListOf<QueryCriteriaGroup>()
    var tmp = QueryCriteriaGroup()
    criterion.forEachIndexed { _, criteria ->
      when {
        criteria.hasExpression()                       -> {
          if (!tmp.isEmpty()) {
            result.add(tmp)
            tmp = QueryCriteriaGroup()
          }
          result.add(QueryCriteriaGroup(mutableListOf(criteria)))
        }

        tmp.isEmpty() && criteria.append == Append.AND -> result.add(
            QueryCriteriaGroup(
                mutableListOf(
                    criteria
                )
            )
        )

        tmp.isEmpty()                                  -> tmp.add(criteria)
        tmp.onlyDefaultEq()
            && criteria.operator == Operations.EqDefault
            && criteria.append == Append.OR
                                                       -> tmp.add(criteria)

        tmp.onlyDefaultEq()                            -> {
          if (criteria.operator == Operations.EqDefault) {
            result.addAll(tmp.criterion.map { QueryCriteriaGroup(mutableListOf(it)) })
            result.add(QueryCriteriaGroup(mutableListOf(criteria)))
          } else {
            result.add(
                QueryCriteriaGroup(
                    (tmp.criterion.map {
                      QueryCriteria(
                          it.property, criteria.operator, it.append, criteria.parameters, it
                          .specificValue, mappings
                      )
                    }.toMutableList() + criteria).toMutableList()
                )
            )
          }
          tmp = QueryCriteriaGroup()
        }

        else                                           -> {
          result.add(tmp)
          tmp = QueryCriteriaGroup(mutableListOf(criteria))
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

  fun resolveUpdateWhere(): String {
    return when {
      criterion.isEmpty() -> String.format(
          """
        WHERE %s = #{%s}
      """, mappings.tableInfo.keyColumn, mappings.tableInfo.keyProperty
      )

      else                -> resolveWhere()
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
    return String.format(DELETE, tableName().name, resolveWhere())
  }

  private fun buildExistsSql(): String {
    return buildCountSql()
  }

  private fun buildInsertSql(): String {
    val columns = mappings.insertFields(this.properties)
    val template: String
    val values = when {
      function.name.endsWith("All")   -> {
        template = when {
          function.name.endsWith("OrUpdateAll") -> BATCH_INSERT_OR_UPDATE
          else                                  -> BATCH_INSERT
        }
        mappings.insertProperties("item.", this.properties)
      }

      this.properties.isNotEmpty()    -> {
        template = when {
          function.name.endsWith("OrUpdate") -> INSERT_OR_UPDATE
          else                               -> INSERT
        }
        mappings.insertProperties(insertProperties = this.properties)
      }

      this.properties.isIncludeEmpty()
          && this.criterion.isEmpty() -> {
        template = when {
          function.name.endsWith("OrUpdate") -> INSERT_OR_UPDATE
          else                               -> INSERT
        }
        mappings.insertProperties()
      }

      else                            -> throw BuildSQLException("无法解析${this.function}")
    }
    if (columns.size != values.size) {
      throw BuildSQLException("解析方法[${function}]失败，插入的字段\n$columns\n与插入的值\n$values\n数量不一致")
    }
    return String.format(
        template, mappings.tableInfo.tableName,
        columns.joinToString(Strings.COLUMN_SEPARATOR),
        values.joinToString(Strings.COLUMN_SEPARATOR),
        resolveUpdateProperties(true)
    )
  }

  private fun buildLogicDeleteSql(): String {
    val mapper = this.mappings.mappings.find { it.field.isAnnotationPresent(TableLogic::class.java) }
      ?: throw IllegalStateException("缺少逻辑删除字段，请在字段上添加@TableLogic注解")
    val logic = function.findAnnotation<Logic>()!!
    val value = when (logic.flag) {
      DeleteValue.Deleted -> 1
      else                -> 0
    }
    return "<script>UPDATE ${tableName().name} SET ${mapper.column} = $value " + resolveWhere() + "</script>"
  }

  private fun buildScript(template: String, vararg args: String): String {
    val formattedSql = String.format(template, *args).trim()
    return when {
      formattedSql.startsWith(SCRIPT_START) -> formattedSql
      else                                  -> SCRIPT_START + LINE_BREAK + formattedSql.trim() + LINE_BREAK + SCRIPT_END
    }
  }

  /**
   * 构建select查询语句
   */
  private fun buildSelectSql(): String {
    // 构建select的列
    val groupBy = resolveGroupBy(mappings)
    val whereSqlResult = resolveWhere()
    val order = resolveOrder()
    val limit = resolveLimit()
    val limitInSubQuery = limitInSubQuery()
    val from = resolveFrom(limitInSubQuery, whereSqlResult, limit)
    val tables = when (from) {
      is SubQueryDeclaration -> from.defaultFrom.getTables()

      is FromDeclaration     -> from.getTables()

      is TableName           -> listOf(from)

      else                   -> listOf()
    }
    val buildColsResult = ColumnsResolver.resolve(mappings, properties).map { col ->
      val t = tables.find { it.name == col.table?.name }
      if (t != null) {
        SelectColumn(t, col.column, col.alias, col.type)
      } else {
        col
      }
    }
    return when {
      limitInSubQuery -> buildScript(
          SELECT, buildColsResult.joinToString(",\n\t") { it.toSql() }, from.toSql(),
          resolvedNameAnnotation?.joinAppend ?: "",
          "", groupBy, order, ""
      )

      else            -> buildScript(
          SELECT, buildColsResult.joinToString(",\n\t") { it.toSql() }, from.toSql(),
          resolvedNameAnnotation?.joinAppend ?: "",
          whereSqlResult, groupBy, order, limit
      )
    }
  }

  private fun hasCollectionJoinProperty(): Boolean {
    return when {
      this.properties.isIncludeNotEmpty() -> this.mappings.mappings
          .filter { it.property in this.properties }

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
      limitInSubQuery: Boolean, whereSqlResult: String, limit: String,
      isCount: Boolean = false
  ): ISqlPart {
    val onlyIncludesTables = if (isCount) {
      criterion.map {
        it.columns.mapNotNull { it.table }
      }.flatten().distinct()
    } else {
      null
    }
    val defaultFrom = mappings.fromDeclaration(properties, containedTables(), onlyIncludesTables, limitInSubQuery)

    return when {
      limitInSubQuery -> SubQueryDeclaration(tableName(), whereSqlResult, limit, defaultFrom)
      includeJoins()  -> defaultFrom
      else            -> tableName()
    }
  }

  private fun resolveGroupBy(mappings: FieldMappings): String {
    val mappingList = when {
      properties.isIncludeNotEmpty() -> mappings.mappings
          .filter { it.property in properties }

      else                           -> mappings.mappings.filter { it.property !in properties.excludes }
    }
    val groupProperties = mappingList.mapNotNull { it.joinInfo }
        .filter { it is PropertyJoinInfo }
        .mapNotNull { (it as PropertyJoinInfo).groupBy }
    return when {
      groupProperties.isNotEmpty() -> "GROUP BY\n\t${groupProperties.joinToString(", ")}"
      else                         -> ""
    }
  }

  private fun resolveUpdateProperties(isInsertUpdate: Boolean): String {
    if (this.properties.isIncludeEmpty()) {
      return wrapSetScript(mappings.mappings.filter {
        it.joinInfo == null && !it.updateIgnore
            && it.property !in this.properties.excludes
            && it.property !in this.properties.updateExcludeProperties
            && it.property != mappings.tableInfo.keyProperty
      }.joinToString(StringPool.NEWLINE) {
        getSqlSet(null, it, isInsertUpdate)
      }, isInsertUpdate)
    }
    val builders = this.properties.includes.map { property ->
      val mapping = this.mappings.mappings.firstOrNull { it.property == property }
      if (mapping == null) {
        throw BuildSQLException("无法解析待更新属性：$property, 方法：$function")
      } else {
        getSqlSet(null, mapping, isInsertUpdate)
      }
    }
    return wrapSetScript(builders.joinToString(StringPool.NEWLINE), isInsertUpdate)
  }

  private fun resolveWhere(): String {
    val groups = resolveGroups()
    val groupBuilders = groups.map {
      it.toSql(mappings)
    }
    val whereAppend = resolvedNameAnnotation?.whereAppend
    return when {
      criterion.isNotEmpty() || (whereAppend != null && whereAppend.isNotBlank()) ->
        String.format(WHERE, trimCondition(
            groupBuilders.joinToString(LINE_BREAK) +
                LINE_BREAK + (whereAppend ?: EMPTY)
        ).lines().joinToString(LINE_BREAK) {
          "\t".repeat(3) + it
        })

      else                                                                        -> ""
    }
  }

  private fun tableName(): TableName {
    return TableName(mappings.tableInfo.tableName, mappings.tableInfo.tableName.replace('.', '_'))
  }

  private fun trimCondition(sql: String): String {
    return sql.trim().trim(" AND").trim(" OR")
  }

  private fun wrapSetScript(sql: String, noSetPrefix: Boolean): String {
    val prefix = when {
      noSetPrefix -> ""
      else        -> SQLKeywords.SET
    }
    return convertTrim(sql, prefix, null, null, ",")
  }

}
