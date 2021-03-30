package com.aegis.mybatis.xmlless.resolver

import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaField

/**
 * Created by 吴昊 on 2018/12/24.
 */
object AnnotationResolver {

  inline fun <reified T : Annotation> resolve(parameter: KAnnotatedElement): T? {
    return when (parameter) {
      is KProperty<*> -> resolveFromProperty(parameter)
      else          -> parameter.findAnnotation()
    }
  }

  inline fun <reified T : Annotation> resolveFromProperty(parameter: KProperty<*>): T? {
    return parameter.findAnnotation() ?: parameter.javaField?.getDeclaredAnnotation(T::class.java)
  }

}
