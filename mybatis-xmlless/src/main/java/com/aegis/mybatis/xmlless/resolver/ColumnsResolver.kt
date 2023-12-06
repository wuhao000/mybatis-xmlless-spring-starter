package com.aegis.mybatis.xmlless.resolver

import com.aegis.mybatis.xmlless.constant.SQLKeywords
import com.aegis.mybatis.xmlless.model.FieldMappings
import com.aegis.mybatis.xmlless.model.Properties
import com.aegis.mybatis.xmlless.model.SelectColumn
import org.slf4j.LoggerFactory
import java.lang.reflect.Method
import java.util.*

/**
 * Created by 吴昊 on 2018/12/12.
 */
object ColumnsResolver {

  private val LOG = LoggerFactory.getLogger(ColumnsResolver::class.java)

  /**
   * 构建查询的列
   */
  fun resolve(mappings: FieldMappings, properties: Properties, method: Method? = null): List<SelectColumn> {
    return resolveColumns(mappings, properties, method).sortedBy { it.toSql() }
  }

  fun resolveIncludedTables(mappings: FieldMappings, properties: Properties, method: Method): List<String> {
    return resolveColumns(mappings, properties, method).mapNotNull {
      it.table
    }.map { it.getAliasOrName() }
  }

  fun wrapColumn(column: String): String {
    return when {
      column.uppercase(Locale.getDefault()) in SQLKeywords.getValues() -> "`$column`"
      else                                                             -> column
    }
  }

  private fun resolveColumns(mappings: FieldMappings, properties: Properties, method: Method?): List<SelectColumn> {
    if (LOG.isDebugEnabled) {
      LOG.debug("Available properties for class ${mappings.modelClass}: ${mappings.mappings.map { it.property }}")
      LOG.debug("Fetch properties for class ${mappings.modelClass}: $properties")
    }
    return mappings.selectFields(properties, method)
  }

}
