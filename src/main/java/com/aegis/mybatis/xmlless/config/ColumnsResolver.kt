package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.xmlless.model.BuildSqlResult
import com.aegis.mybatis.xmlless.model.FieldMappings
import org.slf4j.LoggerFactory

/**
 * Created by 吴昊 on 2018/12/12.
 */
object ColumnsResolver {

  private val log = LoggerFactory.getLogger(ColumnsResolver::class.java)

  fun resolve(mappings: FieldMappings, properties: List<String>, tableName: String): BuildSqlResult {
    if (log.isDebugEnabled) {
      log.debug("Available properties for class ${mappings.modelClass}: ${mappings.mappings.map { it.property }}")
      log.debug("Fetch properties for class ${mappings.modelClass}: $properties")
    }
    val propertyMap = mappings.tableInfo.fieldInfoMap(
        mappings.modelClass
    )
    properties.forEach { property ->
      if (!property.contains(".") && property !in propertyMap) {
        return BuildSqlResult(null, listOf("Cannot recognize property $property"))
      }
    }
    return BuildSqlResult(
        if (properties.isNotEmpty()) {
          properties.map {
            mappings.resolveColumnByPropertyName(it, true)
          }.joinToString(", ") { it.sql!! }
        } else {
          mappings.selectFields()
        }, listOf()
    )
  }

}
