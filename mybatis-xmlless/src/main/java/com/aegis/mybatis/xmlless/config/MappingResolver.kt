package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.xmlless.model.FieldMappings
import com.baomidou.mybatisplus.core.metadata.TableResolver
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
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
    val fields = MappingResolver.resolveFields(modelClass)

    fields.firstOrNull { it.name == this.keyProperty }?.also {
      fieldInfoMap[this.keyProperty] = TableFieldInfo(
          GlobalConfigUtils.defaults(), this, it,
          this.reflector, TableInfoHelper.isExistTableLogic(modelClass, fields)
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
  private val fixed = mutableListOf<Class<*>>()

  fun fixTableInfo(
      tableInfo: TableInfo, builderAssistant: MapperBuilderAssistant
  ) {
    val modelClass = tableInfo.entityType
    if (fixed.contains(modelClass)) {
      return
    }
    TableResolver.fixTableInfo(modelClass, tableInfo, builderAssistant)
    fixed.add(modelClass)
  }

  fun getAllMappings(): List<FieldMappings> {
    return instance.getAllMappings()
  }

  fun getMappingCache(modelClass: Class<*>): FieldMappings? {
    return instance.getMappingCache(modelClass)
  }

  fun resolveNonEntityClass(
      clazz: Class<*>,
      modelClass: Class<*>,
      tableInfo: TableInfo,
      builderAssistant: MapperBuilderAssistant
  ): FieldMappings {
    return instance.resolveNonEntityClass(clazz, modelClass, tableInfo, builderAssistant)
  }

  fun resolve(tableInfo: TableInfo, builderAssistant: MapperBuilderAssistant): FieldMappings {
    return instance.resolve(tableInfo, builderAssistant)
  }

  fun resolveFields(modelClass: Class<*>): List<Field> {
    return instance.resolveFields(modelClass)
  }

  fun resolveKeyGenerator(modelClass: Class<*>): String? {
    val fields = resolveFields(modelClass)
    val idField = fields.firstOrNull {
      (it.isAnnotationPresent(Id::class.java) || it.isAnnotationPresent(TableId::class.java))
          && it.isAnnotationPresent(GeneratedValue::class.java)
    }
    if (idField != null) {
      val generator = idField.getAnnotation(GeneratedValue::class.java).generator
      if (generator.isNotBlank()) {
        return generator
      }
    }
    return null
  }

}
