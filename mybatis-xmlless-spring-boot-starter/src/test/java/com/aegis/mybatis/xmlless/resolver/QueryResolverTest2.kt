package com.aegis.mybatis.xmlless.resolver

import com.aegis.mybatis.dao.StudentDetailDAO
import com.aegis.mybatis.xmlless.config.paginition.XmlLessPageMapperMethod
import org.junit.jupiter.api.Test
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaMethod

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
    val fn = StudentDetailDAO::class.functions
        .first { it.name == "findEducation" }
    val javaType = QueryResolver.resolveJavaType(fn.javaMethod!!)
    val rs = XmlLessPageMapperMethod.mapper.readValue<Any>(
        """[{
          | "school": "南京大学"
          |}]""".trimMargin(), javaType
    )
    println(rs)
  }

}
