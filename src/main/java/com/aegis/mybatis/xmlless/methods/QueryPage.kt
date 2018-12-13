package com.aegis.mybatis.xmlless.methods

import com.aegis.mybatis.xmlless.config.MappingResolver
import com.baomidou.mybatisplus.core.enums.SqlMethod
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.toolkit.Constants
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils
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

  override fun innerInject(
      mapperClass: Class<*>, modelClass: Class<*>,
      tableInfo: TableInfo): MappedStatement {
    val mappings = MappingResolver.resolve(modelClass, tableInfo)
    val sql = String.format(sqlMethod.sql,
        sqlSelectColumns(modelClass, tableInfo, true),
        mappings.fromDeclaration(),
        this.sqlWhereEntityWrapper(tableInfo))
    val sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass)
    return this.addSelectMappedStatement(mapperClass, sqlMethod.method, sqlSource, modelClass, tableInfo)
  }

  /**
   *
   *
   * SQL 查询所有表字段
   *
   *
   * @param table        表信息
   * @param queryWrapper 是否为使用 queryWrapper 查询
   * @return sql 脚本
   */
  private fun sqlSelectColumns(modelClass: Class<*>, table: TableInfo, queryWrapper: Boolean): String {
    val mappings = MappingResolver.resolve(modelClass, table)
    return if (!queryWrapper) {
      mappings.selectFields()
    } else {
      SqlScriptUtils.convertChoose(String.format("%s != null and %s != null",
          Constants.WRAPPER, Constants.Q_WRAPPER_SQL_SELECT),
          SqlScriptUtils.unSafeParam(Constants.Q_WRAPPER_SQL_SELECT), mappings.selectFields())
    }
  }
}
