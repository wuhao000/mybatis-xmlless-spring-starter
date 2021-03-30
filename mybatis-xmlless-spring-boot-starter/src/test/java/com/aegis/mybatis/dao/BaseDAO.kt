package com.aegis.mybatis.dao

import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.xmlless.config.XmlLessMapper
import com.baomidou.mybatisplus.core.mapper.BaseMapper
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
   *
   * @param id
   */
  fun findById(@Param("id") id: ID): T?

  fun findAll(): List<T>

  fun save(t: T)

}
