package com.aegis.mybatis.xmlless.config

import com.baomidou.mybatisplus.core.MybatisConfiguration
import com.baomidou.mybatisplus.core.metadata.IPage
import org.apache.ibatis.binding.MapperRegistry
import org.apache.ibatis.reflection.factory.DefaultObjectFactory
import org.apache.ibatis.session.SqlSession

class MyObjectFactory : DefaultObjectFactory() {

  override fun <T : Any?> isCollection(type: Class<T>?): Boolean {
    if (type == IPage::class.java) {
      return true
    }
    return super.isCollection(type)
  }

}

/**
 * Created by 吴昊 on 2018/12/13.
 *
 * @author wuhao
 * @date 2018
 * @version 0.1
 * @since 0.1
 */
class MybatisXmlLessConfiguration() : MybatisConfiguration() {

  private val mybatisXmlLessMapperRegistry = MybatisXmlLessMapperRegistry(this)

  override fun <T> addMapper(type: Class<T>) {
    mybatisXmlLessMapperRegistry.addXMlLessMapper(type)
  }

  override fun addMappers(packageName: String, superType: Class<*>?) {
    mybatisXmlLessMapperRegistry.addMappers(packageName, superType)
  }

  override fun addMappers(packageName: String) {
    mybatisXmlLessMapperRegistry.addMappers(packageName)
  }

  override fun <T> getMapper(type: Class<T>, sqlSession: SqlSession): T {
    return mybatisXmlLessMapperRegistry.getXmlLessMapper(type, sqlSession)
  }

  override fun getMapperRegistry(): MapperRegistry {
    return mybatisXmlLessMapperRegistry
  }

  override fun hasMapper(type: Class<*>): Boolean {
    return mybatisXmlLessMapperRegistry.hasXmlLessMapper(type)
  }

}
