package com.aegis.mybatis.xmlless.util

import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper
import org.apache.ibatis.builder.MapperBuilderAssistant

/**
 * Created by 吴昊 on 2023/12/8.
 */
fun getTableInfo(clazz: Class<*>, builderAssistant: MapperBuilderAssistant? = null): TableInfo? {
  if (TableInfoHelper.getTableInfo(clazz) == null && builderAssistant != null) {
    return TableInfoHelper.initTableInfo(builderAssistant, clazz)
  }
  return TableInfoHelper.getTableInfo(clazz)
}
