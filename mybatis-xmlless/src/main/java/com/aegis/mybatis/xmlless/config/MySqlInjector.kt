package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.xmlless.methods.XmlLessMethods
import com.baomidou.mybatisplus.core.injector.AbstractMethod
import com.baomidou.mybatisplus.core.injector.AbstractSqlInjector
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector
import com.baomidou.mybatisplus.core.injector.methods.*
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import org.springframework.stereotype.Component


/**
 * SQL注入
 *
 * Created by 吴昊 on 2018/11/6.
 *
 * @author 吴昊
 * @since 0.0.1-SNAPSHOT
 */
@Suppress("unused")
@Component
class MySqlInjector : AbstractSqlInjector() {

  val defaultSqlInjector = DefaultSqlInjector()

  override fun getMethodList(mapperClass: Class<*>): List<AbstractMethod> {
    val list = arrayListOf<AbstractMethod>()
    val mybatisPlusInjections = defaultSqlInjector.getMethodList(mapperClass)
    if (BaseMapper::class.java.isAssignableFrom(mapperClass)) {
      list.addAll(mybatisPlusInjections)
      list.add(XmlLessMethods())
    } else {
      list.add(XmlLessMethods())
      list.addAll(mybatisPlusInjections)
    }
    return list
  }

}
