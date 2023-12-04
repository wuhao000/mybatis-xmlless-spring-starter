/*
 * Copyright 2009-2015 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aegis.mybatis.xmlless.config

import com.baomidou.mybatisplus.core.override.XmlLessPageMapperProxyFactory
import org.apache.ibatis.binding.BindingException
import org.apache.ibatis.binding.MapperRegistry
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.SqlSession
import java.util.*

/**
 *
 *
 * 继承至MapperRegistry
 *
 *
 * @since 2017-04-19
 */
@Suppress("UNCHECKED_CAST")
class MybatisXmlLessMapperRegistry(private val config: Configuration) : MapperRegistry(config) {

  private val knownMappers = HashMap<Class<*>, XmlLessPageMapperProxyFactory<*>>()

  fun <T> addXMlLessMapper(type: Class<T>) {
    addMapper(type)
  }

  override fun <T> addMapper(type: Class<T>) {
    if (type.isInterface) {
      if (hasMapper(type)) {
        // 如果之前注入 直接返回
        return
        // throw new BindingException("Type " + type +
        // " is already known to the MybatisPlusMapperRegistry.");
      }
      var loadCompleted = false
      try {
        knownMappers[type] = XmlLessPageMapperProxyFactory(type)
        // It's important that the type is added before the parser is run
        // otherwise the binding may automatically be attempted by the
        // mapper parser. If the type is already known, it won't try.
        // 自定义无 XML 注入
        val parser = MybatisXmlLessMapperAnnotationBuilder(config, type)
        parser.parse()
        loadCompleted = true
      } finally {
        if (!loadCompleted) {
          knownMappers.remove(type)
        }
      }
    }
  }

  fun <T> getXmlLessMapper(type: Class<T>, sqlSession: SqlSession): T {
    return getMapper(type, sqlSession)
  }

  override fun <T> getMapper(type: Class<T>, sqlSession: SqlSession): T {
    val mapperProxyFactory = knownMappers[type] as XmlLessPageMapperProxyFactory<T>
    try {
      return mapperProxyFactory.newInstance(sqlSession)
    } catch (e: Exception) {
      throw BindingException("Error getting mapper instance. Cause: $e", e)
    }
  }

  /**
   * @since 3.2.2
   */
  override fun getMappers(): Collection<Class<*>> {
    return Collections.unmodifiableCollection(knownMappers.keys)
  }

  override fun <T> hasMapper(type: Class<T>): Boolean {
    return knownMappers.containsKey(type)
  }

  fun <T> hasXmlLessMapper(type: Class<T>): Boolean {
    return hasMapper(type)
  }

}
