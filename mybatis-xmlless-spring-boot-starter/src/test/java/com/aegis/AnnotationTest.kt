package com.aegis

import cn.hutool.core.annotation.AnnotationUtil
import com.aegis.mybatis.xmlless.annotations.JsonMappingProperty
import com.fasterxml.jackson.annotation.JsonProperty
import org.junit.jupiter.api.Test
import org.springframework.core.annotation.AnnotationUtils
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod

/**
 * TODO
 *
 * @author 吴昊
 * @date 2023/12/8 13:51
 * @since v0.0.0
 * @version 1.0
 */
class AnnotationTest {

  @Test
  fun getAnno() {
    val a = A()
    find(JsonMappingProperty::class.java)
    find(JsonProperty::class.java)
  }

  fun find(clazz: Class<out Annotation>) {
    println("1 " + AnnotationUtils.getAnnotation(A::a.javaField, clazz))
    println("2 " + AnnotationUtils.getAnnotation(A::b.javaField, clazz))
  }

  class A {

    @JsonMappingProperty
    @field: JsonProperty
    var a: String = ""

    @JsonMappingProperty
    @JsonProperty
    var b: String = ""

  }

}
