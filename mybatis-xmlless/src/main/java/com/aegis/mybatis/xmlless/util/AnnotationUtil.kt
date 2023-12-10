package com.aegis.mybatis.xmlless.util

import org.springframework.core.annotation.AnnotationUtils
import java.lang.reflect.AnnotatedElement
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaField

/**
 * Created by 吴昊 on 2018/12/24.
 */
object AnnotationUtil {

  inline fun <reified T : Annotation> resolve(parameter: AnnotatedElement): T? {
    return when (parameter) {
      is KProperty<*> -> resolveFromProperty(parameter)
      else            -> parameter.getAnnotation(T::class.java)
    }
  }

  inline fun <reified T : Annotation> resolveFromProperty(parameter: KProperty<*>): T? {
    return parameter.findAnnotation() ?: parameter.javaField?.getDeclaredAnnotation(T::class.java)
  }

  fun hasAnyAnnotation(el: AnnotatedElement, vararg annotations: Class<out Annotation>): Boolean {
    return annotations.any {
      AnnotationUtils.getAnnotation(
          el, it
      ) != null
    }
  }

}
