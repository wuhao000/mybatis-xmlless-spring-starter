package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.xmlless.methods.XmlLessMethods
import com.baomidou.mybatisplus.core.injector.AbstractMethod
import com.baomidou.mybatisplus.core.injector.AbstractSqlInjector
import org.springframework.stereotype.Component


/**
 *
 * Created by 吴昊 on 2018/11/6.
 *
 * @author 吴昊
 * @since 0.0.1-SNAPSHOT
 */
@Suppress("unused")
@Component
class MySqlInjector : AbstractSqlInjector() {

  override fun getMethodList(): List<AbstractMethod> {
    return listOf(
        XmlLessMethods()
    )
  }

}
