package com.aegis.mybatis.xmlless.methods

import com.baomidou.mybatisplus.core.enums.SqlMethod
import com.baomidou.mybatisplus.core.metadata.TableInfo
import org.apache.ibatis.mapping.MappedStatement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
class QueryPage : BaseMethod() {

  private val sqlMethod = SqlMethod.SELECT_PAGE

  companion object {
    private val LOG: Logger = LoggerFactory.getLogger(QueryPage::class.java)
  }

  override fun getId(): String {
    return sqlMethod.method
  }

  override fun innerInject(
      mapperClass: Class<*>, modelClass: Class<*>,
      tableInfo: TableInfo): MappedStatement {
    val mappings = this.resolveFieldMappings(modelClass, tableInfo)
    val sql = String.format(sqlMethod.sql,
        sqlSelectColumns(modelClass, tableInfo, true),
        mappings.fromDeclaration(),
        this.sqlWhereEntityWrapper(tableInfo))
    val sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass)
    return this.addSelectMappedStatement(mapperClass, sqlMethod.method, sqlSource, modelClass, tableInfo)
  }

  override fun requireKeyColumn(): Boolean {
    return false
  }

}
