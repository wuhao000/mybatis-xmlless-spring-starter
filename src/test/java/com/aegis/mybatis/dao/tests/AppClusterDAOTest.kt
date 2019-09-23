package com.aegis.mybatis.dao.tests

import com.aegis.mybatis.dao.AppClusterDAO
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest


/**
 * Created by 吴昊 on 2018/12/17.
 */
class AppClusterDAOTest : BaseTest() {

  @Autowired
  private lateinit var dao: AppClusterDAO

  @Test
  fun findAllPageable() {
    val res = dao.findAll("a", PageRequest.of(0, 20))
    println(res.totalElements)
  }

}
