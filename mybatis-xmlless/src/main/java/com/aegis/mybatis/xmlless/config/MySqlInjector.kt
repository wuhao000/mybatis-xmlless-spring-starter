package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.xmlless.methods.XmlLessMethods
import com.baomidou.mybatisplus.core.injector.AbstractMethod
import com.baomidou.mybatisplus.core.injector.AbstractSqlInjector
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector
import com.baomidou.mybatisplus.core.metadata.TableInfo
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

  override fun getMethodList(mapperClass: Class<*>, tableInfo: TableInfo): MutableList<AbstractMethod> {
    val list = arrayListOf<AbstractMethod>()
    val mybatisPlusInjections = defaultSqlInjector.getMethodList(mapperClass, tableInfo)
    list.add(XmlLessMethods())
    list.addAll(mybatisPlusInjections)
    return list
  }

}
