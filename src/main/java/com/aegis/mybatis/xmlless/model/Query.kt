package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.annotations.ResolvedName
import com.aegis.mybatis.xmlless.annotations.SelectedProperties
import com.aegis.mybatis.xmlless.config.ColumnsResolver
import com.aegis.mybatis.xmlless.config.Operations
import com.baomidou.mybatisplus.core.toolkit.StringPool
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils
import org.springframework.data.domain.Sort
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

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
    val type: QueryType = QueryType.Select,
    private val properties: List<String> = listOf(),
    val conditions: List<Condition> = listOf(),
    val sorts: List<Sort.Order> = listOf(),
    /** 待解析的方法 */
    val function: KFunction<*>,
    /**  数据对象的与数据库的映射管理 */
    val mappings: FieldMappings,
    var limitation: Limitation? = null,
    val resolvedNameAnnotation: ResolvedName?,
    val mapperClass: Class<*>
) {

  /**  方法指定忽略的字段 */
//  val ignoredProperties: List<String>? = function.findAnnotation<IgnoredProperties>()?.properties?.toList()

  var extraSortScript: String? = null
  /**  方法指定查询的字段 */
  private val selectedProperties: List<String>? = function.findAnnotation<SelectedProperties>()?.properties?.toList()

  companion object {
    /**  删除语句模板 */
    private const val DELETE = """<script>
DELETE FROM
  %s
%s
</script>"""
    /**  inser语句模板 */
    private const val INSERT = """<script>
INSERT INTO
  %s(%s)
VALUES
  <foreach collection="list" item="item" separator=",">
    (%s)
  </foreach>
</script>
"""
    /**  limit 语句模板 */
    private const val LIMIT = "LIMIT #{%s}, #{%s}"
    /**  查询语句模板 */
    private const val SELECT = """<script>
SELECT
  %s
FROM
  %s %s
%s %s %s
</script>"""
    /**  count语句模板 */
    private const val SELECT_COUNT = """<script>
SELECT
  COUNT(*)
FROM
  %s
%s
</script>"""
    /**  子查询构建模板 */
    private const val SUB_QUERY = """(SELECT
  *
FROM
  %s
%s
%s) AS %s"""
    /**  更新语句模板 */
    private const val UPDATE = """<script>
UPDATE
  %s
  %s
%s
</script>"""
    /**  where条件模板 */
    private const val WHERE = """<where>
  <trim suffixOverrides=" AND">
    <trim suffixOverrides=" OR">
      %s
    </trim>
  </trim>
</where>"""
  }

  fun toCountSql(): BuildSqlResult {
    return buildCountSql()
  }

  fun toSql(): BuildSqlResult {
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

  private fun buildCountSql(): BuildSqlResult {
    return buildSql(SELECT_COUNT, BuildSqlResult(tableName()), resolveWhere())
  }

  private fun buildDeleteSql(): BuildSqlResult {
    return buildSql(DELETE, BuildSqlResult(tableName()), resolveWhere())
  }

  private fun buildExistsSql(): BuildSqlResult {
    return buildSql(SELECT_COUNT, BuildSqlResult(tableName()), resolveWhere())
  }

  private fun buildInsertSql(): BuildSqlResult {
    val columnsString = mappings.insertFields()
    return if (function.name.endsWith("All")) {
      val valueString = mappings.insertProperties("item.")
      BuildSqlResult(String.format(INSERT, mappings.tableInfo.tableName, columnsString, valueString))
    } else if (this.properties.isEmpty()
        && this.conditions.isEmpty()) {
      val valueString = mappings.insertProperties()
      BuildSqlResult("""INSERT INTO
      |   ${mappings.tableInfo.tableName}($columnsString)
      | VALUE
      |   ($valueString)
    """.trimMargin())
    } else {
      BuildSqlResult(null, "无法解析${this.function}")
    }
  }

  /**
   * 构建select查询语句
   */
  private fun buildSelectSql(): BuildSqlResult {
    // 构建select的列
    val buildColsResult = ColumnsResolver.resolve(mappings, properties())
    val whereSqlResult = resolveWhere()
    val order = resolveOrder()
    val limit = resolveLimit()
    val limitInSubQuery = limitInSubQuery()
    val from = resolveFrom(limitInSubQuery, whereSqlResult, limit)
    return if (limitInSubQuery) {
      buildSql(SELECT, buildColsResult,
          from,
          BuildSqlResult(resolvedNameAnnotation?.joinAppend ?: ""),
          BuildSqlResult.empty(),
          order,
          BuildSqlResult.empty()
      )
    } else {
      buildSql(SELECT, buildColsResult,
          from,
          BuildSqlResult(resolvedNameAnnotation?.joinAppend ?: ""),
          whereSqlResult,
          order,
          limit
      )
    }
  }

  private fun buildSql(template: String, vararg sqlArray: BuildSqlResult): BuildSqlResult {
    val failedReasons = sqlArray.map { it.reasons }.flatten()
    if (failedReasons.isNotEmpty()) {
      return BuildSqlResult(null, failedReasons)
    }
    return BuildSqlResult(String.format(template, *sqlArray.map { it.sql?.trim() }.toTypedArray()))
  }

  private fun buildUpdateSql(): BuildSqlResult {
    return buildSql(UPDATE, BuildSqlResult(tableName()), resolveUpdateProperties(),
        resolveUpdateWhere())
  }

  private fun hasCollectionJoinProperty(): Boolean {
    return if (this.properties().isNotEmpty()) {
      this.mappings.mappings.filter { it.property in this.properties() }
    } else {
      this.mappings.mappings
    }.filter {
      it.joinInfo != null
    }.any {
      Collection::class.java.isAssignableFrom(it.tableFieldInfo.propertyType)
    }
  }

  private fun includeJoins(): Boolean {
    return properties().isEmpty() || properties().any { !it.startsWith(mappings.tableInfo.tableName) }
  }

  private fun limitInSubQuery(): Boolean {
    return hasCollectionJoinProperty() && limitation != null
  }

  private fun properties(): List<String> {
    return selectedProperties ?: properties
  }

  private fun resolveFrom(limitInSubQuery: Boolean, whereSqlResult: BuildSqlResult, limit: BuildSqlResult): BuildSqlResult {
    val defaultFrom = BuildSqlResult(mappings.fromDeclaration())
    return when {
      limitInSubQuery -> buildSql(SUB_QUERY, BuildSqlResult(tableName()), whereSqlResult, limit, defaultFrom)
      includeJoins()  -> defaultFrom
      else            -> BuildSqlResult(tableName())
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

  private fun resolveLimit(): BuildSqlResult {
    if (limitation != null) {
      return BuildSqlResult(String.format(LIMIT, limitation?.offsetParam, limitation?.sizeParam))
    }
    return buildSql("")
  }

  private fun resolveOrder(): BuildSqlResult {
    when {
      this.sorts.isNotEmpty() -> return BuildSqlResult("""
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
""")
      extraSortScript != null -> return BuildSqlResult("""
      <trim suffixOverrides="ORDER BY">
      ORDER BY
      $extraSortScript
</trim>
""")
      else                    -> return BuildSqlResult("")
    }
  }

  private fun resolveUpdateProperties(): BuildSqlResult {
    if (this.properties.isEmpty()) {
      val sqlScript = wrapSetScript(this.mappings.mappings.filter {
        it.joinInfo == null && !it.updateIgnore
            && it.property != mappings.tableInfo.keyProperty
      }.joinToString(StringPool.NEWLINE) {
        it.tableFieldInfo.getSqlSet(null)
      })

      return BuildSqlResult(sqlScript)
    }
    val builders = this.properties.map { property ->
      val mapping = this.mappings.mappings.firstOrNull { it.property == property }
      if (mapping == null) {
        BuildSqlResult(null, "无法解析待更新属性：$property, 方法：$function")
      } else {
        BuildSqlResult(
            mapping.tableFieldInfo.getSqlSet(null)
        )
      }
    }
    val rs = builders.map { it.reasons }.flatten()
    if (rs.isNotEmpty()) {
      return BuildSqlResult(null, rs)
    }
    return BuildSqlResult(
        wrapSetScript(builders.joinToString(StringPool.NEWLINE) { it.sql!! })
    )
  }

  private fun resolveUpdateWhere(): BuildSqlResult {
    val result = resolveWhere()
    return if (result.sql != null && result.sql.isBlank()) {
      BuildSqlResult(String.format("""
        WHERE %s = #{%s}
      """, mappings.tableInfo.keyColumn, mappings.tableInfo.keyProperty))
    } else {
      result
    }
  }

  private fun resolveWhere(): BuildSqlResult {
    val groups = resolveGroups()
    val groupBuilders = groups.map {
      toGroupSqlBuild(it)
    }
    val rs = groupBuilders.map { it.reasons }.flatten()
    if (rs.isNotEmpty()) {
      return BuildSqlResult(null, rs)
    }
    return if (conditions.isNotEmpty()) {
      BuildSqlResult(
          String.format(WHERE, trimCondition(groupBuilders.joinToString("\n") { it.sql!! }
              + "\n" + (resolvedNameAnnotation?.whereAppend ?: "")))
      )
    } else {
      BuildSqlResult("")
    }
  }

  private fun tableName(): String {
    return mappings.tableInfo.tableName
  }

  private fun toGroupSqlBuild(it: List<Condition>): BuildSqlResult {
    val list = it.map { it.toSql(mappings) }
    list.map { it.reasons }.flatten().let {
      if (it.isNotEmpty()) {
        return BuildSqlResult(null, it)
      }
    }
    return if (it.size > 1) {
      it.first().wrapWithTests(
          "(" + trimCondition(it.map { it.toSqlWithoutTest(mappings).sql }
              .joinToString(" ")) + ")"
      )
    } else {
      list.first()
    }
  }

  private fun trimCondition(sql: String): String {
    return sql.trim().trim(" AND").trim(" OR")
  }

  private fun wrapSetScript(sql: String): String? {
    return SqlScriptUtils.convertTrim(sql, "SET", null, null, ",")
  }

}
