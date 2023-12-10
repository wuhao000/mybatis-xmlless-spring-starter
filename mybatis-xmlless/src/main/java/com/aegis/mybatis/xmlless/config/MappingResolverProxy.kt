package com.aegis.mybatis.xmlless.config

import com.aegis.kotlin.toUnderlineCase
import com.aegis.mybatis.xmlless.annotations.*
import com.aegis.mybatis.xmlless.model.*
import com.baomidou.mybatisplus.core.metadata.TableResolver
import com.aegis.mybatis.xmlless.util.AnnotationUtil
import com.aegis.mybatis.xmlless.util.getTableInfo
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper
import org.apache.ibatis.builder.MapperBuilderAssistant
import java.lang.reflect.Field
import kotlin.reflect.KClass

class MappingResolverProxy {

  private val mappingCache = hashMapOf<String, FieldMappings>()


  fun getAllMappings(): List<FieldMappings> {
    return mappingCache.values.toList()
  }

  fun getMappingCache(modelClass: Class<*>): FieldMappings? {
    return mappingCache[modelClass.name]
  }

  fun resolveNonEntityClass(
      clazz: Class<*>,
      modelClass: Class<*>,
      tableInfo: TableInfo,
      builderAssistant: MapperBuilderAssistant
  ): FieldMappings {
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

  fun resolve(tableInfo: TableInfo, builderAssistant: MapperBuilderAssistant): FieldMappings {
    val modelClass = tableInfo.entityType
    if (MappingResolver.getMappingCache(modelClass) != null) {
      return MappingResolver.getMappingCache(modelClass)!!
    }
    MappingResolver.fixTableInfo(tableInfo, builderAssistant)
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
      if (joinClass != modelClass) {
        MappingResolver.resolve(joinTableInfo, builderAssistant)
      }
    }
    return joinInfo
  }

  fun resolveFields(modelClass: Class<*>): List<Field> {
    return TableInfoHelper.getAllFields(modelClass)
  }


  private fun resolveJoin(field: Field, builderAssistant: MapperBuilderAssistant): JoinInfo? {
    val joinEntityProperty = AnnotationUtil.resolve<JoinEntityProperty>(field)
    val joinTableColumn = AnnotationUtil.resolve<JoinTableColumn>(field)
    val joinObject = AnnotationUtil.resolve<JoinObject>(field)
    val count = AnnotationUtil.resolve<Count>(field)
    if ((joinEntityProperty != null || joinTableColumn != null) && joinObject != null) {
      throw IllegalStateException("@JoinObject and @JoinEntityProperty or @JoinTableColumn cannot appear on field $field at the same time.")
    }
    return when {
      joinEntityProperty != null -> joinEntityProperty.let {
        PropertyJoinInfo(
            joinEntityProperty.entity.java,
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
            null,
            ColumnName(it.columnMapTo, field.name),
            TableName.resolve(it.table, null),
            it.joinType,
            it.joinOnThisProperty,
            it.joinOnColumn,
            field.genericType
        )
      }

      joinObject != null         -> resolveJoinFromJoinObject(joinObject, field, builderAssistant)
      count != null              -> {
        val targetTableName = TableName.resolve(count.targetTable)
        PropertyJoinInfo(
            null,
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
    val tableInfo = getTableInfo(targetEntity, builderAssistant)
      ?: error("无法解析${targetEntity}对应的表信息")
    return TableName.resolve(
        TableResolver.getTableName(tableInfo),
        joinOnThisProperty
    )
  }

  private fun resolveJoinOnColumn(
      toEntity: JoinEntity,
      builderAssistant: MapperBuilderAssistant
  ): String {
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

  private fun resolveJoinFromJoinObject(
      joinObject: JoinObject,
      field: Field,
      builderAssistant: MapperBuilderAssistant
  ): JoinInfo {
    val targetColumn = resolveJoinOnColumn(joinObject.entity.java, joinObject.joinOnProperty, builderAssistant)
    val targetTableName = resolveJoinTableName(joinObject, field, builderAssistant)
    val selectedProperties = field.getAnnotation(SelectedProperties::class.java)
    return ObjectJoinInfo(
        joinObject.entity.java,
        Properties(
            selectedProperties?.properties?.toList() ?: listOf()
        ),
        targetTableName,
        joinObject.joinType,
        joinObject.joinOnThisProperty,
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
    val tableInfo = getTableInfo(joinObject.entity.java, builderAssistant)
      ?: error("无法解析${joinObject.entity}对应的表信息")
    return TableName.resolve(TableResolver.getTableName(tableInfo), prefix)
  }

}
