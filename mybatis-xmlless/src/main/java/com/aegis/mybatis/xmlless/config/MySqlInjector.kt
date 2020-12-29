package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.xmlless.methods.XmlLessMethods
import com.baomidou.mybatisplus.core.injector.AbstractMethod
import com.baomidou.mybatisplus.core.injector.AbstractSqlInjector
import com.baomidou.mybatisplus.core.injector.methods.*
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

  override fun getMethodList(mapperClass: Class<*>?): List<AbstractMethod> {
    return listOf(
        XmlLessMethods(),
        Insert(),
        Delete(),
        DeleteByMap(),
        DeleteById(),
        DeleteBatchByIds(),
        Update(),
        UpdateById(),
        SelectById(),
        SelectBatchByIds(),
        SelectByMap(),
        SelectOne(),
        SelectCount(),
        SelectObjs(),
        SelectMapsPage(),
        SelectObjs(),
        SelectList(),
        SelectPage()
    )
  }

}
