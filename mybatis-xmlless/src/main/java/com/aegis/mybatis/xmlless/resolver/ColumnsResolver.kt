package com.aegis.mybatis.xmlless.resolver

import com.aegis.mybatis.xmlless.constant.SQLKeywords
import com.aegis.mybatis.xmlless.model.FieldMappings
import com.aegis.mybatis.xmlless.model.Properties
import com.aegis.mybatis.xmlless.model.SelectColumn
import org.slf4j.LoggerFactory

/**
 * Created by 吴昊 on 2018/12/12.
 */
object ColumnsResolver {

  private val LOG = LoggerFactory.getLogger(ColumnsResolver::class.java)

  /**
   * 构建查询的列
   */
  fun resolve(mappings: FieldMappings, properties: Properties): List<SelectColumn> {
    return resolveColumns(mappings, properties).sortedBy { it.toSql() }
  }

  fun resolveIncludedTables(mappings: FieldMappings, properties: Properties): List<String> {
    return resolveColumns(mappings, properties).mapNotNull {
      it.table
    }
  }

  fun wrapColumn(column: String): String {
    return when {
      column.toUpperCase() in SQLKeywords.getValues() -> "`$column`"
      else                                            -> column
    }
  }

  private fun resolveColumns(mappings: FieldMappings, properties: Properties): List<SelectColumn> {
    if (LOG.isDebugEnabled) {
      LOG.debug("Available properties for class ${mappings.modelClass}: ${mappings.mappings.map { it.property }}")
      LOG.debug("Fetch properties for class ${mappings.modelClass}: $properties")
    }
    return mappings.selectFields(properties)
  }

}
