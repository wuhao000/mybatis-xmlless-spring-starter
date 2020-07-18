package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.xmlless.annotations.*
import com.aegis.mybatis.xmlless.kotlin.toUnderlineCase
import com.aegis.mybatis.xmlless.model.*
import com.aegis.mybatis.xmlless.resolver.TypeResolver
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils
import org.apache.ibatis.builder.MapperBuilderAssistant
import org.springframework.core.annotation.AnnotationUtils
import java.lang.reflect.Field
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Transient

/**
 *
 * @param modelClass
 * @return
 */
fun TableInfo.fieldInfoMap(modelClass: Class<*>): MutableMap<String, TableFieldInfo> {
  val fieldInfoMap = this.fieldList.associateBy {
    it.property
  }.toMutableMap()
  if (!fieldInfoMap.containsKey(this.keyProperty)) {
    MappingResolver.resolveFields(modelClass).firstOrNull { it.name == this.keyProperty }?.apply {
      fieldInfoMap[this@fieldInfoMap.keyProperty] = TableFieldInfo(
          GlobalConfigUtils.defaults().dbConfig, this@fieldInfoMap, this
      )
    }
  }
  return fieldInfoMap
}

/**
 * 指定的两个注解不能同时出现在同一个field上
 * @param annotation1
 * @param annotation2
 */
