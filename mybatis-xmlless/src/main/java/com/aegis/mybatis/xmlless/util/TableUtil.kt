package com.aegis.mybatis.xmlless.util

import com.aegis.mybatis.xmlless.config.MappingResolver
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper
import org.apache.ibatis.builder.MapperBuilderAssistant

/**
 * Created by 吴昊 on 2023/12/8.
 */
fun getTableInfo(clazz: Class<*>, builderAssistant: MapperBuilderAssistant): TableInfo {
  val tableInfo = if (TableInfoHelper.getTableInfo(clazz) == null) {
    TableInfoHelper.initTableInfo(builderAssistant, clazz)
  } else {
    TableInfoHelper.getTableInfo(clazz)
  }
  MappingResolver.fixTableInfo(tableInfo, builderAssistant)
  return tableInfo
}

fun initTableInfo(assistant: MapperBuilderAssistant, clazz: Class<*>): TableInfo {
  return getTableInfo(clazz, assistant)
}
