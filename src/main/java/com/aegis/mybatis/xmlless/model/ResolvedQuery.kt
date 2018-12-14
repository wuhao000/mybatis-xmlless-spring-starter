package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.config.MappingResolver
import com.aegis.mybatis.xmlless.enums.JoinPropertyType
import org.apache.ibatis.builder.MapperBuilderAssistant
import org.apache.ibatis.builder.ResultMapResolver
import org.apache.ibatis.mapping.ResultFlag
import org.apache.ibatis.mapping.ResultMapping
import kotlin.reflect.KFunction


/**
 *
 * Created by 吴昊 on 2018-12-09.
 *
 * @author 吴昊
 * @since 0.0.1
 */
data class ResolvedQuery(
    val query: Query? = null,
    val resultMap: String?,
    /**  sql查询返回的java类型 */
    val returnType: Class<*>?,
    /** 待解析的方法 */
    val function: KFunction<*>,
    var unresolvedReasons: MutableList<String> = arrayListOf()) {

  /**  sql语句 */
  var sql: String?

  init {
    val sqlResult = query?.toSql()
    sql = sqlResult?.sql
    unresolvedReasons.addAll(sqlResult?.reasons?.toMutableList() ?: listOf())
  }

  fun countSql(): String? {
    return query?.toCountSql()?.sql
  }

  fun isValid(): Boolean {
    return query != null && unresolvedReasons.isEmpty()
  }

  fun resolveResultMap(id: String, builderAssistant: MapperBuilderAssistant,
                       modelClass: Class<*>,
                       mappings: FieldMappings?): String {
    if (builderAssistant.configuration.hasResultMap(id)) {
      return id
    }
    val resultMap = ResultMapResolver(builderAssistant, id,
        modelClass,
        null, null,
        mappings?.mappings?.map { mapping ->
          val builder = ResultMapping.Builder(
              builderAssistant.configuration,
              mapping.property
          )
          if (mapping.property == mappings.tableInfo.keyProperty) {
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
                      mappedType, MappingResolver.getMappingCache(mappedType))
              )
            }
          } else {
            builder.javaType(mapping.tableFieldInfo.propertyType)
            builder.column(mapping.column)
          }
          builder.typeHandler(mapping.typeHandler?.newInstance())
          builder.build()
        } ?: listOf(), true).resolve()
    if (!builderAssistant.configuration.hasResultMap(resultMap.id)) {
      builderAssistant.configuration.addResultMap(resultMap)
    }
    return id
  }

  fun type(): QueryType? {
    return query?.type
  }

}
