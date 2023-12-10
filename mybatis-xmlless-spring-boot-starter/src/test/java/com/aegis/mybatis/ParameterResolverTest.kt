package com.aegis.mybatis

import com.aegis.mybatis.dao.ThirdPartyInfoMapper
import com.aegis.mybatis.xmlless.resolver.ParameterResolver
import org.junit.jupiter.api.Test

/**
 * Created by 吴昊 on 2023/12/6.
 */
class ParameterResolverTest {

  @Test
  fun isComplexParameter() {
    ThirdPartyInfoMapper::class.java.methods.filter { it.name == "tenantList" }.forEach {
      val a = ParameterResolver.isComplexType(it.returnType)
      println(it.name + "/" + a)
    }
  }

  @Test
  fun resolve() {
  }

  @Test
  fun resolveNames() {
  }

}
