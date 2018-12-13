package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.bean.Student
import org.junit.Test

/**
 * Created by 吴昊 on 2018/12/12.
 */
class MappingResolverTest {

  @Test
  fun resolve() {
  }

  @Test
  fun resolvedFields() {
    val fields = MappingResolver.resolvedFields(Student::class.java)
    fields.forEach {
      println(it.name)
    }
  }

}
