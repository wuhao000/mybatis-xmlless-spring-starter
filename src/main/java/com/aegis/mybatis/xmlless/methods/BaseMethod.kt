package com.aegis.mybatis.xmlless.methods

import com.aegis.mybatis.xmlless.config.MappingResolver
import com.aegis.mybatis.xmlless.model.FieldMappings
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.toolkit.Constants
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils
import com.baomidou.mybatisplus.extension.injector.AbstractLogicMethod
import org.apache.ibatis.mapping.MappedStatement

/**
 *
 * Created by 吴昊 on 2018/12/6.
 *
 * @author 吴昊
 * @since 0.0.1
 */
abstract class BaseMethod : AbstractLogicMethod() {

  companion object {
    const val HANDLER_PREFIX = "typeHandler="
    const val PROPERTY_PREFIX = "#{"
    const val PROPERTY_SUFFIX = "}"
  }

  abstract fun getId(): String

  final override fun injectMappedStatement(mapperClass: Class<*>, modelClass: Class<*>, tableInfo: TableInfo):
      MappedStatement? {
    MappingResolver.fixTableInfo(modelClass, tableInfo)
    return innerInject(mapperClass, modelClass, tableInfo)
  }

  abstract fun innerInject(mapperClass: Class<*>, modelClass: Class<*>, tableInfo: TableInfo): MappedStatement?

  abstract fun requireKeyColumn(): Boolean

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
  fun sqlSelectColumns(modelClass: Class<*>, table: TableInfo, queryWrapper: Boolean): String {
    val mappings = this.resolveFieldMappings(modelClass, table)
    return if (!queryWrapper) {
      mappings.selectFields()
    } else {
      SqlScriptUtils.convertChoose(String.format("%s != null and %s != null",
          Constants.WRAPPER, Constants.Q_WRAPPER_SQL_SELECT),
          SqlScriptUtils.unSafeParam(Constants.Q_WRAPPER_SQL_SELECT), mappings.selectFields())
    }
  }

  protected fun resolveFieldMappings(modelClass: Class<*>, tableInfo: TableInfo): FieldMappings {
    return MappingResolver.resolve(modelClass, tableInfo)
  }

}
