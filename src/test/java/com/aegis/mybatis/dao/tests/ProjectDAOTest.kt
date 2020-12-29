package com.aegis.mybatis.dao.tests

import com.aegis.mybatis.dao.ProjectDAO
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired


/**
 * TODO
 *
 * @author Administrator
 * @version 1.0
 * @date 2020/7/18 14:13
 * @since TODO
 */
class ProjectDAOTest : BaseTest() {

  @Autowired
  private lateinit var dao: ProjectDAO

  @Test
  fun findById() {
    dao.findTapdWorkspaceId()
  }


  @Test
  fun findByI2d() {
    dao.findTapdWorkspaceIdById("BJ201912001")
  }

}
