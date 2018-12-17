package com.aegis.mybatis.dao.tests

import com.aegis.mybatis.dao.ServerDAO
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired


/**
 * Created by 吴昊 on 2018/12/17.
 */
class ServerDAOTest : BaseTest() {

  @Autowired
  private lateinit var serverDAO: ServerDAO

  @Test
  fun findById() {
    println(serverDAO.findById(1))
    println(serverDAO.findById(2))
  }

}
