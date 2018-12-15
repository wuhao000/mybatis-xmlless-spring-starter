package com.aegis.mybatis.xmlless.methods

import com.aegis.mybatis.xmlless.config.MappingResolver
import com.aegis.mybatis.xmlless.config.QueryResolver
import com.aegis.mybatis.xmlless.model.QueryType
import com.aegis.mybatis.xmlless.model.ResolvedQueries
import com.aegis.mybatis.xmlless.model.ResolvedQuery
import com.aegis.mybatis.xmlless.resolver.ResultMapResolver
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.toolkit.StringPool
import com.baomidou.mybatisplus.core.toolkit.StringPool.DOT
import com.baomidou.mybatisplus.extension.injector.AbstractLogicMethod
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
class UnknownMethods : AbstractLogicMethod() {

  companion object {
    const val COUNT_STATEMENT_SUFFIX = "CountAllSuffix"
    const val HANDLER_PREFIX = "typeHandler="
    const val PROPERTY_PREFIX = "#{"
    const val PROPERTY_SUFFIX = "}"
    private val LOG: Logger = LoggerFactory.getLogger(UnknownMethods::class.java)
    private val log: Logger = LoggerFactory.getLogger(UnknownMethods::class.java)
    private val possibleErrors = listOf(
        "未在级联属性@ModifyIgnore注解将其标记为不需要插入或更新的字段"
    )
  }

  override fun injectMappedStatement(mapperClass: Class<*>, modelClass: Class<*>, tableInfo: TableInfo): MappedStatement {
    // 修正表信息，主要是针对一些JPA注解的支持以及本项目中自定义的一些注解的支持，
    MappingResolver.fixTableInfo(modelClass, tableInfo, builderAssistant)
    // 判断Mapper方法是否已经定义了sql声明，如果没有定义才进行注入，这样如果存在Mapper方法在xml文件中有定义则会优先使用，如果没有定义才会进行推断
    val statementNames = this.configuration.mappedStatementNames
    val unmappedFunctions = mapperClass.kotlin.declaredFunctions.filter {
      (mapperClass.name + DOT + it.name) !in statementNames
    }
    // 解析未定义的方法，进行sql推断
    val resolvedQueries = ResolvedQueries(mapperClass, unmappedFunctions)
    unmappedFunctions.forEach { function ->
      val resolvedQuery: ResolvedQuery = QueryResolver.resolve(function, tableInfo, modelClass, mapperClass)
      resolvedQueries.add(resolvedQuery)
      // query为null则表明推断失败，resolvedQuery中将包含推断失败的原因，会在后面进行统一输出，方便开发人员了解sql推断的具体结果和失败的具体原因
      if (resolvedQuery.query != null && resolvedQuery.sql != null) {
        val sql = resolvedQuery.sql
        try {
          val sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass)
          when (resolvedQuery.type()) {
            in listOf(QueryType.Select,
                QueryType.Exists,
                QueryType.Count) -> {
              val returnType = resolvedQuery.returnType
              if (returnType == null) {
                throw IllegalStateException("无法解析方法${function}的返回类型")
              }
              var resultMap = resolvedQuery.resultMap
              if (resultMap == null && resolvedQuery.type() == QueryType.Select) {
                // 如果没有指定resultMap，则自动生成resultMap
                resultMap = ResultMapResolver.resolveResultMap(mapperClass.name + StringPool.DOT + function.name, this.builderAssistant,
                    modelClass, resolvedQuery.query.mappings, resolvedQuery.returnType)
              }
              // addSelectMappedStatement这个方法中会使用默认的resultMap，该resultMap映射的类型和modelClass一致，所以如果当前方法的返回值和modelClass
              // 不一致时，不能使用该方法，否则会产生类型转换错误
              if (returnType == modelClass && resultMap == null) {
                addSelectMappedStatement(mapperClass, function.name, sqlSource, returnType, tableInfo)
              } else {
                addMappedStatement(mapperClass, function.name,
                    sqlSource, SqlCommandType.SELECT, null, resultMap, returnType,
                    NoKeyGenerator(), null, null)
              }
              // 为select查询自动生成count的statement，用于分页时查询总数
              if (resolvedQuery.type() == QueryType.Select) {
                addSelectMappedStatement(mapperClass, function.name + COUNT_STATEMENT_SUFFIX,
                    languageDriver.createSqlSource(configuration, resolvedQuery.countSql(), modelClass),
                    Long::class.java, tableInfo
                )
              }
            }
            QueryType.Delete     -> {
              addDeleteMappedStatement(mapperClass, function.name, sqlSource)
            }
            QueryType.Insert     -> {
              // 如果id类型为自增，则将自增的id回填到插入的对象中
              val keyGenerator = when {
                tableInfo.idType == IdType.AUTO -> Jdbc3KeyGenerator.INSTANCE
                else                            -> NoKeyGenerator.INSTANCE
              }
              addInsertMappedStatement(
                  mapperClass, modelClass, function.name, sqlSource,
                  keyGenerator, tableInfo.keyProperty, tableInfo.keyColumn
              )
            }
            QueryType.Update     -> {
              addUpdateMappedStatement(mapperClass, modelClass, function.name, sqlSource)
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
    // 其实这里的return是没有必要的，mybatis plus也没有对这个返回值做任何的处理，
    // 所里这里随便返回了一个sql声明
    return addSelectMappedStatement(mapperClass,
        "unknown",
        languageDriver.createSqlSource(configuration, "select 1", modelClass),
        modelClass, tableInfo
    )
  }

}
