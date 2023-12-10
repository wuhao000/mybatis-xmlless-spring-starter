package com.aegis.mybatis.xmlless.methods

import com.aegis.mybatis.xmlless.config.MappingResolver
import com.aegis.mybatis.xmlless.exception.BuildSQLException
import com.aegis.mybatis.xmlless.generator.IdGeneratorUtil
import com.aegis.mybatis.xmlless.model.QueryType
import com.aegis.mybatis.xmlless.model.ResolvedQueries
import com.aegis.mybatis.xmlless.model.ResolvedQuery
import com.aegis.mybatis.xmlless.resolver.QueryResolver
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.core.injector.AbstractMethod
import com.baomidou.mybatisplus.core.metadata.TableInfo
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator
import org.apache.ibatis.executor.keygen.NoKeyGenerator
import org.apache.ibatis.mapping.MappedStatement
import org.apache.ibatis.mapping.SqlCommandType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.Method


/**
 *
 * Created by 吴昊 on 2018-12-09.
 *
 * @author 吴昊
 * @since 0.0.1
 */
class XmlLessMethods : AbstractMethod("") {

  companion object {
    const val COUNT_STATEMENT_SUFFIX = "CountAllSuffix"
    const val HANDLER_PREFIX = "typeHandler="
    private val LOG: Logger = LoggerFactory.getLogger(XmlLessMethods::class.java)
    private val possibleErrors = listOf(
        "未在级联属性@ModifyIgnore注解将其标记为不需要插入或更新的字段",
        "复杂对象未指定TypeHandler"
    )
  }

  override fun injectMappedStatement(
      mapperClass: Class<*>,
      modelClass: Class<*>,
      tableInfo: TableInfo
  ): MappedStatement {
    // 修正表信息，主要是针对一些JPA注解的支持以及本项目中自定义的一些注解的支持，
    MappingResolver.fixTableInfo(tableInfo, builderAssistant)
    // 判断Mapper方法是否已经定义了sql声明，如果没有定义才进行注入，这样如果存在Mapper方法在xml文件中有定义则会优先使用，如果没有定义才会进行推断
    val unmappedMethods = mapperClass.methods.filter {
      it.declaringClass != Object::class.java
    }.filter {
      !configuration.hasStatement("${mapperClass.name}$DOT${it.name}")
    }.filter {
      // 过滤mybatis-plus的生成方法
      !it.declaringClass.name.startsWith("com.baomidou.mybatisplus.core.mapper")
    }
    // 解析未定义的方法，进行sql推断
    val resolvedQueries = ResolvedQueries(mapperClass)
    unmappedMethods.forEach { method ->
      val resolvedQuery: ResolvedQuery =
          QueryResolver.resolve(method, tableInfo, modelClass, mapperClass, builderAssistant)
      resolvedQueries.add(resolvedQuery)
      // query为null则表明推断失败，resolvedQuery中将包含推断失败的原因，会在后面进行统一输出，方便开发人员了解sql推断的具体结果和失败的具体原因
      if (resolvedQuery.query != null && resolvedQuery.sql != null) {
        resolve(resolvedQuery, modelClass, method, mapperClass, tableInfo)
      }
    }
    resolvedQueries.log()
    // 其实这里的return是没有必要的，mybatis plus也没有对这个返回值做任何的处理，
    // 所里这里随便返回了一个sql声明
    return addSelectMappedStatementForTable(
        mapperClass,
        "unknown",
        languageDriver.createSqlSource(configuration, "select 1", modelClass),
        tableInfo
    )
  }

  private fun resolve(
      resolvedQuery: ResolvedQuery,
      modelClass: Class<*>,
      function: Method,
      mapperClass: Class<*>,
      tableInfo: TableInfo
  ) {
    val query = resolvedQuery.query!!
    val sql = resolvedQuery.sql
    try {
      val sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass)
      when (resolvedQuery.type!!) {
        QueryType.Select,
        QueryType.Exists,
        QueryType.Count       -> {
          val returnType = resolvedQuery.returnType ?: throw BuildSQLException("无法解析方法${function}的返回类型")
          val resultMap = resolvedQuery.resultMap
          // addSelectMappedStatement这个方法中会使用默认的resultMap，该resultMap映射的类型和modelClass一致，所以如果当前方法的返回值和modelClass
          // 不一致时，不能使用该方法，否则会产生类型转换错误
          if (returnType == modelClass && resultMap == null) {
            addSelectMappedStatementForTable(mapperClass, function.name, sqlSource, tableInfo)
          } else {
            addMappedStatement(
                mapperClass, function.name,
                sqlSource, SqlCommandType.SELECT, null, resultMap, returnType,
                NoKeyGenerator(), null, null
            )
          }
          // 为select查询自动生成count的statement，用于分页时查询总数
          if (resolvedQuery.type == QueryType.Select) {
            addSelectMappedStatementForOther(
                mapperClass, function.name + COUNT_STATEMENT_SUFFIX,
                languageDriver.createSqlSource(configuration, query.buildCountSql(), modelClass),
                Long::class.java
            )
          }
        }

        QueryType.Delete      -> {
          addDeleteMappedStatement(mapperClass, function.name, sqlSource)
        }

        QueryType.Insert      -> {
          // 如果id类型为自增，则将自增的id回填到插入的对象中
          val generator = MappingResolver.resolveKeyGenerator(modelClass)
          val keyGenerator = if (generator != null) {
            IdGeneratorUtil.getGenerator(generator)
          } else if (tableInfo.idType == IdType.AUTO) {
            Jdbc3KeyGenerator.INSTANCE
          } else {
            NoKeyGenerator.INSTANCE
          }
          addInsertMappedStatement(
              mapperClass, modelClass, function.name, sqlSource,
              keyGenerator, tableInfo.keyProperty, tableInfo.keyColumn
          )
        }

        QueryType.Update,
        QueryType.LogicDelete -> {
          addUpdateMappedStatement(mapperClass, modelClass, function.name, sqlSource)
        }

      }
    } catch (ex: Exception) {
      LOG.error(
          """
  出错了 >>>>>>>>
    成功解析SQL但注入mybatis失败，失败的SQL为：
  $sql
  
    可能存在下列情形之一：
  ${possibleErrors.joinToString { String.format("\n\t\t-\t%s\n", it) }}""".trimIndent(), ex
      )
    }
  }

}
