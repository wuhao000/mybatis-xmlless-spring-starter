package com.aegis.mybatis.dao.tests

import com.aegis.mybatis.BaseTest
import com.aegis.mybatis.bean.AppCluster
import com.aegis.mybatis.dao.AppClusterDAO
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import kotlin.test.assertEquals


/**
 * Created by 吴昊 on 2018/12/17.
 */
open class AppClusterDAOTest : BaseTest() {

  @Autowired
  private lateinit var dao: AppClusterDAO

  @Test
  fun findAllPageable() {
    dao.save(AppCluster())
    dao.save(AppCluster())
    val res = dao.findAll(null, PageRequest.of(0, 20))
    assertEquals(2, res.totalElements)
  }

}
