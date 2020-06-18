package com.aegis.mybatis.xmlless.resolver

import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.dao.StudentDetailDAO
import com.aegis.mybatis.xmlless.config.BaseResolverTest
import org.junit.Test

/**
 * TODO
 *
 * @author 吴昊
 * @date 2020/6/18 9:00
 * @since 3.1.2 TODO
 * @version 1.0
 */
class QueryResolverTest : BaseResolverTest(
    Student::class.java, StudentDetailDAO::class.java,
    "findFavorites",
    "findAll"
) {

  @Test
  fun resolveResultMap() {
    getFunctions().forEach {
      val query = QueryResolver.resolve(
          it, tableInfo, modelClass, mapperClass, builderAssistant
      )
      val rmId = query.resultMap
      val rm = builderAssistant.configuration
          .getResultMap(rmId)
      println(query)
      println(rmId)
      println(rm)
    }
  }

}
