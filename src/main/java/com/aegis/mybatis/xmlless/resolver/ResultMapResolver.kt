package com.aegis.mybatis.xmlless.resolver

import com.aegis.mybatis.xmlless.config.MappingResolver
import com.aegis.mybatis.xmlless.enums.JoinPropertyType
import com.aegis.mybatis.xmlless.model.FieldMapping
import com.aegis.mybatis.xmlless.model.FieldMappings
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
                       modelClass: Class<*>,
                       mappings: FieldMappings?,
                       returnType: Class<*>?): String {
    if (builderAssistant.configuration.hasResultMap(id)) {
      return id
    }
    val resultMap = if (returnType == modelClass) {
      ResultMapResolver(builderAssistant, id,
          modelClass, null, null,
          mappings?.mappings?.map { mapping ->
            buildByMapping(id, builderAssistant, mapping, mappings.tableInfo)
          } ?: listOf(), true).resolve()
    } else {
      ResultMapResolver(builderAssistant, id, returnType, null, null, listOf(), true).resolve()
    }
    if (!builderAssistant.configuration.hasResultMap(resultMap.id)) {
      builderAssistant.configuration.addResultMap(resultMap)
    }
    return id
  }

  private fun buildByMapping(id: String, builderAssistant: MapperBuilderAssistant,
                             mapping: FieldMapping, tableInfo: TableInfo): ResultMapping? {
    val builder = ResultMapping.Builder(
        builderAssistant.configuration,
        mapping.property
    )
    if (mapping.property == tableInfo.keyProperty) {
      builder.flags(listOf(ResultFlag.ID))
    }
    if (mapping.joinInfo != null) {
      if (mapping.joinInfo.joinPropertyType == JoinPropertyType.SingleProperty) {
        builder.javaType(mapping.tableFieldInfo.propertyType)
        builder.column(mapping.joinInfo.selectColumns.first())
      } else if (mapping.joinInfo.joinPropertyType == JoinPropertyType.Object) {
        if (!mapping.joinInfo.associationPrefix.isNullOrBlank()) {
          builder.columnPrefix(mapping.joinInfo.associationPrefix)
        }
        builder.javaType(mapping.joinInfo.rawType())
        val mappedType = mapping.joinInfo.realType()!!
        builder.nestedResultMapId(
            resolveResultMap(id + "_" + mapping.property, builderAssistant,
                mappedType, MappingResolver.getMappingCache(mappedType), null)
        )
      }
    } else {
      builder.javaType(mapping.tableFieldInfo.propertyType)
      builder.column(mapping.column)
    }
    builder.typeHandler(mapping.typeHandler?.newInstance())
    return builder.build()
  }

}
