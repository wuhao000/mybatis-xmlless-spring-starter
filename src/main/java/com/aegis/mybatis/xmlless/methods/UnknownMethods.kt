package com.aegis.mybatis.xmlless.methods

import com.aegis.mybatis.xmlless.config.QueryResolver
import com.aegis.mybatis.xmlless.model.QueryType
import com.aegis.mybatis.xmlless.model.ResolvedQueries
import com.aegis.mybatis.xmlless.model.ResolvedQuery
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.toolkit.StringPool
import com.baomidou.mybatisplus.core.toolkit.StringPool.DOT
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator
import org.apache.ibatis.executor.keygen.NoKeyGenerator
import org.apache.ibatis.mapping.MappedStatement
import org.apache.ibatis.mapping.SqlCommandType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.full.declaredFunctions


/**
 *
 * Created by 吴昊 on 2018-12-09.
 *
 * @author 吴昊
 * @since 0.0.1
 */
class UnknownMethods : BaseMethod() {

  companion object {
    const val COUNT_STATEMENT_SUFFIX = "CountAllSuffix"
    private val LOG: Logger = LoggerFactory.getLogger(UnknownMethods::class.java)
    private val log: Logger = LoggerFactory.getLogger(UnknownMethods::class.java)
    private val possibleErrors = listOf(
        "未在级联属性@ModifyIgnore注解将其标记为不需要插入或更新的字段"
    )
  }

  override fun innerInject(mapperClass: Class<*>, modelClass: Class<*>, tableInfo: TableInfo): MappedStatement {
    val statementNames = this.configuration.mappedStatementNames
    val unmappedFunctions = mapperClass.kotlin.declaredFunctions.filter {
      (mapperClass.name + DOT + it.name) !in statementNames
    }
    val resolvedQueries = ResolvedQueries(mapperClass, unmappedFunctions)
    unmappedFunctions.forEach { function ->
      val resolvedQuery: ResolvedQuery = QueryResolver.resolve(function, tableInfo, modelClass, mapperClass)
      resolvedQueries.add(resolvedQuery)
      if (resolvedQuery.query != null && resolvedQuery.sql != null) {
        val sql = resolvedQuery.sql
        try {
          val sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass)
          when (resolvedQuery.type()) {
            in listOf(QueryType.Select,
                QueryType.Exists,
                QueryType.Count) -> {
              val returnType = resolvedQuery.returnType
              var resultMap = resolvedQuery.resultMap
              if (resultMap == null && resolvedQuery.type() == QueryType.Select) {
                val resultMapId = mapperClass.name + StringPool.DOT + function.name
                resultMap = resolvedQuery.resolveResultMap(resultMapId, this.builderAssistant,
                    modelClass,
                    resolvedQuery.query.mappings)
              }
              if (returnType == modelClass && resultMap == null) {
                addSelectMappedStatement(mapperClass, function.name, sqlSource, returnType, tableInfo)
              } else {
                addMappedStatement(mapperClass, function.name,
                    sqlSource, SqlCommandType.SELECT, null,
                    resultMap, returnType,
                    NoKeyGenerator(), null, null)
              }
              // 为select查询自动生成count的statement，用于分页时查询总数
              if (resolvedQuery.type() == QueryType.Select) {
                addSelectMappedStatement(
                    mapperClass, function.name + COUNT_STATEMENT_SUFFIX,
                    languageDriver.createSqlSource(configuration, resolvedQuery.countSql(), modelClass),
                    Long::class.java,
                    tableInfo
                )
              }
            }
            QueryType.Delete     -> {
              addDeleteMappedStatement(mapperClass, function.name, sqlSource)
            }
            QueryType.Insert     -> {
              val keyGenerator = if (tableInfo.idType == IdType.AUTO) {
                Jdbc3KeyGenerator.INSTANCE
              } else {
                NoKeyGenerator.INSTANCE
              }
              addInsertMappedStatement(
                  mapperClass, modelClass, function.name, sqlSource,
                  keyGenerator,
                  tableInfo.keyProperty, tableInfo.keyColumn
              )
            }
            QueryType.Update     -> {
              addUpdateMappedStatement(mapperClass, modelClass, function.name, sqlSource)
            }
            null                 -> {
            }
            else                 -> {
            }
          }
        } catch (ex: Exception) {
          LOG.error("""出错了 >>>>>>>>
              可能存在下列情形之一：
              ${possibleErrors.joinToString { String.format("\n\t\t-\t%s\n", it) }}
              """.trimIndent(), ex)
        }
      }
    }
    resolvedQueries.log()
    return addSelectMappedStatement(mapperClass,
        "unknown",
        languageDriver.createSqlSource(configuration, "select 1", modelClass),
        modelClass, tableInfo
    )
  }

}
