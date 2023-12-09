package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.bean.User
import com.aegis.mybatis.dao.UserDAO
import org.junit.jupiter.api.Test
import kotlin.reflect.jvm.javaMethod


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
    println(queries.first { it.method == UserDAO::findAll.javaMethod })
  }

  @Test
  fun resolvePartlyUpdate() {
    val query = createQueryForMethod(UserDAO::update.javaMethod!!)
    println(query)
  }

  @Test
  fun resolveSave() {
    val query = createQueryForMethod(UserDAO::save.javaMethod!!)
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


}
