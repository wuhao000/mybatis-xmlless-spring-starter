package com.aegis.mybatis.xmlless.resolver

import com.aegis.mybatis.xmlless.annotations.JsonMappingProperty
import com.aegis.mybatis.xmlless.annotations.JsonResult
import com.aegis.mybatis.xmlless.config.MappingResolver
import com.aegis.mybatis.xmlless.model.*
import com.baomidou.mybatisplus.core.metadata.TableInfo
import org.apache.ibatis.builder.MapperBuilderAssistant
import org.apache.ibatis.builder.ResultMapResolver
import org.apache.ibatis.mapping.ResultFlag
import org.apache.ibatis.mapping.ResultMapping
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.StringTypeHandler
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

/**
 *
 * Created by 吴昊 on 2018-12-15.
 *
 * @author 吴昊
 * @since 0.0.8
 */
object ResultMapResolver {

  fun isJsonType(
      modelClass: Class<*>,
      query: Query?,
      function: KFunction<*>?
  ): Boolean {
    if (function != null && function.findAnnotation<JsonResult>() != null) {
      return true
    }
    return (modelClass.isAnnotationPresent(JsonMappingProperty::class.java)
        && query != null && query.properties.includes.size == 1)
  }

  fun resolveResultMap(
      id: String,
      builderAssistant: MapperBuilderAssistant,
      modelClass: Class<*>,
      query: Query? = null,
      optionalProperties: Properties = Properties(),
      function: KFunction<*>? = null
  ): String? {
    if (modelClass.name.startsWith("java.lang") || modelClass.isEnum) {
      return null
    }
    val copyId = id.replace(".", "_")
    if (builderAssistant.configuration.hasResultMap(copyId)) {
      return copyId
    }
    val mappings = MappingResolver.getMappingCache(modelClass)
    val isJsonType = isJsonType(modelClass, query, function)
    val resultMap = ResultMapResolver(
        builderAssistant, copyId,
        if (isJsonType) {
          JsonWrapper::class.java
        } else {
          modelClass
        }, null, null,
        resolveResultMappings(
            mappings, query,
            optionalProperties, builderAssistant, modelClass,
            copyId, function
        ), true
    ).resolve()
    if (!builderAssistant.configuration.hasResultMap(resultMap.id)) {
      builderAssistant.configuration.addResultMap(resultMap)
    }
    return copyId
  }

  private fun buildByMapping(
      id: String, builderAssistant: MapperBuilderAssistant,
      mapping: FieldMapping, tableInfo: TableInfo, modelClass: Class<*>
  ): ResultMapping? {
    if (mapping.selectIgnore) {
      return null
    }
    val builder = ResultMapping.Builder(
        builderAssistant.configuration,
        mapping.property
    )
    if (mapping.property == tableInfo.keyProperty) {
      builder.flags(listOf(ResultFlag.ID))
    }
    if (mapping.joinInfo != null) {
      when (val joinInfo = mapping.joinInfo) {
        is PropertyJoinInfo -> {
          val rawType = joinInfo.rawType()
          if (Collection::class.java.isAssignableFrom(rawType)) {
            builder.column(null)
            builder.javaType(rawType)
            builder.nestedResultMapId(
                createSinglePropertyResultMap(
                    id + "_" + mapping.property, builderAssistant,
                    joinInfo.realType(),
                    joinInfo.propertyColumn.alias
                )
            )
          } else {
            builder.javaType(joinInfo.realType())
            builder.column(joinInfo.propertyColumn.alias)
          }
        }
        is ObjectJoinInfo   -> {
          if (!joinInfo.associationPrefix.isNullOrBlank()) {
            builder.columnPrefix(joinInfo.associationPrefix)
          }
          builder.javaType(joinInfo.rawType())
          val mappedType = joinInfo.realType()
          builder.nestedResultMapId(
              when (mappedType) {
                modelClass -> id
                else       -> resolveResultMap(
                    id + "_" + mapping.property, builderAssistant,
                    mappedType, null, joinInfo.selectProperties
                )
              }
          )
        }
      }
    } else {
      builder.javaType(mapping.tableFieldInfo.propertyType)
      builder.column(mapping.column)
    }
    builder.typeHandler(mapping.typeHandler)
    return builder.build()
  }

  private fun createSinglePropertyResultMap(
      id: String,
      builderAssistant: MapperBuilderAssistant,
      realType: Class<*>,
      column: String
  ): String {
    val copyId = id.replace(".", "_")
    if (builderAssistant.configuration.hasResultMap(copyId)) {
      return copyId
    }
    val resultMap = ResultMapResolver(
        builderAssistant, copyId,
        realType, null, null,
        listOf(
            ResultMapping.Builder(
                builderAssistant.configuration,
                null, column, Object::class.java
            ).build()
        ), true
    ).resolve()

    if (!builderAssistant.configuration.hasResultMap(resultMap.id)) {
      builderAssistant.configuration.addResultMap(resultMap)
    }
    return copyId
  }

  private fun isMappingPropertyInQuery(mapping: FieldMapping, query: Query): Boolean {
    if (query.properties.isIncludeNotEmpty() && mapping.property in query.properties) {
      return true
    }
    if (mapping.joinInfo != null && mapping.joinInfo is ObjectJoinInfo) {
      return query.properties.includes.filter { it.contains('.') && it !in query.properties.excludes }
          .map { it.split('.')[0] }.contains(mapping.property)
    }
    return false
  }

  private fun resolveResultMappings(
      mappings: FieldMappings?,
      query: Query?,
      optionalProperties: Properties,
      builderAssistant: MapperBuilderAssistant,
      modelClass: Class<*>,
      copyId: String,
      function: KFunction<*>?
  ): List<ResultMapping> {
    if (isJsonType(modelClass, query, function)) {
      val column = query!!.mappings.mappings
          .first { it.property == query.properties.includes[0] }.column
      return listOf(
          ResultMapping.Builder(
              builderAssistant.configuration,
              null, column, String::class.java
          ).typeHandler(StringTypeHandler())
              .flags(arrayListOf(ResultFlag.CONSTRUCTOR))
              .jdbcType(JdbcType.VARCHAR)
              .build()
      )
    }
    return mappings?.mappings?.mapNotNull { mapping ->
      when {
        query != null && (query.properties.isIncludeNotEmpty()
            && !isMappingPropertyInQuery(mapping, query))      -> null
        (optionalProperties.isIncludeNotEmpty()
            && mapping.property !in optionalProperties)
            || mapping.property in optionalProperties.excludes -> null
        else                                             -> buildByMapping(
            copyId,
            builderAssistant,
            mapping,
            mappings.tableInfo,
            modelClass
        )
      }
    } ?: listOf()
  }

}
