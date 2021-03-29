/*
 * Copyright (c) 2011-2020, hubin (jobob@qq.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.aegis.mybatis.xmlless.config.paginition

import org.apache.ibatis.binding.MapperMethod
import org.apache.ibatis.lang.UsesJava7
import org.apache.ibatis.reflection.ExceptionUtil
import org.apache.ibatis.session.SqlSession
import java.lang.invoke.MethodHandles
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 *
 *
 * 替换掉引用<br></br>
 * 重写类： org.apache.ibatis.binding.MapperProxy
 *
 *
 * @author miemie
 * @since 2018-06-09
 */
class XmlLessPageMapperProxy<T>(private val sqlSession: SqlSession,
                                private val mapperInterface: Class<T>,
                                private val methodCache: MutableMap<Method, MapperMethod>) : InvocationHandler {

  companion object {
    private const val serialVersionUID = -6424540398559729838L
  }

  @Throws(Throwable::class)
  override fun invoke(proxy: Any, method: Method, args: Array<Any>?): Any? {
    try {
      if (Any::class.java == method.declaringClass) {
        return if (args != null) {
          method.invoke(this, *args)
        } else {
          method.invoke(this)
        }
      } else if (isDefaultMethod(method)) {
        return invokeDefaultMethod(proxy, method, args)
      }
    } catch (t: Throwable) {
      throw ExceptionUtil.unwrapThrowable(t)
    }
    val mapperMethod = cachedMapperMethod(method)
    return mapperMethod.execute(sqlSession, args)
  }

  private fun cachedMapperMethod(method: Method): MapperMethod {
    var mapperMethod: MapperMethod? = methodCache[method]
    if (mapperMethod == null) {
      mapperMethod = XmlLessPageMapperMethod(mapperInterface, method, sqlSession.configuration)
      methodCache[method] = mapperMethod
    }
    return mapperMethod
  }

  @UsesJava7
  @Throws(Throwable::class)
  private fun invokeDefaultMethod(proxy: Any, method: Method, args: Array<Any>?): Any {
    val constructor = MethodHandles.Lookup::class.java
        .getDeclaredConstructor(Class::class.java, Int::class.javaPrimitiveType)
    if (!constructor.isAccessible) {
      constructor.isAccessible = true
    }
    val declaringClass = method.declaringClass

    val bindResult = constructor
        .newInstance(declaringClass,
            MethodHandles.Lookup.PRIVATE or MethodHandles.Lookup.PROTECTED
                or MethodHandles.Lookup.PACKAGE or MethodHandles.Lookup.PUBLIC)
        .unreflectSpecial(method, declaringClass).bindTo(proxy)
    return if (args != null) {
      bindResult.invokeWithArguments(*args)
    } else {
      bindResult.invokeWithArguments()
    }
  }

  /**
   * Backport of java.lang.reflect.Method#isDefault()
   */
  private fun isDefaultMethod(method: Method): Boolean {
    return method.modifiers and (Modifier.ABSTRACT or Modifier.PUBLIC or Modifier.STATIC) == Modifier.PUBLIC && method.declaringClass.isInterface
  }

  interface MapperMethodInvoker {
    @Throws(Throwable::class)
    operator fun invoke(var1: Any?, var2: Method?, var3: Array<Any?>?, var4: SqlSession?): Any?
  }
}
