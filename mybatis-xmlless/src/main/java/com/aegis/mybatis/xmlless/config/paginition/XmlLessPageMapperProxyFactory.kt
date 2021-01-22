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

import org.apache.ibatis.binding.MapperProxyFactory
import org.apache.ibatis.session.SqlSession
import org.springframework.core.ResolvableType
import kotlin.reflect.KClass
import kotlin.reflect.KType

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
class XmlLessPageMapperProxyFactory<T>(mapperInterface: Class<T>)
  : MapperProxyFactory<T>(mapperInterface) {

  override fun newInstance(sqlSession: SqlSession): T {
    val mapperProxy = XmlLessPageMapperProxy(sqlSession, mapperInterface, methodCache)
    return newInstance(mapperProxy)
  }

}
