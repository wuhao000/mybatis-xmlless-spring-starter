@file:Suppress("MemberVisibilityCanBePrivate")

package com.aegis.mybatis.xmlless.resolver

import cn.hutool.core.util.ReflectUtil
import com.aegis.kotlin.toCamelCase
import com.aegis.kotlin.toPascalCase
import com.aegis.kotlin.toWords
import com.aegis.mybatis.xmlless.annotations.*
import com.aegis.mybatis.xmlless.config.MappingResolver
import com.aegis.mybatis.xmlless.constant.PAGEABLE_SORT
import com.aegis.mybatis.xmlless.enums.Operations
import com.aegis.mybatis.xmlless.exception.BuildSQLException
import com.aegis.mybatis.xmlless.kotlin.split
import com.aegis.mybatis.xmlless.model.*
import com.aegis.mybatis.xmlless.util.AnnotationUtil
import com.aegis.mybatis.xmlless.util.MethodUtil
import com.aegis.mybatis.xmlless.util.getTableInfo
import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.metadata.TableResolver
import com.baomidou.mybatisplus.core.override.XmlLessPageMapperMethod
import com.baomidou.mybatisplus.core.toolkit.StringPool.DOT
import com.fasterxml.jackson.databind.JavaType
import org.apache.ibatis.annotations.ResultMap
import org.apache.ibatis.builder.MapperBuilderAssistant
import org.slf4j.LoggerFactory
import org.springframework.core.ResolvableType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
object QueryResolver {

  private val log = LoggerFactory.getLogger(QueryResolver::class.java)
  private val QUERY_CACHE = hashMapOf<String, ResolvedQuery>()
  private val SPECIAL_NAME_PART = listOf("OrUpdate", "OrUpdateAll")
  private val MULTIPLE_SPACE_REG = "\\s+".toRegex()

  @Suppress("unused")
  fun getQueryCache(key: String): ResolvedQuery? {
    return QUERY_CACHE[key]
  }

  fun getQueryCache(method: Method, mapperClass: Class<*>): ResolvedQuery? {
    return QUERY_CACHE[mapperClass.name + DOT + method.name]
  }

  fun putQueryCache(method: Method, mapperClass: Class<*>, query: ResolvedQuery) {
    QUERY_CACHE[mapperClass.name + DOT + method.name] = query
  }

  fun resolve(
      method: Method, tableInfo: TableInfo,
      modelClass: Class<*>, mapperClass: Class<*>,
      builderAssistant: MapperBuilderAssistant
  ): ResolvedQuery {
    if (getQueryCache(method, mapperClass) != null) {
      return getQueryCache(method, mapperClass)!!
    }
    try {
      val clazz = resolveReturnType(method, mapperClass)

      val returnMappings = if (clazz != modelClass
          && !MethodUtil.isJsonResult(method)
          && ParameterResolver.isComplexType(clazz)
      ) {
        MappingResolver.resolveNonEntityClass(
            clazz, modelClass, tableInfo, builderAssistant
        )
      } else {
        null
      }
      val modelMappings = MappingResolver.resolve(tableInfo, builderAssistant)
      val methodInfo = MethodInfo(method, modelClass, builderAssistant, returnMappings ?: modelMappings, modelMappings)
      returnMappings?.mappings?.mapNotNull { it.joinInfo }?.forEach {
        if (it is ObjectJoinInfo) {
          TableResolver.fixTableInfo(it.entity, getTableInfo(it.entity, builderAssistant), builderAssistant)
        } else if (it is PropertyJoinInfo && it.entity != null) {
          TableResolver.fixTableInfo(it.entity, getTableInfo(it.entity, builderAssistant), builderAssistant)
        }
      }
      val resolvedNames = getResolvedName(methodInfo)
      val resolveSortsResult = resolveSorts(resolvedNames, methodInfo.resolvedName?.sort?.toList() ?: listOf())
      val resolveTypeResult = resolveType(resolveSortsResult.remainNames.first(), method)
      val resolvePropertiesResult = resolveProperties(resolveTypeResult.remainWords, methodInfo)
      val conditionWordsList = resolveSortsResult.remainNames.drop(1).map { it.toWords() }
      val conditions = getFinalGroupedConditionList(
          resolvePropertiesResult,
          methodInfo,
          resolveTypeResult,
          conditionWordsList,
          returnMappings ?: modelMappings
      )
      val query = Query(
          resolveTypeResult.type,
          Properties(
              resolvePropertiesResult.properties,
              resolvePropertiesResult.excludeProperties,
              resolvePropertiesResult.updateExcludeProperties,
              resolvePropertiesResult.propertiesMapping
          ),
          conditions.filter { it.isNotEmpty() },
          resolveSortsResult.sorts,
          getGroupBy(methodInfo),
          methodInfo
      )
      method.parameters.forEachIndexed { index, param ->
        if (Pageable::class.java.isAssignableFrom(param.type)) {
          val paramName = methodInfo.paramNames[index]
          query.limitation = Limitation("$paramName.offset", "$paramName.pageSize")
          query.extraSortScript = String.format(PAGEABLE_SORT, paramName, paramName)
        }
      }
      val returnType = resolveReturnType(method, mapperClass)
      val resolvedQuery = ResolvedQuery(
          query,
          resolveResultMap(
              method, query,
              mapperClass, returnType, builderAssistant
          ), returnType, method
      )
      putQueryCache(method, mapperClass, resolvedQuery)
      return resolvedQuery
    } catch (e: Exception) {
      log.error("解析方法 ${method.name} 失败", e)
      return ResolvedQuery(null, null, null, method, e.message)
    }
  }

