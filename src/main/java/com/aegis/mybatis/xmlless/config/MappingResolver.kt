package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.xmlless.annotations.*
import com.aegis.mybatis.xmlless.enums.JoinPropertyType
import com.aegis.mybatis.xmlless.kotlin.toUnderlineCase
import com.aegis.mybatis.xmlless.model.FieldMapping
import com.aegis.mybatis.xmlless.model.FieldMappings
import com.aegis.mybatis.xmlless.model.JoinInfo
import com.aegis.mybatis.xmlless.model.TableName
import com.aegis.mybatis.xmlless.resolver.TypeResolver
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo
import com.baomidou.mybatisplus.core.metadata.TableInfo
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils
import com.baomidou.mybatisplus.core.toolkit.TableInfoHelper
import org.apache.ibatis.builder.MapperBuilderAssistant
import org.springframework.core.annotation.AnnotationUtils
import java.lang.reflect.Field
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
  if (AnnotationUtils.findAnnotation(this, annotation1) != null && AnnotationUtils.findAnnotation(this, annotation2) != null) {
    throw IllegalStateException(
        "Annotation $annotation1 and $annotation2 cannot be both present on field $this"
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

  private val FIXED_CLASSES = hashSetOf<Class<*>>()
  private val MAPPING_CACHE = hashMapOf<String, FieldMappings>()

  fun fixTableInfo(modelClass: Class<*>, tableInfo: TableInfo, builderAssistant: MapperBuilderAssistant? = null) {
    if (FIXED_CLASSES.contains(modelClass)) {
      return
    }
    if (modelClass.isAnnotationPresent(Table::class.java)) {
      val table = modelClass.getDeclaredAnnotation(Table::class.java)
      if (table.name.isNotBlank()) {
        if (table.schema.isNotBlank()) {
          tableInfo.tableName = table.schema + "." + table.name
        } else {
          tableInfo.tableName = table.name
        }
      }
    }
    val allFields = resolveFields(modelClass)
    val keyField = if (tableInfo.keyColumn == null || tableInfo.keyProperty == null) {
      allFields.firstOrNull {
        it.isAnnotationPresent(Id::class.java) || it.isAnnotationPresent(TableId::class.java)
      }
    } else {
      allFields.firstOrNull { it.name == tableInfo.keyProperty }
    }
    if (builderAssistant != null) {
      allFields.filter { it.isAnnotationPresent(JoinObject::class.java) }.forEach {
        val fieldType = TypeResolver.resolveRealType(it.genericType)
        if (fieldType != null) {
          fixTableInfo(fieldType, TableInfoHelper.initTableInfo(builderAssistant, fieldType), builderAssistant)
        }
      }
    }
    if (keyField != null) {
      if (keyField.isAnnotationPresent(GeneratedValue::class.java)) {
        tableInfo.idType = IdType.AUTO
      }
      if (tableInfo.keyColumn == null || tableInfo.keyProperty == null) {
        val keyColumn = keyField.name?.toUnderlineCase()?.toLowerCase()
        tableInfo.keyProperty = keyField.name
        tableInfo.keyColumn = keyColumn
      }
    }
    FIXED_CLASSES.add(modelClass)
  }

  fun getMappingCache(modelClass: Class<*>): FieldMappings? {
    return MAPPING_CACHE[modelClass.name]
  }

  fun resolve(modelClass: Class<*>, tableInfo: TableInfo): FieldMappings {
    if (getMappingCache(modelClass) != null) {
      return getMappingCache(modelClass)!!
    }
    fixTableInfo(modelClass, tableInfo)
    val fieldInfoMap = tableInfo.fieldInfoMap(modelClass)
    val fields = resolveFields(modelClass).filter {
      it.name in fieldInfoMap
    }
    val mapping = FieldMappings(fields.map { field ->
      val transient = field.getDeclaredAnnotation(Transient::class.java)
      val fieldInfo = fieldInfoMap[field.name]!!
      field.annotationIncompatible(Transient::class.java, SelectIgnore::class.java)
      field.annotationIncompatible(Transient::class.java, UpdateIgnore::class.java)
      field.annotationIncompatible(Transient::class.java, InsertIgnore::class.java)
      FieldMapping(field.name,
          fieldInfo.column ?: field.name.toUnderlineCase().toLowerCase(),
          field.getDeclaredAnnotation(Handler::class.java)?.value?.java,
          fieldInfo,
          transient != null || AnnotationUtils.findAnnotation(field, InsertIgnore::class.java) != null
              || AnnotationUtils.findAnnotation(field, GeneratedValue::class.java) != null,
          transient != null || AnnotationUtils.findAnnotation(field, UpdateIgnore::class.java) != null,
          transient != null || AnnotationUtils.findAnnotation(field, SelectIgnore::class.java) != null,
          resolveJoin(field)
      )
    }, tableInfo, modelClass,
        modelClass.getDeclaredAnnotation(SelectedProperties::class.java)?.properties,
        modelClass.getDeclaredAnnotation(IgnoredProperties::class.java)?.properties
    )
    MAPPING_CACHE[modelClass.name] = mapping
    return mapping
  }

  fun resolveFields(modelClass: Class<*>): List<Field> {
    return TableInfoHelper.getAllFields(modelClass)
  }

  private fun resolveJoin(field: Field): JoinInfo? {
    val joinProperty = field.getDeclaredAnnotation(JoinProperty::class.java)
    val joinObject = field.getDeclaredAnnotation(JoinObject::class.java)
    if (joinProperty != null && joinObject != null) {
      throw IllegalStateException("@JoinObject and @JoinProperty cannot appear on field $field at the same time.")
    }
    return when {
      joinProperty != null -> joinProperty.let {
        val targetTableName = resolveNameAndAlias(it.targetTable)
        JoinInfo(listOf(resolveJoinColumn(joinProperty)),
            targetTableName.name,
            targetTableName.alias,
            it.joinType,
            it.joinProperty,
            it.targetColumn,
            JoinPropertyType.SingleProperty)
      }
      joinObject != null   -> joinObject.let {
        val targetTableName = resolveNameAndAlias(it.targetTable)
        JoinInfo(resolveJoinColumns(joinObject, field),
            targetTableName.name,
            targetTableName.alias,
            it.joinType, it.joinProperty, it.targetColumn, JoinPropertyType.Object
        ).apply {
          associationPrefix = joinObject.associationPrefix
          javaType = field.genericType
        }
      }
      else                 -> null
    }
  }

  private fun resolveJoinColumn(join: JoinProperty): String {
    return join.selectColumn
  }

  private fun resolveJoinColumns(join: JoinObject, field: Field): List<String> {
    return if (join.selectColumns.isNotEmpty()) {
      join.selectColumns.toList().map {
        if (it.toUpperCase().contains(" AS ") || join.associationPrefix.isBlank()) {
          it
        } else {
          it + " AS " + join.associationPrefix + it
        }
      }
    } else {
      TableInfoHelper.getAllFields(field.type).filter {
        !it.isAnnotationPresent(SelectIgnore::class.java)
            && !it.isAnnotationPresent(Transient::class.java)
            && !it.isAnnotationPresent(JoinObject::class.java)
            && !it.isAnnotationPresent(JoinProperty::class.java)
      }.map {
        val columnName = it.name.toUnderlineCase().toLowerCase()
        columnName + " AS " + join.associationPrefix + columnName
      }
    }
  }

  private fun resolveNameAndAlias(targetTable: String): TableName {
    val targetTableSplits = targetTable.split("\\s+".toRegex())
    val tableName = targetTableSplits[0]
    val alias = when (targetTableSplits.size) {
      3    -> {
        if (targetTableSplits[1].toLowerCase() != "as") {
          throw IllegalStateException("Unexpect join table name expression: $targetTable")
        }
        targetTableSplits[2]
      }
      2    -> targetTableSplits[1]
      1    -> null
      else -> throw IllegalStateException("Unexpect join table name expression: $targetTable")
    }
    return TableName(tableName, alias)
  }

}
