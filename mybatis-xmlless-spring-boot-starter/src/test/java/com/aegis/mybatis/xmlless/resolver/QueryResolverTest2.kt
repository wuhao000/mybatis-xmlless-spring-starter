package com.aegis.mybatis.xmlless.resolver

import com.aegis.mybatis.bean.Dog
import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.dao.DogDAO
import com.aegis.mybatis.dao.StudentDAO
import com.aegis.mybatis.dao.StudentDetailDAO
import com.aegis.mybatis.xmlless.config.paginition.XmlLessPageMapperMethod
import org.junit.jupiter.api.Test
import org.springframework.core.ResolvableType
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaMethod
import kotlin.test.assertEquals

/**
 * TODO
 *
 * @author 吴昊
 * @date 2020/6/18 9:00
 * @since 3.1.2 TODO
 * @version 1.0
 */
class QueryResolverTest2 {

  @Test
  fun resolveReturnType() {
    val kclass = StudentDetailDAO::class
    val fn = kclass.functions
        .first { it.name == "findEducation" }
    val javaType = QueryResolver.resolveJavaType(fn.javaMethod!!, kclass.java)
    val rs = XmlLessPageMapperMethod.mapper.readValue<Any>(
        """[{
          | "school": "南京大学"
          |}]""".trimMargin(), javaType
    )
    println(javaType)
    println(rs)
  }

  @Test
  fun resolveReturnType2() {
    val fn = DogDAO::class.functions
        .first { it.name == "findById" }
    val javaType = QueryResolver.resolveJavaType(fn.javaMethod!!, DogDAO::class.java)

    println(QueryResolver.resolveReturnType(fn))
    println(A::class.functions)
    println(B::class.functions)
    assertEquals(Dog::class.java, javaType?.rawClass)

  }

  @Test
  fun resolveReturnType3() {
    val fn = StudentDAO::class.functions
        .first { it.name == "findById" }
    val javaType = QueryResolver.resolveJavaType(fn.javaMethod!!, StudentDAO::class.java)
    assertEquals(Student::class.java, javaType?.rawClass)
  }

}