  fun resolveJavaType(method: Method, clazz: Class<*>, forceSingleValue: Boolean = false): JavaType? {
    val type = if (!forceSingleValue && Collection::class.java.isAssignableFrom(method.returnType)) {
      ResolvableType.forMethodReturnType(method, clazz).generics[0]
    } else {
      ResolvableType.forMethodReturnType(method, clazz)
    }
    if (type.type is ParameterizedType) {
      return toJavaType(type.type)
    }
    return toJavaType(type.resolve() ?: type.type)
  }

  /**
   * 解析要查询或者更新的字段
   */
  fun resolveProperties(remainWords: List<String>, methodInfo: MethodInfo): ResolvePropertiesResult {
    val byIndex = remainWords.indexOf("By")
    var properties: List<String> = if (byIndex == 0 || methodInfo.name == "selectOne") {
      listOf()
    } else {
      val propertiesWords = if (byIndex > 0) {
        remainWords.subList(0, byIndex)
      } else {
        remainWords
      }
      propertiesWords.split("And")
          .filter { !(it.size == 1 && (it.first() == "All" || it.first() == "Batch")) }
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
    val method = methodInfo.method
    if (method.getAnnotation(SelectedProperties::class.java) != null) {
      properties = method.getAnnotation(SelectedProperties::class.java)!!.properties.toList()
    }
    if (method.getAnnotation(ExcludeProperties::class.java) != null) {
      excludeProperties = method.getAnnotation(ExcludeProperties::class.java)!!.properties.toList()
      updateExcludeProperties = method.getAnnotation(ExcludeProperties::class.java)!!.update.toList()
    }
    val returnType = resolveJavaType(methodInfo.method, methodInfo.modelClass)
    val propertyMappings = if (returnType != null && returnType != Void.TYPE) {
      ReflectUtil.getFields(returnType.rawClass).mapNotNull {
        it.getAnnotation(PropertyMapping::class.java)
      }.associate { it.property to it.value }
    } else {
      mapOf()
    }
    return ResolvePropertiesResult(
        properties, conditionWords, excludeProperties, updateExcludeProperties,
        propertyMappings
    )
  }

  fun resolveResultMap(
      function: Method, query: Query,
      mapperClass: Class<*>, returnType: Class<*>, builderAssistant: MapperBuilderAssistant
  ): String? {
    val resultMap = function.getAnnotation(ResultMap::class.java)?.value?.firstOrNull()
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
      } else {
        function.returnType
      }
    } else {
      ResolvableType.forMethodReturnType(function, clazz).resolve()!!
    }
  }

  fun getGroupBy(methodInfo: MethodInfo): List<String> {
    return methodInfo.resolvedName?.groupBy?.toList() ?: listOf()
  }

  fun resolveSorts(names: List<String>, sortExpressions: List<String>): ResolveSortsResult {
    val remainNames = mutableListOf<String>()
    val sorts = mutableListOf<Sort.Order>()
    names.forEach { name ->
      val orderByIndex = name.indexOf("OrderBy")
      if (orderByIndex >= 0) {
        val orderByString = name.substring(orderByIndex).replace("OrderBy", "")
        sorts.addAll(resolveSortFromExpression(orderByString))
      }
      val remainName = if (orderByIndex >= 0) {
        name.substring(0, orderByIndex)
      } else {
        name
      }
      if (remainName.isNotBlank()) {
        remainNames.add(remainName)
      }
    }
    sortExpressions.forEach {
      sorts.addAll(resolveSortFromExpression(it))
    }
    return ResolveSortsResult(sorts, remainNames)
  }

  fun resolveType(name: String, method: Method): ResolveTypeResult {
    val wordsWithoutSort = name.toWords()
    val typeWord = wordsWithoutSort[0]
    val type: QueryType = when (typeWord) {
      in listOf("Find", "Select", "Query", "Search") -> QueryType.Select
      "Exists"                                       -> QueryType.Exists
      "Count"                                        -> QueryType.Count
      "Update"                                       -> QueryType.Update
      in listOf("Delete", "Remove")                  -> {
        val logicDelete = AnnotationUtil.hasAnyAnnotation(
            method,
            Deleted::class.java,
            NotDeleted::class.java
        )
        if (logicDelete) {
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

  fun toPascalCaseName(name: String): String {
    return name.split(MULTIPLE_SPACE_REG).joinToString("") { it.toPascalCase() }
  }

  fun resolveSortFromExpression(
      str: String
  ): List<Sort.Order> {
    val orderByString = if (str.startsWith("OrderBy")) {
      str.drop(7)
    } else if (str.replace(MULTIPLE_SPACE_REG, " ").lowercase().startsWith("order by ")) {
      str.replace(MULTIPLE_SPACE_REG, " ").drop("order by ".length)
    } else {
      str
    }
    if (orderByString.contains(" ")) {
      return resolveFromSpaceSplitExpression(orderByString, str)
    }
    val sortStrings = orderByString.split("And").filter { it.isNotBlank() }
    return sortStrings.map {
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
  }

  private fun resolveFromSpaceSplitExpression(
      orderByString: String,
      str: String
  ): List<Sort.Order> {
    val words = orderByString.replace(" and ", " AND ").split(" ")
    return words.split("AND").map {
      if (it.size == 2) {
        if (it[1] in listOf("asc", "ASC")) {
          Sort.Order(Sort.Direction.ASC, it.first())
        } else if (it[1] in listOf("desc", "DESC")) {
          Sort.Order(Sort.Direction.DESC, it.first())
        } else {
          error("从表达式【${str}】中解析排序条件失败")
        }
      } else if (it.size == 1) {
        Sort.Order(Sort.Direction.ASC, it.first())
      } else {
        error("从表达式【${str}】中解析排序条件失败")
      }
    }
  }

  private fun getFinalGroupedConditionList(
      resolvePropertiesResult: ResolvePropertiesResult,
      methodInfo: MethodInfo,
      resolveTypeResult: ResolveTypeResult,
      conditionWordsList: List<List<String>>,
      mappings: FieldMappings
  ): List<List<QueryCriteria>> {
    val result = mutableListOf<List<QueryCriteria>>()
    result += CriteriaResolver.resolveConditions(
        resolvePropertiesResult.conditionWords, methodInfo
    ).map { listOf(it) }
    result += conditionWordsList.map {
      CriteriaResolver.resolveConditions(
          it, methodInfo
      )
    }
    result += CriteriaResolver.createComplexParameterCondition(methodInfo, mappings)
    val logicType = methodInfo.getLogicType()
    if (logicType != null && resolveTypeResult.type == QueryType.Select) {
      val logicDeleteFieldInfo = methodInfo.mappings.tableInfo.logicDeleteFieldInfo
        ?: throw IllegalStateException("缺少逻辑删除字段，请在字段上添加@TableLogic注解")
      result += listOf(
          QueryCriteria(
              logicDeleteFieldInfo.property,
              Operations.Eq,
              parameters = listOf(),
              specificValue = SpecificValue(
                  stringValue = "",
                  nonStringValue = methodInfo.mappings.getLogicDelFlagValue(logicType).toString()
              ),
              methodInfo = methodInfo
          )
      )
    }
    return result
  }

  private fun getResolvedName(methodInfo: MethodInfo): List<String> {
    val resolvedNameAnnotation = methodInfo.resolvedName
    val list = arrayListOf<String>()
    when {
      resolvedNameAnnotation != null -> {
        if (resolvedNameAnnotation.name.isNotBlank()) {
          list.add(toPascalCaseName(resolvedNameAnnotation.name))
        } else {
          list.add(methodInfo.name)
        }
        list.addAll(resolvedNameAnnotation.conditions.map { toPascalCaseName(it) })
      }

      else                           -> list.add(methodInfo.name)
    }
    return list.toList()
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
      val updateExcludeProperties: List<String>,
      val propertiesMapping: Map<String, String>
  )

  /**
   * 从方法名中解析出的排序属性结果
   * @author 吴昊
   * @since 0.0.2
   */
  data class ResolveSortsResult(val sorts: List<Sort.Order>, val remainNames: List<String>)

  /**
   * 从方法名中解析出的sql类型结果
   * @author 吴昊
   * @since 0.0.2
   */
  data class ResolveTypeResult(val type: QueryType, val remainWords: List<String>, val typeWord: String)

}
