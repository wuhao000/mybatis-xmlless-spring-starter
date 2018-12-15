package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.xmlless.exception.BuildSQLException
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
  fun resolve(mappings: FieldMappings, properties: List<String>): String {
    if (LOG.isDebugEnabled) {
      LOG.debug("Available properties for class ${mappings.modelClass}: ${mappings.mappings.map { it.property }}")
      LOG.debug("Fetch properties for class ${mappings.modelClass}: $properties")
    }
    val propertyMap = mappings.tableInfo.fieldInfoMap(
        mappings.modelClass
    )
    properties.forEach { property ->
      if (!property.contains(".") && property !in propertyMap) {
        throw BuildSQLException("无法解析属性$property")
      }
    }
    return when {
      // 指定属性进行查询
      properties.isNotEmpty() -> properties.joinToString(", ") {
        mappings.resolveColumnByPropertyName(it, true)
      }
      // 查询全部属性
      else                    -> mappings.selectFields()
    }
  }

}
