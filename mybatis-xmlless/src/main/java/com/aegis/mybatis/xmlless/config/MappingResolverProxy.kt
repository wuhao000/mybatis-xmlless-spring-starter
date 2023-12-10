package com.aegis.mybatis.xmlless.config

import com.aegis.kotlin.toUnderlineCase
import com.aegis.mybatis.xmlless.annotations.*
import com.aegis.mybatis.xmlless.model.*
import com.aegis.mybatis.xmlless.model.Properties
import com.aegis.mybatis.xmlless.resolver.TypeResolver
import com.aegis.mybatis.xmlless.util.getTableInfo
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper
import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.apache.ibatis.builder.MapperBuilderAssistant
import java.lang.reflect.Field
import java.util.*
import kotlin.reflect.KClass

class MappingResolverProxy {

  private val fixedClasses = hashSetOf<Class<*>>()
  private val mappingCache = hashMapOf<String, FieldMappings>()

  fun fixTableInfo(modelClass: Class<*>, tableInfo: TableInfo, builderAssistant: MapperBuilderAssistant) {
    // 防止重复处理
    if (fixedClasses.contains(modelClass)) {
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
        val tableField = tableInfo.getFieldInfoMap(modelClass).values.find { it.property == field.name }
          ?: error(
              """can not find table field ${field.name} for table ${
                tableInfo.tableName
              }, optional fields are ${
                tableInfo.fieldList.joinToString(",") { it.property }
              }"""
          )
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
        val keyColumn = keyField.name.toUnderlineCase().lowercase(Locale.getDefault())
        val field = TableInfo::class.java.getDeclaredField("keyProperty")
        field.isAccessible = true
        field.set(tableInfo, keyField.name)
        val keyColumnField = TableInfo::class.java.getDeclaredField("keyColumn")
        keyColumnField.isAccessible = true
        keyColumnField.set(tableInfo, keyColumn)
      }
    }
    fixedClasses.add(modelClass)
  }

  fun getAllMappings(): List<FieldMappings> {
    return mappingCache.values.toList()
  }

  fun getMappingCache(modelClass: Class<*>): FieldMappings? {
    return mappingCache[modelClass.name]
  }

  fun resolveNonEntityClass(clazz: Class<*>,
                            modelClass: Class<*>,
                            tableInfo: TableInfo,
                            builderAssistant: MapperBuilderAssistant): FieldMappings {
    if (MappingResolver.getMappingCache(clazz) != null) {
      return MappingResolver.getMappingCache(clazz)!!
    }
    val currentTableInfo = getTableInfo(clazz, builderAssistant)!!
    val modelFieldInfoMap = tableInfo.getFieldInfoMap(modelClass)
    val currentFieldInfoMap = currentTableInfo.getFieldInfoMap(clazz)
    val fields = MappingResolver.resolveFields(clazz).filter {
      it.name in modelFieldInfoMap || it.name in currentFieldInfoMap
    }
    val mapping = FieldMappings(fields.map { field ->
      val fieldInfo = modelFieldInfoMap[field.name] ?: currentFieldInfoMap[field.name] as TableFieldInfo
      val joinInfo = resolveJoinInfo(fieldInfo, builderAssistant, modelClass)
      FieldMapping(field, fieldInfo, joinInfo)
    }, tableInfo, clazz)
    mappingCache[clazz.name] = mapping
    return mapping
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
      val fieldInfo = fieldInfoMap[field.name] as TableFieldInfo
      val joinInfo = resolveJoinInfo(fieldInfo, builderAssistant, modelClass)
      FieldMapping(field, fieldInfo, joinInfo)
    }, tableInfo, modelClass)
    mappingCache[modelClass.name] = mapping
    return mapping
  }

  private fun resolveJoinInfo(
      fieldInfo: TableFieldInfo,
      builderAssistant: MapperBuilderAssistant,
      modelClass: Class<*>
  ): JoinInfo? {
    val joinInfo = resolveJoin(fieldInfo.field, builderAssistant)
    if (joinInfo is ObjectJoinInfo) {
      val joinClass = joinInfo.realType()
      val joinTableInfo = getTableInfo(joinInfo.realType(), builderAssistant)
      // 防止无限循环
      if (joinTableInfo != null && joinClass != modelClass) {
        MappingResolver.resolve(joinClass, joinTableInfo, builderAssistant)
      }
    }
    return joinInfo
  }

  fun resolveFields(modelClass: Class<*>): List<Field> {
    return TableInfoHelper.getAllFields(modelClass)
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

  private fun resolveJoin(field: Field, builderAssistant: MapperBuilderAssistant): JoinInfo? {
    val joinProperty = field.getDeclaredAnnotation(JoinProperty::class.java)
    val joinEntityProperty = field.getDeclaredAnnotation(JoinEntityProperty::class.java)
    val joinTableColumn = field.getDeclaredAnnotation(JoinTableColumn::class.java)
    val joinObject = field.getDeclaredAnnotation(JoinObject::class.java)
    val count = field.getDeclaredAnnotation(Count::class.java)
    if (joinProperty != null && joinObject != null) {
      throw IllegalStateException("@JoinObject and @JoinProperty cannot appear on field $field at the same time.")
    }
    return when {
      joinEntityProperty != null -> joinEntityProperty.let {
        PropertyJoinInfo(
            ColumnName(
                resolveFromEntity(it.entity, it.propertyMapTo, builderAssistant),
                field.name
            ),
            tableName(it.entity.java, it.joinOnThisProperty.toUnderlineCase().lowercase() + "_", builderAssistant),
            it.joinType,
            it.joinOnThisProperty,
            resolveJoinOnColumn(it.entity.java, joinEntityProperty.joinOnProperty, builderAssistant),
            field.genericType
        )
      }

      joinTableColumn != null    -> joinTableColumn.let {
        PropertyJoinInfo(
            ColumnName(it.columnMapTo, field.name),
            TableName.resolve(it.table, null),
            it.joinType,
            it.joinOnThisProperty,
            it.joinOnColumn,
            field.genericType
        )
      }

      joinProperty != null       -> joinProperty.let {
        PropertyJoinInfo(
            ColumnName(
                resolveSelectedColumn(it, builderAssistant), field.name
            ),
            tableName(it.toEntity, it.toTable, it.joinOnThisProperty.toUnderlineCase() + "_", builderAssistant),
            it.joinType,
            it.joinOnThisProperty,
            resolveJoinOnColumn(it.toEntity, it.toTable, builderAssistant),
            field.genericType
        )
      }

      joinObject != null         -> resolveJoinFromJoinObject(joinObject, field, builderAssistant)
      count != null              -> {
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

      else                       -> null
    }
  }

  private fun tableName(
      toEntity: JoinEntity,
      toTable: JoinTable,
      joinOnThisProperty: String,
      builderAssistant: MapperBuilderAssistant
  ): TableName {
    if (toEntity.targetEntity != Any::class) {
      return tableName(toEntity.targetEntity.java, joinOnThisProperty, builderAssistant)
    }
    return TableName.resolve(toTable.targetTable, joinOnThisProperty)
  }

  private fun tableName(
      targetEntity: Class<*>,
      joinOnThisProperty: String,
      builderAssistant: MapperBuilderAssistant
  ): TableName {
    return TableName.resolve(
        getTableInfo(targetEntity, builderAssistant)?.tableName
          ?: error("无法解析${targetEntity}对应的表信息"), joinOnThisProperty
    )
  }

  private fun resolveJoinOnColumn(
      toEntity: JoinEntity,
      toTable: JoinTable,
      builderAssistant: MapperBuilderAssistant
  ): String {
    if (toEntity.targetEntity == Any::class) {
      return toTable.joinOnColumn
    }
    return resolveJoinOnColumn(toEntity.targetEntity.java, toEntity.joinOnProperty, builderAssistant)
  }

  private fun resolveJoinOnColumn(
      targetEntity: Class<*>,
      joinOnProperty: String,
      builderAssistant: MapperBuilderAssistant
  ): String {
    val joinEntityInfo = getTableInfo(targetEntity, builderAssistant)
      ?: error("无法解析${targetEntity}对应的数据库表信息")
    if (joinOnProperty.isBlank()) {
      return joinEntityInfo.keyColumn
    }
    if (joinOnProperty == joinEntityInfo.keyProperty) {
      return joinEntityInfo.keyColumn
    }
    return joinEntityInfo.fieldList.find { it.property == joinOnProperty }?.column
      ?: error("无法解析${targetEntity}的属性${joinOnProperty}对应的数据库字段")
  }

  private fun resolveFromEntity(
      targetEntity: KClass<*>,
      targetProperty: String,
      builderAssistant: MapperBuilderAssistant
  ): String {
    val joinEntityInfo =
        getTableInfo(targetEntity.java, builderAssistant) ?: error("无法解析${targetEntity}对应的数据库表信息")
    return joinEntityInfo.fieldList.find { it.property == targetProperty }?.column
      ?: error("无法解析${targetEntity}的属性${targetProperty}对应的数据库字段")
  }

  private fun resolveSelectedColumn(joinProperty: JoinProperty, builderAssistant: MapperBuilderAssistant): String {
    if (joinProperty.toEntity.targetEntity == Any::class) {
      return joinProperty.toTable.columnMapTo
    }
    return resolveFromEntity(
        joinProperty.toEntity.targetEntity, joinProperty.toEntity.propertyMapTo, builderAssistant

    )
  }

  private fun resolveJoinFromJoinObject(
      joinObject: JoinObject,
      field: Field,
      builderAssistant: MapperBuilderAssistant
  ): JoinInfo {
    val targetColumn = resolveJoinOnColumn(joinObject.toEntity, joinObject.toTable, builderAssistant)
    val targetTableName = resolveJoinTableName(joinObject, field, builderAssistant)
    val selectedProperties = field.getAnnotation(SelectedProperties::class.java)
    return ObjectJoinInfo(
        Properties(
            selectedProperties?.properties?.toList() ?: listOf()
        ),
        targetTableName,
        joinObject.joinType,
        joinObject.joinOnProperty,
        targetColumn,
        field.name + "_",
        field.genericType
    )
  }

  private fun resolveJoinTableName(
      joinObject: JoinObject,
      field: Field,
      builderAssistant: MapperBuilderAssistant
  ): TableName {
    val prefix = field.name + "_"
    if (joinObject.toEntity.targetEntity != Any::class) {
      val tableInfo = getTableInfo(joinObject.toEntity.targetEntity.java, builderAssistant)
        ?: error("无法解析${joinObject.toEntity.targetEntity}对应的表信息")
      return TableName.resolve(tableInfo.tableName, prefix)
    }
    if (joinObject.toTable.targetTable.isBlank()) {
      val tableInfo = getTableInfo(field.type, builderAssistant) ?: error("无法解析字段${field.name}关联的表")
      return TableName.resolve(tableInfo.tableName, prefix)
    }
    return TableName.resolve(joinObject.toTable.targetTable, prefix)
  }

}
