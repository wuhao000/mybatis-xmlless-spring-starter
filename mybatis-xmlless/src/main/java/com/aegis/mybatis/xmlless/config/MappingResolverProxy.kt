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
import org.apache.ibatis.builder.MapperBuilderAssistant
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.data.annotation.CreatedDate
import java.lang.reflect.Field
import javax.persistence.*

class MappingResolverProxy {

  private val FIXED_CLASSES = hashSetOf<Class<*>>()
  private val MAPPING_CACHE = hashMapOf<String, FieldMappings>()

  fun fixTableInfo(modelClass: Class<*>, tableInfo: TableInfo, builderAssistant: MapperBuilderAssistant) {
    // 防止重复处理
    if (FIXED_CLASSES.contains(modelClass)) {
      return
    }
    fixTableName(modelClass, tableInfo)
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
    // 使用@Column注解修正字段信息，注意不要覆盖mybatis-plus自带@TableField注解
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
      // 自增主键回填
      if (keyField.isAnnotationPresent(GeneratedValue::class.java)) {
        val field = TableInfo::class.java.getDeclaredField("idType")
        field.isAccessible = true
        field.set(tableInfo, IdType.AUTO)
      }
      if (tableInfo.keyColumn == null || tableInfo.keyProperty == null) {
        val keyColumn = keyField.name.toUnderlineCase().toLowerCase()
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
    val fieldInfoMap = tableInfo.getFieldInfoMap(modelClass)
    val fields = MappingResolver.resolveFields(modelClass).filter {
      it.name in fieldInfoMap
    }
    val mapping = FieldMappings(fields.map { field ->
      val fieldInfo = fieldInfoMap[field.name]!!
      field.annotationIncompatible(Transient::class.java, SelectIgnore::class.java)
      field.annotationIncompatible(Transient::class.java, UpdateIgnore::class.java)
      field.annotationIncompatible(Transient::class.java, CreatedDate::class.java)
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
   * 如果使用了jpa注解@Table，使用@Table注解的表名称
   */
  private fun fixTableName(modelClass: Class<*>, tableInfo: TableInfo) {
    if (modelClass.isAnnotationPresent(Table::class.java)) {
      val table = modelClass.getDeclaredAnnotation(Table::class.java)
      if (table.name.isNotBlank()) {
        // 因为TableInfo的name属性以及schema属性都是通过构造函数初始化的，所以只能通过反射修改
        val field = TableInfo::class.java.getDeclaredField("tableName")
        field.isAccessible = true
        if (table.schema.isNotBlank()) {
          field.set(tableInfo, table.schema + "." + table.name)
        } else {
          field.set(tableInfo, table.name)
        }
      }
    }
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
        val targetTableName = if (it.targetTable.isBlank()) {
          val tableInfo: TableInfo? = TableInfoHelper.getTableInfo(field.type)
          if (tableInfo == null) {
            throw IllegalStateException("无法解析字段${field.name}关联的表")
          } else {
            TableName.resolve(tableInfo.tableName, joinObject.associationPrefix)
          }
        } else {
          TableName.resolve(it.targetTable, joinObject.associationPrefix)
        }
        val targetColumn = if (it.targetColumn.isBlank()) {
          val tableInfo: TableInfo? = TableInfoHelper.getTableInfo(field.type)
          if (tableInfo == null) {
            throw IllegalStateException("无法解析字段${field.name}关联的字段")
          } else {
            tableInfo.keyColumn
          }
        } else {
          it.targetColumn
        }
        ObjectJoinInfo(
            Properties(
                joinObject.selectProperties.toList()
            ),
            targetTableName,
            it.joinType,
            it.joinProperty,
            targetColumn,
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
