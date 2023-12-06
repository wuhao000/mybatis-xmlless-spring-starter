package com.aegis.mybatis.dao.tests

import com.aegis.mybatis.BaseTest
import com.aegis.mybatis.bean.StringKeyObj
import com.aegis.mybatis.dao.StringKeyObjDAO
import jakarta.annotation.Resource
import org.junit.jupiter.api.Test

/**
 * TODO
 *
 * @author wuhao
 * @date 2023/12/6 18:27
 * @since v0.0.0
 * @version 1.0
 */

class StringKeyObjDAOTest: BaseTest() {

  @Resource
  private lateinit var dao: StringKeyObjDAO


  @Test
  fun insert() {
    dao.insert(StringKeyObj().apply {
      name = "test"
    })
    dao.findAll().forEach {
      println(it.id)
    }
  }
}
