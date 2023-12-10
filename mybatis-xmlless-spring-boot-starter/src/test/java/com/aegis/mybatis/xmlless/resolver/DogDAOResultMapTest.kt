package com.aegis.mybatis.xmlless.resolver

import com.aegis.mybatis.bean.Dog
import com.aegis.mybatis.dao.DogDAO
import com.aegis.mybatis.dao.StudentDetailDAO
import com.aegis.mybatis.xmlless.config.BaseResolverTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DogDAOResultMapTest : BaseResolverTest(
    DogDAO::class.java, Dog::class.java,
    "findAll"
) {

  @Test
  fun resolveResultMap() {
    getFunctions().forEach {
      val query = QueryResolver.resolve(
          it, tableInfo, modelClass, mapperClass, builderAssistant
      )
      val rmId = query.resultMap
      val rm = builderAssistant.configuration.getResultMap(rmId)
      println(query)
      println(rmId)
      println(rm.type)
    }
  }

  @Test
  fun resolveReturnType() {
    val fn = StudentDetailDAO::class.functions
        .first { it.name == "findEducation" }
    val type = QueryResolver.resolveReturnType(fn.javaMethod!!, StudentDetailDAO::class.java)
    println(type)
  }

}
