package com.aegis.mybatis.xmlless.model

import com.aegis.kotlin.isNotNullAndNotBlank
import com.aegis.mybatis.xmlless.annotations.Handler
import com.aegis.mybatis.xmlless.annotations.JsonMappingProperty
import com.aegis.mybatis.xmlless.annotations.MyBatisIgnore
import com.aegis.mybatis.xmlless.config.TmpHandler
import com.aegis.mybatis.xmlless.constant.PROPERTY_PREFIX
import com.aegis.mybatis.xmlless.constant.PROPERTY_SUFFIX
import com.aegis.mybatis.xmlless.kotlin.toUnderlineCase
import com.aegis.mybatis.xmlless.methods.XmlLessMethods.Companion.HANDLER_PREFIX
import com.aegis.mybatis.xmlless.resolver.QueryResolver
import com.aegis.mybatis.xmlless.util.FieldUtil
import com.baomidou.mybatisplus.annotation.TableLogic
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo
import jakarta.persistence.Transient
import org.apache.ibatis.type.TypeHandler
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.data.annotation.CreatedDate
import java.lang.reflect.Field
import java.lang.reflect.Type
import java.util.*

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
  val column: String = tableFieldInfo.column ?: field.name.toUnderlineCase().lowercase(Locale.getDefault())

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
  val type: Type = field.genericType

  /**  mybatis的字段处理器 */
  val typeHandler: TypeHandler<*>?

  /** 是否更新时忽略 */
  val updateIgnore: Boolean

  /** 是否逻辑删除的标记字段 */
  val isLogicDelFlag = field.isAnnotationPresent(TableLogic::class.java)

  /** 逻辑删除的标记为已删除字段值 */
  val logicDelValue: Any?

  /** 逻辑删除的标记为未删除字段值 */
  val logicNotDelValue: Any?

  init {
    insertIgnore = FieldUtil.isInsertIgnore(field)
    updateIgnore = FieldUtil.isUpdateIgnore(field)
    selectIgnore = FieldUtil.isSelectIgnore(field)
    typeHandler = resolveTypeHandler(field)
    logicDelValue = parseLogicFlagValue(if (isLogicDelFlag) {
      val delValue = field.getAnnotation(TableLogic::class.java)?.delval
      if (delValue.isNotNullAndNotBlank()) {
        delValue
      } else {
        "1"
      }
    } else {
      null
    })
    logicNotDelValue = parseLogicFlagValue(if (isLogicDelFlag) {
      val notDelValue = field.getAnnotation(TableLogic::class.java)?.value
      if (notDelValue.isNotNullAndNotBlank()) {
        notDelValue
      } else {
        "0"
      }
    } else {
      null
    })
  }

  private fun parseLogicFlagValue(value: String?): Any? {
    if (value == null) {
      return null
    }
    if (type == String::class.java) {
      return value
    }
    return value.toInt()
  }

  fun getInsertPropertyExpression(prefix: String? = null): String {
    if (AnnotationUtils.findAnnotation(field, CreatedDate::class.java) != null) {
      val unwrapProperty = getPropertyExpression(prefix, false)
      return """<choose>
      <when test="$unwrapProperty != null">
        ${getPropertyExpression(prefix)}
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
