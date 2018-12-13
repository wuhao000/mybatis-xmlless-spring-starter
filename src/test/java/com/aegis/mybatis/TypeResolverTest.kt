package com.wuhao.mybatistest

import org.junit.Test
import java.io.BufferedReader
import java.io.InputStreamReader
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
  fun console() {
    val p = Runtime.getRuntime()
        .exec("""echo -e "\033[30m黑色字\033[0m"""")
    val status = p.waitFor()
    val br = BufferedReader(InputStreamReader(p.inputStream))
    val strbr = StringBuffer()
    var line: String? = br.readLine()
    while (line != null) {
      strbr.append(line).append("\n")
      line = br.readLine()
    }
    println(strbr)

    Runtime.getRuntime()
        .exec("""sh ls"""")
  }

  @Test
  fun getType() {
    TypeResolverTest::class.java.methods.forEach {
      if (it.name == "t") {
        println(Collection::class.java.isAssignableFrom(it.returnType))
        val type = it.genericReturnType as ParameterizedType
        val typeArgument = type.actualTypeArguments[0]
        println(typeArgument.javaClass as Class<*>)
      }
    }
  }

}
