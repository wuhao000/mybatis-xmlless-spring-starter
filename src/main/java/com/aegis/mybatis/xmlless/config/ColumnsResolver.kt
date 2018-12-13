package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.xmlless.model.BuildSqlResult
import com.aegis.mybatis.xmlless.model.FieldMappings
import org.slf4j.LoggerFactory

/**
 * Created by 吴昊 on 2018/12/12.
 */
object ColumnsResolver {

  private val LOG = LoggerFactory.getLogger(ColumnsResolver::class.java)

  /**
   * 构建查询的列
   */
  fun resolve(mappings: FieldMappings, properties: List<String>): BuildSqlResult {
    if (LOG.isDebugEnabled) {
      LOG.debug("Available properties for class ${mappings.modelClass}: ${mappings.mappings.map { it.property }}")
      LOG.debug("Fetch properties for class ${mappings.modelClass}: $properties")
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
