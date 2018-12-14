package com.aegis.mybatis.dao

import com.aegis.mybatis.bean.User
import com.aegis.mybatis.bean.UserSimple
import com.aegis.mybatis.xmlless.annotations.ResolvedName
import com.aegis.mybatis.xmlless.annotations.SelectedProperties
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
   * @return
   */
  @SelectedProperties(["name"])
  fun findAllNames(): List<String>

  /**
   *
   * @param id
   * @return
   */
  fun findById(id: Int): User?

  /**
   *
   * @param id
   * @return
   */
  @ResolvedName("findById")
  fun findSimpleUserById(id: Int): UserSimple

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

  fun count(): Int

}
