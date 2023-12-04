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
package com.baomidou.mybatisplus.core.override

import com.baomidou.mybatisplus.core.override.MybatisMapperProxy.MapperMethodInvoker
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
internal class XmlLessPageMapperProxy<T>(
    private val sqlSession: SqlSession,
    private val mapperInterface: Class<T>,
    private val methodCache: MutableMap<Method, MapperMethodInvoker>
) : InvocationHandler {

  private val methodCache2: MutableMap<Method, XmlLessPageMapperMethod> = mutableMapOf()
  private val mapperProxy = MybatisMapperProxy(sqlSession, mapperInterface, methodCache)

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
    return cachedMapperMethod(method, proxy, args)
  }

  private fun cachedMapperMethod(method: Method, proxy: Any, args: Array<Any>?): Any? {
    val invoker: MapperMethodInvoker? = methodCache[method]
    if (invoker != null) {
      return invoker.invoke(proxy, method, args, sqlSession)
    }
    var mapperMethod = methodCache2[method]
    if (mapperMethod != null) {
      return mapperMethod.execute(sqlSession, args)
    }
    mapperMethod = XmlLessPageMapperMethod(mapperInterface, method, sqlSession.configuration)
    methodCache2[method] = mapperMethod
    return mapperMethod.execute(sqlSession, args)
  }

  @UsesJava7
  @Throws(Throwable::class)
  private fun invokeDefaultMethod(proxy: Any, method: Method, args: Array<Any>?): Any {
    return mapperProxy.invoke(proxy, method, args)
  }

  /**
   * Backport of java.lang.reflect.Method#isDefault()
   */
  private fun isDefaultMethod(method: Method): Boolean {
    return method.modifiers and (Modifier.ABSTRACT or Modifier.PUBLIC or Modifier.STATIC) == Modifier.PUBLIC && method.declaringClass.isInterface
  }

}
