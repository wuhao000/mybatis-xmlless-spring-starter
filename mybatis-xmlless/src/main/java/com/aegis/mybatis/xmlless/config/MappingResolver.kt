package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.xmlless.model.FieldMappings
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils
import org.apache.ibatis.builder.MapperBuilderAssistant
import java.lang.reflect.Field

/**
 *
 * @param modelClass
 * @return
 */
fun TableInfo.getFieldInfoMap(modelClass: Class<*>): MutableMap<String, TableFieldInfo> {
  val fieldInfoMap = this.fieldList.associateBy {
    it.property
  }.toMutableMap()
  if (!fieldInfoMap.containsKey(this.keyProperty)) {
    MappingResolver.resolveFields(modelClass).firstOrNull { it.name == this.keyProperty }?.also {
      fieldInfoMap[this.keyProperty] = TableFieldInfo(
          GlobalConfigUtils.defaults().dbConfig, this, it
      )
    }
  }
  return fieldInfoMap
}


/**
 *
 * Created by 吴昊 on 2018/12/10.
 *
 * @author 吴昊
 * @since 0.0.2
 */
object MappingResolver {

  private val instance: MappingResolverProxy = MappingResolverProxy()

  fun fixTableInfo(
      modelClass: Class<*>, tableInfo: TableInfo,
      builderAssistant: MapperBuilderAssistant
  ) {
    instance.fixTableInfo(modelClass, tableInfo, builderAssistant)
  }

  fun getAllMappings(): List<FieldMappings> {
    return instance.getAllMappings()
  }

  fun getMappingCache(modelClass: Class<*>): FieldMappings? {
    return instance.getMappingCache(modelClass)
  }

  fun resolve(modelClass: Class<*>, tableInfo: TableInfo, builderAssistant: MapperBuilderAssistant): FieldMappings {
    return instance.resolve(modelClass, tableInfo, builderAssistant)
  }

  fun resolveFields(modelClass: Class<*>): List<Field> {
    return instance.resolveFields(modelClass)
  }

}
