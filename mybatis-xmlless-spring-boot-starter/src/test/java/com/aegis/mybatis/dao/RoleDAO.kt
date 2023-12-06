@file:Suppress("unused")

package com.aegis.mybatis.dao

import com.aegis.mybatis.bean.Role
import com.aegis.mybatis.xmlless.XmlLessMapper
import com.aegis.mybatis.xmlless.annotations.DeleteValue
import com.aegis.mybatis.xmlless.annotations.Logic
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

  @Logic(DeleteValue.NotDeleted)
  fun findById(id: Int): Role?

  @Logic(DeleteValue.Deleted)
  fun deleteById(id: Int)

  @Logic(DeleteValue.Deleted)
  fun findAll(): List<Role>

}
