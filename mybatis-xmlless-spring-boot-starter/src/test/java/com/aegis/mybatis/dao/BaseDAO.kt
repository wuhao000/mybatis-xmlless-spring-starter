package com.aegis.mybatis.dao

import com.aegis.mybatis.xmlless.annotations.*
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

/**
 *
 * Created by 吴昊 on 2018-12-12.
 *
 * @author 吴昊
 * @since 0.0.4
 */
interface BaseDAO<T, ID> {

  /**
   * @return
   */
  fun findAll(): List<T>

  /**
   *
   * @param id
   * @return
   */
  fun findById(@Param("id") id: ID): T?

  /**
   * @return
   */
  @NotDeleted
  @ResolvedName("findAll")
  fun findNonDeleted(): List<T>

  @Deleted
  @ResolvedName("findAll")
  fun findDeleted(): List<T>

  /**
   * @param t
   */
  fun save(t: T)

}
