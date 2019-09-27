package com.aegis.mybatis.dao.tests

import com.aegis.mybatis.bean.User
import com.aegis.mybatis.dao.UserDAO
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort


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
  fun findAllNames() {
    val names = dao.findAllNames()
    println(names)
    assert(names.size == dao.count())
  }

  @Test
  fun pageable() {
    val page = dao.findAll(PageRequest.of(
        0, 20, Sort.Direction.DESC, "id"
    ))
    assert(page.content.isNotEmpty())
  }

  @Test
  fun save() {
    val user = User(
        name = "w",
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
        deleted = false
    )
    val user2 = User(
        name = "w",
        deleted = false
    )
    dao.saveAll(listOf(user1, user2))
    assert(user1.id!! > 0)
    assert(user2.id!! > 0)
    dao.deleteById(user1.id!!)
    dao.deleteById(user2.id!!)
  }

}
