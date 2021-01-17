package com.aegis.mybatis.xmlless.starter

import com.baomidou.mybatisplus.core.metadata.IPage
import org.apache.ibatis.reflection.factory.DefaultObjectFactory

class MyObjectFactory : DefaultObjectFactory() {

  override fun <T : Any?> isCollection(type: Class<T>?): Boolean {
    if (type == IPage::class.java) {
      return true
    }
    return super.isCollection(type)
  }

}
