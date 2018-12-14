package com.aegis.mybatis.dao

import com.aegis.mybatis.bean.User
import com.aegis.mybatis.xmlless.config.XmlLessMapper
import org.apache.ibatis.annotations.Mapper

/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
@Mapper
interface UserDAO : XmlLessMapper<User> {

  /**
   *
   * @param id
   */
  fun deleteById(id: Int)

  /**
   *
   * @param id
   * @return
   */
  fun findById(id: Int): User?

  /**
   *
   * @param user
   */
  fun save(user: User)

  /**
   *
   * @param user
   */
  fun saveAll(user: List<User>)

  /**
   *
   * @param user
   */
  fun update(user: User)

}
