@file:Suppress("unused")

package com.aegis.mybatis.dao

import com.aegis.mybatis.bean.User
import com.aegis.mybatis.bean.UserSimple
import com.aegis.mybatis.xmlless.XmlLessMapper
import com.aegis.mybatis.xmlless.annotations.ResolvedName
import com.aegis.mybatis.xmlless.annotations.SelectedProperties
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
@Mapper
interface UserDAO : XmlLessMapper<User> {

  /**
   *
   * @return
   */
  fun count(): Int

  /**
   *
   * @param id
   */
  fun deleteById(id: Int)

  /**
   *
   * @param pageable
   * @return
   */
  fun findAll(@Param("pageable") pageable: Pageable): Page<User>

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
   * @param idNumber
   * @return
   */
  fun findByIdNumber(idNumber: String): List<User>

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

}
