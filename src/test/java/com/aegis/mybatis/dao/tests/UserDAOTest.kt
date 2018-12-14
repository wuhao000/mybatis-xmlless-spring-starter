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
    println(user.id)
  }

}
