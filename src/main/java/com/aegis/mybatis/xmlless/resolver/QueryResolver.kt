@file:Suppress("MemberVisibilityCanBePrivate")

package com.aegis.mybatis.xmlless.resolver

import com.aegis.mybatis.xmlless.annotations.ResolvedName
import com.aegis.mybatis.xmlless.annotations.SelectedProperties
import com.aegis.mybatis.xmlless.config.MappingResolver
import com.aegis.mybatis.xmlless.constant.PAGEABLE_SORT
import com.aegis.mybatis.xmlless.exception.BuildSQLException
import com.aegis.mybatis.xmlless.kotlin.split
import com.aegis.mybatis.xmlless.kotlin.toCamelCase
import com.aegis.mybatis.xmlless.kotlin.toWords
import com.aegis.mybatis.xmlless.model.*
import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.toolkit.StringPool
import com.baomidou.mybatisplus.core.toolkit.StringPool.DOT
import org.apache.ibatis.annotations.ResultMap
import org.apache.ibatis.builder.MapperBuilderAssistant
import org.apache.ibatis.reflection.ParamNameResolver
import org.apache.ibatis.session.Configuration
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure

/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
object QueryResolver {

  private val QUERY_CACHE = hashMapOf<String, ResolvedQuery>()

  @Suppress("unused")
  fun getQueryCache(key: String): ResolvedQuery? {
    return QUERY_CACHE[key]
  }

  fun getQueryCache(function: KFunction<*>, mapperClass: Class<*>): ResolvedQuery? {
    return QUERY_CACHE[mapperClass.name + DOT + function.name]
  }

  fun putQueryCache(function: KFunction<*>, mapperClass: Class<*>, query: ResolvedQuery) {
    QUERY_CACHE[mapperClass.name + DOT + function.name] = query
  }

  fun resolve(function: KFunction<*>, tableInfo: TableInfo,
              modelClass: Class<*>, mapperClass: Class<*>,
              builderAssistant: MapperBuilderAssistant): ResolvedQuery {
    if (getQueryCache(function, mapperClass) != null) {
      return getQueryCache(function, mapperClass)!!
    }
    try {
      val mappings = MappingResolver.resolve(modelClass, tableInfo, builderAssistant)
      val paramNames = ParamNameResolver(
          Configuration().apply {
            this.isUseActualParamName = true
          }, function.javaMethod
      ).names
      val resolvedNameAnnotation = function.findAnnotation<ResolvedName>()
      val resolvedName = resolvedNameAnnotation?.name ?: function.name
      val resolveSortsResult = resolveSorts(resolvedName)
      val resolveTypeResult = resolveType(resolveSortsResult.remainName)
      val resolvePropertiesResult = resolveProperties(resolveTypeResult.remainWords, function)
      val conditions = ConditionResolver.resolveConditions(resolvePropertiesResult.conditionWords, function, paramNames)
      val query = Query(
          resolveTypeResult.type,
          resolvePropertiesResult.properties,
          conditions,
          resolveSortsResult.sorts,
          function,
          mappings,
          null,
          resolvedNameAnnotation
      )
      function.valueParameters.forEachIndexed { index, param ->
        val paramName = paramNames[index]
        if (Pageable::class.isSuperclassOf(param.type.jvmErasure)) {
          query.limitation = Limitation("$paramName.offset", "$paramName.pageSize")
          query.extraSortScript = String.format(PAGEABLE_SORT, paramName, paramName)
        }
      }
      val returnType = resolveReturnType(function)
      val resolvedQuery = ResolvedQuery(
          query, resolveResultMap(function, query.type,
          mapperClass, query.mappings, returnType,
          builderAssistant), returnType, function
      )
      putQueryCache(function, mapperClass, resolvedQuery)
      return resolvedQuery
    } catch (e: Exception) {
      if (e !is BuildSQLException) {
        e.printStackTrace()
      }
      return ResolvedQuery(null, null, null, function, e.message)
    }
  }

