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
    val returnType = QueryResolver.resolveReturnType(fn.javaMethod!!, DogDAO::class.java)
    assertEquals(Dog::class.java, javaType?.rawClass)
    assertEquals(Dog::class.java, returnType)
  }

  @Test
  fun resolveReturnType3() {
    val fn = StudentDAO::class.functions
        .first { it.name == "findById" }
    val javaType = QueryResolver.resolveJavaType(fn.javaMethod!!, StudentDAO::class.java)
    assertEquals(Student::class.java, javaType?.rawClass)
  }

  @Test
  fun resolveReturnType4() {
    val fn = DogDAO::class.functions
        .first { it.name == "findAll" }
    val javaType = QueryResolver.resolveJavaType(fn.javaMethod!!, DogDAO::class.java)
    val returnType = QueryResolver.resolveReturnType(fn.javaMethod!!, DogDAO::class.java)
    assertEquals(Dog::class.java, javaType?.rawClass)
    assertEquals(Dog::class.java, returnType)
  }

  @Test
  fun a() {
    val fn = DogDAO::class.functions
        .first { it.name == "findAll" }
    val fn2 = StudentDAO::class.functions
        .first { it.name == "findAll" }
    val r = ResolvableType.forMethodReturnType(fn.javaMethod, DogDAO::class.java).resolve()!!
    val j1 = QueryResolver.resolveJavaType(fn.javaMethod!!, DogDAO::class.java)
    val r2 = ResolvableType.forMethodReturnType(fn2.javaMethod, DogDAO::class.java).resolve()!!
//    val j2 = QueryResolver.resolveJavaType(fn2.javaMethod!!, StudentDAO::class.java)
    println(r)
    println(r2)
    println(j1)

//    println(j2)
  }

  @Test
  fun resolveType() {
    val m = StudentDetailDAO::class.java.methods.first { it.name == "findEducation" }
    val m2 = StudentDetailDAO::class.java.methods.first { it.name == "findDetailById" }
    val type = ResolvableType.forMethodReturnType(m, StudentDetailDAO::class.java).resolve()
    val a = QueryResolver.resolveJavaType(m, StudentDetailDAO::class.java, true)
    println(QueryResolver.toJavaType(ResolvableType.forMethodReturnType(m, StudentDetailDAO::class.java).resolve()))
    println(QueryResolver.toJavaType(m.returnType))
  }

}
