package com.aegis.mybatis.dao.tests

import com.aegis.mybatis.bean.User
import com.aegis.mybatis.dao.UserDAO
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired


/**
 *
 * Created by 吴昊 on 2018/12/14.
 *
 * @author 吴昊
 * @since 0.0.7
 */
class UserDAOTest : BaseTest() {

  @Autowired
  private lateinit var dao: UserDAO

  @Test
  fun save() {
    val user = User(
        name = "w",
        age = 12,
        deleted = false
    )
    dao.save(user)
    assert(user.id!! > 0)
    println(dao.findSimpleUserById(user.id!!))
    dao.deleteById(user.id!!)
  }

  @Test
  fun saveAll() {
    val user1 = User(
        name = "test",
        age = 12,
        deleted = false
    )
    val user2 = User(
        name = "w",
        age = 12,
        deleted = false
    )
    dao.saveAll(listOf(user1, user2))
    assert(user1.id!! > 0)
    assert(user2.id!! > 0)
    dao.deleteById(user1.id!!)
    dao.deleteById(user2.id!!)
  }

  @Test
  fun findAllNames(){
    val names = dao.findAllNames()
    println(names)
    assert(names.size == dao.count())
  }
}
