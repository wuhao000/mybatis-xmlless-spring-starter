package com.aegis.mybatis.xmlless.config

import com.baomidou.mybatisplus.core.MybatisConfiguration
import org.apache.ibatis.binding.MapperRegistry
import org.apache.ibatis.session.SqlSession

/**
 * Created by 吴昊 on 2018/12/13.
 */
class MybatisXmlLessConfiguration : MybatisConfiguration() {

  private val mybatisXmlLessMapperRegistry = MybatisXmlLessMapperRegistry(this)

  override fun <T> addMapper(type: Class<T>) {
    mybatisXmlLessMapperRegistry.addMapper(type)
  }

  override fun <T> getMapper(type: Class<T>, sqlSession: SqlSession): T {
    return mybatisXmlLessMapperRegistry.getMapper(type, sqlSession)
  }

  override fun getMapperRegistry(): MapperRegistry {
    return mybatisXmlLessMapperRegistry
  }

  override fun addMappers(packageName: String, superType: Class<*>) {
    mybatisXmlLessMapperRegistry.addMappers(packageName, superType)
  }

  override fun addMappers(packageName: String) {
    mybatisXmlLessMapperRegistry.addMappers(packageName)
  }

  override fun hasMapper(type: Class<*>): Boolean {
    return mybatisXmlLessMapperRegistry.hasMapper(type)
  }
}
