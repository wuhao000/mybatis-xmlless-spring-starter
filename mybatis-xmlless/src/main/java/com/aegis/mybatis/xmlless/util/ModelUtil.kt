package com.aegis.mybatis.xmlless.util

import com.aegis.mybatis.xmlless.annotations.JsonMappingProperty
import org.springframework.core.annotation.AnnotationUtils

/**
 * Created by 吴昊 on 2023/12/8.
 */
object ModelUtil {


  fun isJsonMappingClass(clazz: Class<*>): Boolean {
    return AnnotationUtils.getAnnotation(clazz, JsonMappingProperty::class.java) != null
  }

}
