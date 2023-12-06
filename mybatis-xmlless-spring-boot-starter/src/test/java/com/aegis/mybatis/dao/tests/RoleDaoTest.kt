package com.aegis.mybatis.dao.tests

import com.aegis.mybatis.BaseTest
import com.aegis.mybatis.bean.Role
import com.aegis.mybatis.dao.RoleDAO
import jakarta.annotation.Resource
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 *
 * @author 吴昊
 * @date 2023/12/6 13:21
 * @since v0.0.0
 * @version 1.0
 */
class RoleDaoTest : BaseTest() {

  @Resource
  private lateinit var dao: RoleDAO


  @Test
  fun del() {
    val role = Role(name = "a")
    dao.save(role)
    assert(role.id > 0)
    val role1 = dao.findById(role.id)
    assertNotNull(role1)
    dao.deleteById(role.id)
    val role2 = dao.findById(role.id)
    assertNull(role2)
    val role3 = dao.findAll().first { it.id == role.id }
    assertEquals("2", role3.delFlag)
  }

}
