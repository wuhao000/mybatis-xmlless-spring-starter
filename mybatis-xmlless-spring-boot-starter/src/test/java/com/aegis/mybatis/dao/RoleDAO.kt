@file:Suppress("unused")

package com.aegis.mybatis.dao

import com.aegis.mybatis.bean.Role
import com.aegis.mybatis.xmlless.XmlLessMapper
import com.aegis.mybatis.xmlless.annotations.Deleted
import com.aegis.mybatis.xmlless.annotations.NotDeleted
import org.apache.ibatis.annotations.Mapper

/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
@Mapper
interface RoleDAO : XmlLessMapper<Role> {

  /**
   *
   * @param user
   */
  fun save(user: Role)

  @NotDeleted
  fun findById(id: Int): Role?

  @Deleted
  fun deleteById(id: Int)

  @Deleted
  fun findAll(): List<Role>

}
