package com.aegis.mybatis.dao

import com.aegis.mybatis.xmlless.annotations.DeleteValue
import com.aegis.mybatis.xmlless.annotations.Logic
import com.aegis.mybatis.xmlless.annotations.ResolvedName
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

/**
 *
 * Created by 吴昊 on 2018-12-12.
 *
 * @author 吴昊
 * @since 0.0.4
 */
@Mapper
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
  @Logic(DeleteValue.NotDeleted)
  @ResolvedName("findAll")
  fun findNonDeleted(): List<T>

  @Logic(DeleteValue.Deleted)
  @ResolvedName("findAll")
  fun findDeleted(): List<T>

  /**
   * @param t
   */
  fun save(t: T)

}
