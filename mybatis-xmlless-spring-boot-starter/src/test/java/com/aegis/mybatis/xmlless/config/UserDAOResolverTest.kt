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
    UserDAO::class.java,
    User::class.java,
    method, "save"
) {

  companion object {
    val method = "findAll"
  }


  @Test
  fun resolveFindAll() {
    val q = createQueryForMethod(UserDAO::findAll.javaMethod!!)
    println(q)
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
    val method = UserDAO::findAll.javaMethod!!
    createQueryForMethod(method)
    val resultMaps = builderAssistant.configuration.resultMaps
    val ids = resultMaps.map { it.id }
    println(ids)
    assert(ids.contains("$currentNameSpace.com_aegis_mybatis_dao_UserDAO_${method.name}"))
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
