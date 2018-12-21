package com.aegis.mybatis.dao

import com.aegis.mybatis.bean.Server
import com.aegis.mybatis.xmlless.config.XmlLessMapper
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

/**
 *
 * Created by 吴昊 on 2018/12/17.
 *
 * @author 吴昊
 * @since 0.0.9
 */
@Mapper
interface ServerDAO : XmlLessMapper<Server> {

  /**
   *
   * @param name
   */
  fun deleteByName(@Param("name") name: String)

  /**
   *
   * @param name
   * @return
   */
  fun existsByName(@Param("name") name: String): Boolean

  /**
   *
   * @return
   * @param id
   */
  fun findById(@Param("id") id: Int): Server?

  /**
   *
   * @param name
   * @return
   */
  fun findByName(@Param("name") name: String): Server?

  /**
   *
   * @param server
   */
  fun save(server: Server)

  /**
   *
   * @param server
   * @return
   */
  fun update(server: Server): Int

}