private fun Field.annotationIncompatible(annotation1: Class<out Annotation>, annotation2: Class<out Annotation>) {
  if (AnnotationUtils.findAnnotation(this, annotation1) != null && AnnotationUtils.findAnnotation(
          this,
          annotation2
      ) != null
  ) {
    throw IllegalStateException(
        "注解$annotation1 和 $annotation2 不能同时出现在 $this 上"
    )
  }
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

class MappingResolverProxy {

  private val FIXED_CLASSES = hashSetOf<Class<*>>()
  private val MAPPING_CACHE = hashMapOf<String, FieldMappings>()

  fun fixTableInfo(modelClass: Class<*>, tableInfo: TableInfo, builderAssistant: MapperBuilderAssistant) {
    if (FIXED_CLASSES.contains(modelClass)) {
      return
    }
    if (modelClass.isAnnotationPresent(Table::class.java)) {
      val table = modelClass.getDeclaredAnnotation(Table::class.java)
      if (table.name.isNotBlank()) {
        val field = TableInfo::class.java.getDeclaredField("tableName")
        field.isAccessible = true
        if (table.schema.isNotBlank()) {
          field.set(tableInfo, table.schema + "." + table.name)
        } else {
          field.set(tableInfo, table.name)
        }
      }
    }
    val allFields = MappingResolver.resolveFields(modelClass)
    val keyField = if (tableInfo.keyColumn == null || tableInfo.keyProperty == null) {
      allFields.firstOrNull {
        it.isAnnotationPresent(Id::class.java) || it.isAnnotationPresent(TableId::class.java)
      }
    } else {
      allFields.firstOrNull { it.name == tableInfo.keyProperty }
    }
    allFields.filter { it.isAnnotationPresent(JoinObject::class.java) }.forEach {
      val fieldType = TypeResolver.resolveRealType(it.genericType)
      // 防止无限循环
      if (fieldType != modelClass) {
        MappingResolver.fixTableInfo(
            fieldType,
            TableInfoHelper.initTableInfo(builderAssistant, fieldType),
            builderAssistant
        )
      }
    }
    allFields.filter {
      it.isAnnotationPresent(Column::class.java) && !it.isAnnotationPresent(TableField::class.java)
    }.forEach { field ->
      val column = field.getDeclaredAnnotation(Column::class.java)
      if (column.name.isNotBlank()) {
        val tableField = tableInfo.fieldList.first { it.property == field.name }
        val columnField = TableFieldInfo::class.java.getDeclaredField("column")
        columnField.isAccessible = true
        columnField.set(tableField, column.name)
      }
    }
    if (keyField != null) {
      if (keyField.isAnnotationPresent(GeneratedValue::class.java)) {
        val field = TableInfo::class.java.getDeclaredField("idType")
        field.isAccessible = true
        field.set(tableInfo, IdType.AUTO)
      }
      if (tableInfo.keyColumn == null || tableInfo.keyProperty == null) {
        val keyColumn = keyField.name?.toUnderlineCase()?.toLowerCase()
        val field = TableInfo::class.java.getDeclaredField("keyProperty")
        field.isAccessible = true
        field.set(tableInfo, keyField.name)
        val keyColumnField = TableInfo::class.java.getDeclaredField("keyColumn")
        keyColumnField.isAccessible = true
        keyColumnField.set(tableInfo, keyColumn)
      }
    }
    FIXED_CLASSES.add(modelClass)
  }

  fun getAllMappings(): List<FieldMappings> {
    return MAPPING_CACHE.values.toList()
  }

  fun getMappingCache(modelClass: Class<*>): FieldMappings? {
    return MAPPING_CACHE[modelClass.name]
  }

  fun resolve(modelClass: Class<*>, tableInfo: TableInfo, builderAssistant: MapperBuilderAssistant): FieldMappings {
    if (MappingResolver.getMappingCache(modelClass) != null) {
      return MappingResolver.getMappingCache(modelClass)!!
    }
    MappingResolver.fixTableInfo(modelClass, tableInfo, builderAssistant)
    val fieldInfoMap = tableInfo.fieldInfoMap(modelClass)
    val fields = MappingResolver.resolveFields(modelClass).filter {
      it.name in fieldInfoMap
    }
    val mapping = FieldMappings(fields.map { field ->
      val fieldInfo = fieldInfoMap[field.name]!!
      field.annotationIncompatible(Transient::class.java, SelectIgnore::class.java)
      field.annotationIncompatible(Transient::class.java, UpdateIgnore::class.java)
      field.annotationIncompatible(Transient::class.java, InsertIgnore::class.java)
      val joinInfo = resolveJoin(field)
      if (joinInfo is ObjectJoinInfo) {
        val joinClass = joinInfo.realType()
        val joinTableInfo = TableInfoHelper.getTableInfo(joinInfo.realType())
        // 防止无限循环
        if (joinTableInfo != null && joinClass != modelClass) {
          MappingResolver.resolve(joinClass, joinTableInfo, builderAssistant)
        }
      }
      FieldMapping(field, fieldInfo, joinInfo)
    }, tableInfo, modelClass, builderAssistant.configuration.isMapUnderscoreToCamelCase)
    MAPPING_CACHE[modelClass.name] = mapping
    return mapping
  }

  fun resolveFields(modelClass: Class<*>): List<Field> {
    return TableInfoHelper.getAllFields(modelClass)
  }

  private fun resolveJoin(field: Field): JoinInfo? {
    val joinProperty = field.getDeclaredAnnotation(JoinProperty::class.java)
    val joinObject = field.getDeclaredAnnotation(JoinObject::class.java)
    val count = field.getDeclaredAnnotation(Count::class.java)
    if (joinProperty != null && joinObject != null) {
      throw IllegalStateException("@JoinObject and @JoinProperty cannot appear on field $field at the same time.")
    }
    return when {
      joinProperty != null -> joinProperty.let {
        val targetTableName = TableName.resolve(it.targetTable)
        PropertyJoinInfo(
            ColumnName(joinProperty.selectColumn, field.name),
            targetTableName,
            it.joinType,
            it.joinProperty,
            it.targetColumn,
            field.genericType
        )
      }
      joinObject != null   -> joinObject.let {
        val targetTableName = TableName.resolve(it.targetTable, joinObject.associationPrefix)
        ObjectJoinInfo(
            Properties(
                joinObject.selectProperties.toList()
            ),
            targetTableName,
            it.joinType,
            it.joinProperty, it.targetColumn,
            joinObject.associationPrefix,
            field.genericType
        )
      }
      count != null        -> {
        val targetTableName = TableName.resolve(count.targetTable)
        PropertyJoinInfo(
            ColumnName("COUNT(${targetTableName.alias}.${count.countColumn})", field.name),
            targetTableName,
            count.joinType,
            count.joinProperty,
            count.targetColumn,
            field.genericType,
            "${targetTableName.alias}.${count.countColumn}"
        )
      }
      else                 -> null
    }
  }

}
