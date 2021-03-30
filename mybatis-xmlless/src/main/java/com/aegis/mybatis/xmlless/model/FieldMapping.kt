package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.annotations.*
import com.aegis.mybatis.xmlless.config.TmpHandler
import com.aegis.mybatis.xmlless.constant.PROPERTY_PREFIX
import com.aegis.mybatis.xmlless.constant.PROPERTY_SUFFIX
import com.aegis.mybatis.xmlless.kotlin.toUnderlineCase
import com.aegis.mybatis.xmlless.methods.XmlLessMethods.Companion.HANDLER_PREFIX
import com.aegis.mybatis.xmlless.resolver.QueryResolver
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo
import org.apache.ibatis.type.TypeHandler
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.data.annotation.CreatedDate
import java.lang.reflect.Field
import javax.persistence.GeneratedValue
import javax.persistence.Transient

/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
data class FieldMapping(
    val field: Field,
    val tableFieldInfo: TableFieldInfo,
    val joinInfo: JoinInfo?
) {

  /**  对应数据库表的列名称 */
  val column: String = tableFieldInfo.column ?: field.name.toUnderlineCase().toLowerCase()
  /** 是否插入时忽略 */
  val insertIgnore: Boolean
  /** 是否json对象 */
  val isJsonObject: Boolean = field.isAnnotationPresent(JsonMappingProperty::class.java)
  /** 是否json数组 */
  val isJsonArray: Boolean = isJsonObject && Collection::class.java.isAssignableFrom(field.type)
  /**  持久化类的属性名称 */
  val property: String = field.name
  /** 是否查询时忽略 */
  val selectIgnore: Boolean
  /** 字段类型 */
  val type: Class<*> = field.type
  /**  mybatis的字段处理器 */
  val typeHandler: TypeHandler<*>?
  /** 是否更新时忽略 */
  val updateIgnore: Boolean

  init {
    val transient = field.getDeclaredAnnotation(Transient::class.java)
    insertIgnore = transient != null || AnnotationUtils.findAnnotation(field, InsertIgnore::class.java) != null
        || AnnotationUtils.findAnnotation(field, GeneratedValue::class.java) != null
    updateIgnore = transient != null || AnnotationUtils.findAnnotation(field, UpdateIgnore::class.java) != null
        || AnnotationUtils.findAnnotation(field, CreatedDate::class.java) != null
    selectIgnore = transient != null || AnnotationUtils.findAnnotation(field, SelectIgnore::class.java) != null
    typeHandler = resolveTypeHandler(field)
  }

  fun getInsertPropertyExpression(prefix: String? = null): String {
    if (this.field.isAnnotationPresent(CreatedDate::class.java)) {
      return """<choose>
      <when test="$property != null">
        ${PROPERTY_PREFIX}${property}${PROPERTY_SUFFIX}
      </when>
      <otherwise>
        sysdate()
      </otherwise>
    </choose>""".trimIndent()
    }
    return getPropertyExpression(prefix)
  }

  fun getPropertyExpression(prefix: String? = null, wrap: Boolean = true): String {
    val template = if (wrap) {
      """$PROPERTY_PREFIX%s%s%s$PROPERTY_SUFFIX"""
    } else {
      """%s%s%s"""
    }
    return String.format(
        template, prefix ?: "", property, if (typeHandler != null) {
      ", $HANDLER_PREFIX${typeHandler::class.java.name}"
    } else {
      ""
    }
    )
  }

  private fun resolveTypeHandler(field: Field): TypeHandler<*>? {
    val handlerAnno = field.getDeclaredAnnotation(Handler::class.java)
    if (handlerAnno != null) {
      return handlerAnno.value.java.newInstance()
    }
    if (field.isAnnotationPresent(JsonMappingProperty::class.java)) {
      return TmpHandler(QueryResolver.toJavaType(field.genericType))
    }
    return null
  }

}