  /**
   * 解析要查询或者更新的字段
   */
  fun resolveProperties(remainWords: List<String>, function: KFunction<*>): ResolvePropertiesResult {
    val byIndex = remainWords.indexOf("By")
    var properties: List<String> = if (byIndex == 0) {
      listOf()
    } else {
      val propertiesWords = if (byIndex > 0) {
        remainWords.subList(0, byIndex)
      } else {
        remainWords
      }
      propertiesWords.split("And")
          .filter { !(it.size == 1 && it.first() == "All") }
          .map { it.joinToString("").toCamelCase() }
    }
    val conditionWords = if (byIndex >= 0) {
      remainWords.subList(byIndex + 1, remainWords.size)
    } else {
      listOf()
    }
    // 如果方法指定了要查询或者更新的属性，从方法名称解析的字段无效
    if (function.findAnnotation<SelectedProperties>() != null) {
      properties = function.findAnnotation<SelectedProperties>()!!.properties.toList()
    }
    return ResolvePropertiesResult(properties, conditionWords)
  }

  fun resolveResultMap(function: KFunction<*>, type: QueryType,
                       mapperClass: Class<*>, mappings: FieldMappings, returnType: Class<*>, builderAssistant: MapperBuilderAssistant): String? {
    var resultMap = function.findAnnotation<ResultMap>()?.value?.firstOrNull()
    if (resultMap == null && type == QueryType.Select) {
      // 如果没有指定resultMap，则自动生成resultMap
      resultMap = ResultMapResolver.resolveResultMap(mapperClass.name + StringPool.DOT + function.name,
          builderAssistant, returnType, mappings)
    }
    return resultMap
  }

  fun resolveReturnType(function: KFunction<*>): Class<*> {
    return if (listOf(Collection::class, Page::class, IPage::class)
            .any { it.java.isAssignableFrom(function.javaMethod!!.returnType) }) {
      val type = (function.javaMethod!!.genericReturnType as ParameterizedType).actualTypeArguments[0]
      if (type is Class<*>) {
        type
      } else {
        function.javaMethod!!.returnType
      }
    } else {
      function.javaMethod!!.returnType
    }
  }

  fun resolveSorts(name: String): ResolveSortsResult {
    val orderByIndex = name.indexOf("OrderBy")
    val sorts = if (orderByIndex > 0) {
      val orderByString = name.substring(orderByIndex).replace("OrderBy", "")
      val sortStrings = orderByString.split("And")
      sortStrings.map {
        var direction = Sort.Direction.ASC
        val sortProperty = when {
          it.endsWith("Desc") -> {
            direction = Sort.Direction.DESC
            it.substring(0, it.length - 4)
          }
          it.endsWith("Asc")  -> it.substring(0, it.length - 3)
          else                -> it
        }
        Sort.Order(direction, sortProperty)
      }
    } else {
      listOf()
    }
    val remainName = if (orderByIndex > 0) {
      name.substring(0, orderByIndex)
    } else {
      name
    }
    return ResolveSortsResult(sorts, remainName)
  }

  fun resolveType(name: String): ResolveTypeResult {
    val wordsWithoutSort = name.toWords()
    val typeWord = wordsWithoutSort[0]
    val type: QueryType = when (typeWord) {
      in listOf("Find", "Select", "Query") -> QueryType.Select
      "Exists"                             -> QueryType.Exists
      "Count"                              -> QueryType.Count
      "Update"                             -> QueryType.Update
      in listOf("Delete", "Remove")        -> QueryType.Delete
      in listOf("Insert", "Save")          -> QueryType.Insert
      else                                 -> null
    } ?: throw BuildSQLException("无法解析SQL类型，解析的名称为$name")
    val remainWords = wordsWithoutSort.drop(1)
    return ResolveTypeResult(type, remainWords, typeWord)
  }

  /**
   * 从方法名中解析出的查询属性结果
   * @author 吴昊
   * @since 0.0.2
   */
  data class ResolvePropertiesResult(val properties: List<String>, val conditionWords: List<String>)

  /**
   * 从方法名中解析出的排序属性结果
   * @author 吴昊
   * @since 0.0.2
   */
  data class ResolveSortsResult(val sorts: List<Sort.Order>, val remainName: String)

  /**
   * 从方法名中解析出的sql类型结果
   * @author 吴昊
   * @since 0.0.2
   */
  data class ResolveTypeResult(val type: QueryType, val remainWords: List<String>, val typeWord: String)

}
