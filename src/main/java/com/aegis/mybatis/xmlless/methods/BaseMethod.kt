package com.aegis.mybatis.xmlless.methods

import com.aegis.mybatis.xmlless.config.MappingResolver
import com.baomidou.mybatisplus.core.metadata.TableInfo
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

  final override fun injectMappedStatement(mapperClass: Class<*>, modelClass: Class<*>, tableInfo: TableInfo):
      MappedStatement? {
    MappingResolver.fixTableInfo(modelClass, tableInfo)
    return innerInject(mapperClass, modelClass, tableInfo)
  }

  abstract fun innerInject(mapperClass: Class<*>, modelClass: Class<*>, tableInfo: TableInfo): MappedStatement?



}
