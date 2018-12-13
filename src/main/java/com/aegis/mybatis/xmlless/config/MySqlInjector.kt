package com.aegis.mybatis.xmlless.config

import com.aegis.mybatis.xmlless.methods.QueryPage
import com.aegis.mybatis.xmlless.methods.UnknownMethods
import com.baomidou.mybatisplus.core.injector.AbstractMethod
import com.baomidou.mybatisplus.core.injector.AbstractSqlInjector
import com.baomidou.mybatisplus.core.injector.methods.*
import org.springframework.stereotype.Component


/**
 *
 * Created by 吴昊 on 2018/11/6.
 *
 * @author 吴昊
 * @since 0.0.1-SNAPSHOT
 */
@Component
class MySqlInjector : AbstractSqlInjector() {

  override fun getMethodList(): List<AbstractMethod> {
    return listOf(
        DeleteByMap(),
        DeleteBatchByIds(),
        SelectBatchByIds(),
        SelectByMap(),
        SelectOne(),
        SelectCount(),
        SelectMaps(),
        SelectMapsPage(),
        SelectObjs(),
        SelectList(),
        QueryPage(),
        UnknownMethods()
    )
  }

}
