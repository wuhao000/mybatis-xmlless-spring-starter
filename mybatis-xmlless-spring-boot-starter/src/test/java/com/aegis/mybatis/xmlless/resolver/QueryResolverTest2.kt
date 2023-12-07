package com.aegis.mybatis.xmlless.resolver

import com.aegis.jackson.createObjectMapper
import com.aegis.mybatis.bean.Dog
import com.aegis.mybatis.bean.EducationInfo
import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.dao.DogDAO
import com.aegis.mybatis.dao.StudentDAO
import com.aegis.mybatis.dao.StudentDetailDAO
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

  val objectMapper = createObjectMapper()


  @Test
  fun resolveReturnType() {
    val clazz = StudentDetailDAO::class.java
    val method = StudentDetailDAO::findEducation.javaMethod!!
    val t = ResolvableType.forMethodReturnType(method, clazz)
    val javaType = QueryResolver.resolveJavaType(method, clazz)
    val rs = objectMapper.readValue<Any>(
        """[{
          | "school": "南京大学"
          |}]""".trimMargin(), javaType
    )
    println(javaType)
    assert(rs is List<*>)
    rs as List<*>
    assert(rs.size == 1)
    assert(rs.first() is EducationInfo)
  }

  @Test
  fun resolveReturnType2() {
    val modelClass = Dog::class.java
    val mapperClass = DogDAO::class.java
    val method = DogDAO::class.java.methods
        .first { it.name == "findById" }
    val javaType = QueryResolver.resolveJavaType(method, mapperClass)
    val returnType = QueryResolver.resolveReturnType(method, mapperClass)
    println(ResolvableType.forMethodReturnType(method, mapperClass).type)
    assertEquals(modelClass, javaType?.rawClass)
    assertEquals(modelClass, returnType)
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
    val method = DogDAO::class.java.methods
        .first { it.name == "findAll" }
    val mapperClass = DogDAO::class.java
    val javaType = QueryResolver.resolveJavaType(method, mapperClass)
    val returnType = QueryResolver.resolveReturnType(method, mapperClass)
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
