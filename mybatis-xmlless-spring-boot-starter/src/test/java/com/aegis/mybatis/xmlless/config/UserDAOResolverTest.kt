package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.bean.User
import com.aegis.mybatis.dao.UserDAO
import com.aegis.mybatis.xmlless.resolver.QueryResolver
import org.junit.jupiter.api.Test


/**
 *
 * Created by 吴昊 on 2018-12-09.
 *
 * @author 吴昊
 * @since 0.0.1
 */
class UserDAOResolverTest : BaseResolverTest(
    User::class.java, UserDAO::class.java, method, "save"
) {

  companion object {
    val method = "findAll"
  }


  @Test
  fun resolveFindAll() {
    println(queries.first { it.method.name == "findAll" })
  }

  @Test
  fun resolvePartlyUpdate() {
    val query = createQueryForMethod("update")
    println(query)
  }

  @Test
  fun resolveSave() {
    val query = createQueryForMethod("save")
    println(query)
  }

  @Test
  fun resolveResultMap() {
    val resultMaps = builderAssistant.configuration.resultMaps
    val ids = resultMaps.map { it.id }
    println(builderAssistant.hashCode())
    assert(ids.contains("$currentNameSpace.com_aegis_mybatis_dao_UserDAO_$method"))
  }

  @Test
  fun resultMapResolve() {
    val resultMaps = builderAssistant.configuration.resultMaps
    resultMaps.forEach {
      println(it.type)
      println(it.mappedColumns)
      println(it.mappedProperties)
    }
  }

  private fun createQueryForMethod(name: String): Any {
    return mapperClass.methods.filter { it.name == name }.map {
      QueryResolver.resolve(it, tableInfo, modelClass, mapperClass, builderAssistant)
    }.first()
  }

}
