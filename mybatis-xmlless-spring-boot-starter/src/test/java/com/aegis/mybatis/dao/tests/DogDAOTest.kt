package com.aegis.mybatis.dao.tests

import com.aegis.mybatis.BaseTest
import com.aegis.mybatis.dao.AppClusterDAO
import com.aegis.mybatis.dao.DogDAO
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest


/**
 * Created by 吴昊 on 2018/12/17.
 */
open class DogDAOTest : BaseTest() {

  @Autowired
  private lateinit var dao: DogDAO

  @Test
  fun findAllPageable() {
    dao.findById(1)
  }

}
