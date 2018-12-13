package com.aegis.mybatis.xmlless.methods

import com.aegis.mybatis.xmlless.config.QueryResolver
import com.aegis.mybatis.xmlless.model.QueryType
import com.aegis.mybatis.xmlless.model.ResolvedQueries
import com.aegis.mybatis.xmlless.model.ResolvedQuery
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.toolkit.StringPool.DOT
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

    const val COUNT_STATEMENT_SUFFIX = "CountAll"
    private val log: Logger = LoggerFactory.getLogger(UnknownMethods::class.java)
  }

  override fun getId(): String {
    return "unknown"
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
      if (resolvedQuery.query != null) {
        val sql = resolvedQuery.sql
        if (sql != null) {
          val sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass)
          when (resolvedQuery.type()) {
            in listOf(QueryType.Select,
                QueryType.Exists,
                QueryType.Count) -> {
              val returnType = resolvedQuery.returnType
              val resultMap = resolvedQuery.resultMap
              if (returnType == modelClass && resultMap == null) {
                addSelectMappedStatement(
                    mapperClass,
                    function.name,
                    sqlSource,
                    returnType, tableInfo
                )
              } else {
                addMappedStatement(mapperClass, function.name,
                    sqlSource, SqlCommandType.SELECT, null,
                    resultMap, returnType,
                    NoKeyGenerator(), null, null)
              }
              if (resolvedQuery.type() == QueryType.Select) {
                addSelectMappedStatement(
                    mapperClass,
                    function.name + COUNT_STATEMENT_SUFFIX,
                    languageDriver.createSqlSource(configuration, resolvedQuery.countSql(), modelClass),
                    Long::class.java,
                    tableInfo
                )
              }
            }
            QueryType.Delete     -> {
              addDeleteMappedStatement(
                  mapperClass, function.name, sqlSource
              )
            }
            QueryType.Insert     -> {
              addInsertMappedStatement(
                  mapperClass, modelClass, function.name, sqlSource, NoKeyGenerator(),
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

  override fun requireKeyColumn(): Boolean {
    return false
  }

}
