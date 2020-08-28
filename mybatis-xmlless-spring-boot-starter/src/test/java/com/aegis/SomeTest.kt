package com.aegis

import com.aegis.mybatis.xmlless.config.MappingResolver
import org.junit.Test
import org.springframework.cglib.proxy.Enhancer
import org.springframework.cglib.proxy.MethodInterceptor
import org.springframework.cglib.proxy.MethodProxy
import java.lang.reflect.Method

/**
 * Created by 吴昊 on 2018/12/13.
 */
class SomeTest {

  @Test
  fun test2() {
    class MyMethodInterceptor : MethodInterceptor {

      @Throws(Throwable::class)
      override fun intercept(obj: Any?, method: Method?, args: Array<Any?>?, proxy: MethodProxy): Any {
        //return "1"
//        return proxy.invokeSuper(obj, args)
        println("before")
//        val res: Any = proxy.invokeSuper(obj, args)
        println("after")
        return "proxy name"
      }

    }


    val enhancer = Enhancer()
    enhancer.setSuperclass(TestInter::class.java)
    enhancer.setCallback(MyMethodInterceptor())
    val a = enhancer.create()
    println((a as TestInter).name())
    val clazz = a::class.java
    val b = clazz.getConstructor().newInstance()
    println((b as TestInter).name())

  }

}

open class TestInter {

  /**
   *
   * @return
   */
  fun name(): String {
    return "a"
  }

}

open class TestInterImpl {

  fun name(): String {
    println("222")
    println("say hello")
    return "say hello"
  }

}
