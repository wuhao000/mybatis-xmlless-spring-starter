package com.aegis.mybatis.xmlless.config

import org.junit.jupiter.api.Test
import java.lang.reflect.ParameterizedType


/**
 *
 * Created by 吴昊 on 2018-12-09.
 *
 * @author 吴昊
 * @since 0.0.1
 */
class TypeResolverTest {

  @Test
  fun getType() {
    TypeResolverTest::class.java.methods.forEach {
      if (it.name == "t") {
        val type = it.genericReturnType as ParameterizedType
        val typeArgument = type.actualTypeArguments[0]
        println(typeArgument.javaClass as Class<*>)
      }
    }
  }

}
