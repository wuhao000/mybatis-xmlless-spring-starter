package com.aegis.mybatis.xmlless.resolver

import com.aegis.mybatis.xmlless.config.MappingResolver
import com.aegis.mybatis.xmlless.model.FieldMapping
import com.aegis.mybatis.xmlless.model.ObjectJoinInfo
import com.aegis.mybatis.xmlless.model.PropertyJoinInfo
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
                       modelClass: Class<*>): String? {
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
          buildByMapping(copyId, builderAssistant, mapping, mappings.tableInfo, modelClass)
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
          builder.javaType(joinInfo.realType())
          builder.column(joinInfo.propertyColumn.alias)
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
                mappedType
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

}
