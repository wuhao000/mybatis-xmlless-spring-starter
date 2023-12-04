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

import org.apache.ibatis.session.SqlSession
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap

/**
 *
 *
 * 替换掉引用<br></br>
 * 重写类： org.apache.ibatis.binding.MapperProxyFactory
 *
 *
 * @author miemie
 * @since 2018-06-09
 */
internal class XmlLessPageMapperProxyFactory<T>(val mapperInterface: Class<T>) {

  private val methodCache: MutableMap<Method, MybatisMapperProxy.MapperMethodInvoker> = ConcurrentHashMap()

  fun newInstance(sqlSession: SqlSession): T {
    val mapperProxy = XmlLessPageMapperProxy(sqlSession, mapperInterface, methodCache)
    return newInstance(mapperProxy)
  }

  protected fun newInstance(mapperProxy: XmlLessPageMapperProxy<T>?): T {
    return Proxy.newProxyInstance(mapperInterface.classLoader, arrayOf<Class<*>>(mapperInterface), mapperProxy) as T
  }

}
