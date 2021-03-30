@file:Suppress("MemberVisibilityCanBePrivate")

package com.aegis.mybatis.xmlless.resolver

import com.aegis.mybatis.xmlless.annotations.ExcludeProperties
import com.aegis.mybatis.xmlless.annotations.LogicDelete
import com.aegis.mybatis.xmlless.annotations.ResolvedName
import com.aegis.mybatis.xmlless.annotations.SelectedProperties
import com.aegis.mybatis.xmlless.config.MappingResolver
import com.aegis.mybatis.xmlless.config.MybatisXmlLessConfiguration
import com.aegis.mybatis.xmlless.config.paginition.XmlLessPageMapperMethod
import com.aegis.mybatis.xmlless.constant.PAGEABLE_SORT
import com.aegis.mybatis.xmlless.exception.BuildSQLException
import com.aegis.mybatis.xmlless.kotlin.split
import com.aegis.mybatis.xmlless.kotlin.toCamelCase
import com.aegis.mybatis.xmlless.kotlin.toPascalCase
import com.aegis.mybatis.xmlless.kotlin.toWords
import com.aegis.mybatis.xmlless.model.*
import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.toolkit.StringPool.DOT
import com.fasterxml.jackson.databind.JavaType
import org.apache.ibatis.annotations.ResultMap
import org.apache.ibatis.builder.MapperBuilderAssistant
import org.springframework.core.ResolvableType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
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
  private val SPECIAL_NAME_PART = listOf("OrUpdate", "OrUpdateAll")

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

  fun resolve(
      function: KFunction<*>, tableInfo: TableInfo,
      modelClass: Class<*>, mapperClass: Class<*>,
      builderAssistant: MapperBuilderAssistant
  ): ResolvedQuery {
    if (getQueryCache(function, mapperClass) != null) {
      return getQueryCache(function, mapperClass)!!
    }
    try {
      val mappings = MappingResolver.resolve(modelClass, tableInfo, builderAssistant)
      val paramNames = ParameterResolver.resolveNames(function)
      val resolvedNameAnnotation = function.findAnnotation<ResolvedName>()
      val resolvedName = getResolvedName(function)
      val resolveSortsResult = resolveSorts(resolvedName)
      val resolveTypeResult = resolveType(resolveSortsResult.remainName, function)
      val resolvePropertiesResult = resolveProperties(resolveTypeResult.remainWords, function)
      val conditions = CriteriaResolver.resolveConditions(resolvePropertiesResult.conditionWords, function, mappings)
      val query = Query(
          resolveTypeResult.type,
          Properties(
              resolvePropertiesResult.properties, resolvePropertiesResult.excludeProperties,
              resolvePropertiesResult.updateExcludeProperties
          ),
          conditions,
          resolveSortsResult.sorts,
          function,
          mappings,
          null,
          resolvedNameAnnotation
      )
      function.valueParameters.forEachIndexed { index, param ->
        if (Pageable::class.isSuperclassOf(param.type.jvmErasure)) {
          val paramName = paramNames[index]
          query.limitation = Limitation("$paramName.offset", "$paramName.pageSize")
          query.extraSortScript = String.format(PAGEABLE_SORT, paramName, paramName)
        }
      }
      val returnType = resolveReturnType(function.javaMethod!!, mapperClass)
      val resolvedQuery = ResolvedQuery(
          query, resolveResultMap(
          function, query,
          mapperClass, returnType, builderAssistant
      ), returnType, function
      )
      putQueryCache(function, mapperClass, resolvedQuery)
      return resolvedQuery
    } catch (e: Exception) {
      e.printStackTrace()
      return ResolvedQuery(null, null, null, function, e.message)
    }
  }

  fun resolveJavaType(function: Method, clazz: Class<*>, forceSingleValue: Boolean = false): JavaType? {
    return if (!forceSingleValue && Collection::class.java.isAssignableFrom(function.returnType)) {
      val type = ResolvableType.forMethodReturnType(function, clazz).generics[0].resolve()
      toJavaType(type!!)
    } else {
      toJavaType(ResolvableType.forMethodReturnType(function, clazz).resolve()!!)
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
    var excludeProperties = listOf<String>()
    var updateExcludeProperties = listOf<String>()
    // 如果方法指定了要查询或者更新的属性，从方法名称解析的字段无效
    if (function.findAnnotation<SelectedProperties>() != null) {
      properties = function.findAnnotation<SelectedProperties>()!!.properties.toList()
    }
    if (function.findAnnotation<ExcludeProperties>() != null) {
      excludeProperties = function.findAnnotation<ExcludeProperties>()!!.properties.toList()
      updateExcludeProperties = function.findAnnotation<ExcludeProperties>()!!.update.toList()
    }
    return ResolvePropertiesResult(properties, conditionWords, excludeProperties, updateExcludeProperties)
  }

  fun resolveResultMap(
      function: KFunction<*>, query: Query,
      mapperClass: Class<*>, returnType: Class<*>, builderAssistant: MapperBuilderAssistant
  ): String? {
    val resultMap = function.findAnnotation<ResultMap>()?.value?.firstOrNull()
    if (resultMap == null && query.type == QueryType.Select) {
      // 如果没有指定resultMap，则自动生成resultMap
      return ResultMapResolver.resolveResultMap(
          mapperClass.name + DOT + function.name,
          builderAssistant, returnType, query, Properties(), function
      )
    }
    return resultMap
  }

  fun resolveReturnType(function: Method, clazz: Class<*>): Class<*> {
    return if (listOf(
            Collection::class,
            Page::class,
            IPage::class
        ).any { it.java.isAssignableFrom(function.returnType) }
    ) {
      val type = ResolvableType.forMethodReturnType(function, clazz).generics[0].resolve()
      if (type is Class<*>) {
        type
      } else if (type is ParameterizedType) {
        val rawType = type.rawType
        if (rawType is Class<*>) {
          rawType
        } else {
          function.returnType
        }
      } else {
        function.returnType
      }
    } else {
      ResolvableType.forMethodReturnType(function, clazz).resolve()!!
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
        Sort.Order(direction, sortProperty.toCamelCase())
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

  fun resolveType(name: String, function: KFunction<*>): ResolveTypeResult {
    val wordsWithoutSort = name.toWords()
    val typeWord = wordsWithoutSort[0]
    val type: QueryType = when (typeWord) {
      in listOf("Find", "Select", "Query", "Search") -> QueryType.Select
      "Exists"                                       -> QueryType.Exists
      "Count"                                        -> QueryType.Count
      "Update"                                       -> QueryType.Update
      in listOf("Delete", "Remove")                  -> {
        if (function.findAnnotation<LogicDelete>() != null) {
          QueryType.LogicDelete
        } else {
          QueryType.Delete
        }
      }
      in listOf("Insert", "Save", "Add")             -> QueryType.Insert
      else                                           -> null
    } ?: throw BuildSQLException("无法解析SQL类型，解析的名称为$name")
    val remainWords = wordsWithoutSort.drop(1).toMutableList()
    if (remainWords.joinToString("") in SPECIAL_NAME_PART) {
      remainWords.clear()
    }
    return ResolveTypeResult(type, remainWords.toList(), typeWord)
  }

  fun toJavaType(type: Type): JavaType? {
    val typeFactory = XmlLessPageMapperMethod.mapper.typeFactory
    return typeFactory.constructType(type)
  }

  private fun getResolvedName(function: KFunction<*>): String {
    val resolvedNameAnnotation = function.findAnnotation<ResolvedName>()
    return when {
      resolvedNameAnnotation != null -> when {
        resolvedNameAnnotation.name.isBlank() -> ""
        else                                  -> "${resolvedNameAnnotation.name}And"
      } + resolvedNameAnnotation.partNames.joinToString("And") {
        it.toPascalCase()
      }
      else                           -> function.name
    }
  }

  /**
   * 从方法名中解析出的查询属性结果
   * @author 吴昊
   * @since 0.0.2
   */
  data class ResolvePropertiesResult(
      val properties: List<String>,
      val conditionWords: List<String>,
      val excludeProperties: List<String>,
      val updateExcludeProperties: List<String>
  )

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
