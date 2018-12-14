package com.aegis

import com.aegis.mybatis.bean.Student
import org.junit.Test
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.lang.reflect.ParameterizedType

/**
 * Created by 吴昊 on 2018/12/13.
 */
class SomeTest {

  @Test
  fun test() {
    val pageable = PageRequest.of(0, 20, Sort.by("name"))
    println(pageable.sort
        .get()
        .toArray())
  }

  @Test
  fun test2() {
    Student::class.java.declaredFields.forEach {
      val type = it.genericType
      if (type is Class<*>) {
        println("is class: $type")
      } else if (type is ParameterizedType) {
        println(type.rawType is Class<*>)
        println(type.actualTypeArguments[0] as Class<*>)
      }
    }
  }

}
