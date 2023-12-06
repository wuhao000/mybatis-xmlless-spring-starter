package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.bean.AppCluster
import com.aegis.mybatis.dao.AppClusterDAO
import com.aegis.mybatis.xmlless.resolver.QueryResolver
import org.junit.jupiter.api.Test
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.javaMethod


/**
 *
 * Created by 吴昊 on 2018-12-09.
 *
 * @author 吴昊
 * @since 0.0.1
 */
class AppClusterDAOResolverTest : BaseResolverTest(
    AppCluster::class.java, AppClusterDAO::class.java, "findAll"
) {

  @Test
  fun resolveFindAllPage(){
    val query = createQueryForMethod("findAll")
    println(query)
  }

  private fun createQueryForMethod(name: String): Any {
    return mapperClass.kotlin.declaredFunctions
        .filter { it.name == name }
        .map {
          QueryResolver.resolve(it.javaMethod!!, tableInfo, modelClass, mapperClass, builderAssistant)
        }.first()
  }

}
