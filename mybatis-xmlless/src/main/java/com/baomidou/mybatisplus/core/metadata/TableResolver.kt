package com.baomidou.mybatisplus.core.metadata

import com.aegis.kotlin.toUnderlineCase
import com.aegis.mybatis.xmlless.annotations.JoinObject
import com.aegis.mybatis.xmlless.config.MappingResolver
import com.aegis.mybatis.xmlless.config.getFieldInfoMap
import com.aegis.mybatis.xmlless.resolver.TypeResolver
import com.aegis.mybatis.xmlless.util.initTableInfo
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import jakarta.persistence.*
import org.apache.ibatis.builder.MapperBuilderAssistant
import java.lang.reflect.Field
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by 吴昊 on 2023/12/10.
 */
object TableResolver {

  private val fixedClasses = hashSetOf<Class<*>>()

  fun fixTableInfo(modelClass: Class<*>, tableInfo: TableInfo, builderAssistant: MapperBuilderAssistant) {
    // 防止重复处理
    if (fixedClasses.contains(modelClass)) {
      return
    }
    fixTableName(modelClass, tableInfo)
    val allFields = MappingResolver.resolveFields(modelClass)
    fixFields(allFields, modelClass, builderAssistant, tableInfo)
    val keyField = if (tableInfo.keyColumn == null || tableInfo.keyProperty == null) {
      allFields.firstOrNull {
        it.isAnnotationPresent(Id::class.java) || it.isAnnotationPresent(TableId::class.java)
      }
    } else {
      allFields.firstOrNull { it.name == tableInfo.keyProperty }
    }
    if (keyField != null) {
      // 自增主键回填
      if (keyField.isAnnotationPresent(GeneratedValue::class.java)
          && keyField.getAnnotation(GeneratedValue::class.java).strategy == GenerationType.AUTO
      ) {
        tableInfo.idType = IdType.AUTO
      }
      if (tableInfo.keyColumn == null || tableInfo.keyProperty == null) {
        val keyColumn = keyField.name.toUnderlineCase().lowercase(Locale.getDefault())
        tableInfo.keyProperty = keyField.name
        tableInfo.keyColumn = keyColumn
      }
    }
    fixedClasses.add(modelClass)
  }

  private fun fixFields(
      allFields: List<Field>,
      modelClass: Class<*>,
      builderAssistant: MapperBuilderAssistant,
      tableInfo: TableInfo
  ) {
    allFields.filter { it.isAnnotationPresent(JoinObject::class.java) }.forEach {
      val fieldType = TypeResolver.resolveRealType(it.genericType)
      // 防止无限循环
      if (fieldType != modelClass) {
        MappingResolver.fixTableInfo(
            initTableInfo(builderAssistant, fieldType),
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
                getTableName(tableInfo)
              }, optional fields are ${
                tableInfo.fieldList.joinToString(",") { it.property }
              }"""
          )
        val columnField = TableFieldInfo::class.java.getDeclaredField("column")
        columnField.isAccessible = true
        columnField.set(tableField, column.name)
      }
    }
  }


  /**
   * 如果使用了jpa注解@Table，使用@Table注解的表名称
   */
  private fun fixTableName(modelClass: Class<*>, tableInfo: TableInfo) {
    if (modelClass.isAnnotationPresent(Table::class.java)) {
      val table = modelClass.getDeclaredAnnotation(Table::class.java)
      if (table.name.isNotBlank()) {
        tableInfo.tableName = table.name
        if (table.schema.isNotBlank()) {
          tableInfo.tableName = table.schema + "." + table.name
        } else {
          tableInfo.tableName = table.name
        }
      }
    }
  }

  private val tableNameMap = ConcurrentHashMap<Class<*>, String>()

  fun getTableName(tableInfo: TableInfo): String {
    if (tableNameMap.containsKey(tableInfo.entityType)) {
      return tableNameMap[tableInfo.entityType]!!
    }
    if (tableInfo.entityType.isAnnotationPresent(Table::class.java)) {
      val table = tableInfo.entityType.getDeclaredAnnotation(Table::class.java)
      if (table.name.isNotBlank()) {
        tableNameMap[tableInfo.entityType] = table.name
        return table.name
      }
    }
    return tableInfo.tableName
  }

}
