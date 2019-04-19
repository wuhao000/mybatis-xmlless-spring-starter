package com.aegis.mybatis.xmlless.resolver

import com.aegis.mybatis.xmlless.config.MappingResolver
import com.aegis.mybatis.xmlless.model.FieldMapping
import com.aegis.mybatis.xmlless.model.ObjectJoinInfo
import com.aegis.mybatis.xmlless.model.PropertyJoinInfo
import com.aegis.mybatis.xmlless.model.Query
import com.baomidou.mybatisplus.core.metadata.TableInfo
import org.apache.ibatis.builder.MapperBuilderAssistant
import org.apache.ibatis.builder.ResultMapResolver
import org.apache.ibatis.mapping.ResultFlag
import org.apache.ibatis.mapping.ResultMapping

/**
 *
 * Created by 吴昊 on 2018-12-15.
 *
 * @author 吴昊
 * @since 0.0.8
 */
object ResultMapResolver {

  fun resolveResultMap(id: String, builderAssistant: MapperBuilderAssistant,
                       modelClass: Class<*>, query: Query? = null, optionalProperties: List<String> = listOf()): String? {
    if (modelClass.name.startsWith("java.lang") || modelClass.isEnum) {
      return null
    }
    val copyId = id.replace(".", "_")
    if (builderAssistant.configuration.hasResultMap(copyId)) {
      return copyId
    }
    val mappings = MappingResolver.getMappingCache(modelClass)
    val resultMap = ResultMapResolver(builderAssistant, copyId,
        modelClass, null, null,
        mappings?.mappings?.mapNotNull { mapping ->
          when {
            query != null && query.properties.isNotEmpty()
                && !isMappingPropertyInQuery(mapping, query) -> null
            optionalProperties.isNotEmpty()
                && mapping.property !in optionalProperties   -> null
            else                                             -> buildByMapping(copyId, builderAssistant, mapping, mappings.tableInfo, modelClass)
          }
        } ?: listOf(), true).resolve()
    if (!builderAssistant.configuration.hasResultMap(resultMap.id)) {
      builderAssistant.configuration.addResultMap(resultMap)
    }
    return copyId
  }

  private fun buildByMapping(id: String, builderAssistant: MapperBuilderAssistant,
                             mapping: FieldMapping, tableInfo: TableInfo, modelClass: Class<*>): ResultMapping? {
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
      val joinInfo = mapping.joinInfo
      when (joinInfo) {
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
          builder.nestedResultMapId(when (mappedType) {
            modelClass -> id
            else       -> resolveResultMap(id + "_" + mapping.property, builderAssistant,
                mappedType, null, joinInfo.selectProperties
            )
          })
        }
      }
    } else {
      builder.javaType(mapping.tableFieldInfo.propertyType)
      builder.column(mapping.column)
    }
    builder.typeHandler(mapping.typeHandler?.newInstance())
    return builder.build()
  }

  private fun createSinglePropertyResultMap(id: String, builderAssistant: MapperBuilderAssistant, realType: Class<*>,
                                            column: String): String {
    val copyId = id.replace(".", "_")
    if (builderAssistant.configuration.hasResultMap(copyId)) {
      return copyId
    }
    val resultMap = ResultMapResolver(builderAssistant, copyId,
        realType, null, null,
        listOf(
            ResultMapping.Builder(
                builderAssistant.configuration,
                null, column, Object::class.java
            ).build()
        ), true).resolve()

    if (!builderAssistant.configuration.hasResultMap(resultMap.id)) {
      builderAssistant.configuration.addResultMap(resultMap)
    }
    return copyId
  }

  private fun isMappingPropertyInQuery(mapping: FieldMapping, query: Query): Boolean {
    if (mapping.property in query.properties) {
      return true
    }
    if (mapping.joinInfo != null && mapping.joinInfo is ObjectJoinInfo) {
      return query.properties.filter { it.contains('.') }
          .map { it.split('.')[0] }.contains(mapping.property)
    }
    return false
  }

}
