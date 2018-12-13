package com.aegis.mybatis.dao

import com.aegis.mybatis.bean.User
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import org.apache.ibatis.annotations.Mapper

/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
@Mapper
interface MyUserDAO : BaseMapper<User> {

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
  fun update(user: User)

}
