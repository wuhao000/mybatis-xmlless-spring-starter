package com.aegis.mybatis.dao

import com.aegis.mybatis.bean.Dog
import com.aegis.mybatis.xmlless.annotations.DeleteValue
import com.aegis.mybatis.xmlless.annotations.Logic
import com.aegis.mybatis.xmlless.annotations.ResolvedName
import com.aegis.mybatis.xmlless.config.XmlLessMapper
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
interface DogDAO : XmlLessMapper<Dog>, BaseDAO<Dog, Int> {

  /**
   * @param id
   * @return
   */
  fun deleteById(@Param("id") id: Int): Int

  /**
   * @param id
   * @return
   */
  @Logic(DeleteValue.Deleted)
  @ResolvedName("deleteById")
  fun deleteLogicById(@Param("id") id: Int): Int

  /**
   * @param id
   * @return
   */
  fun existsById(@Param("id") id: Int): Boolean

  /**
   * @param name
   * @return
   */
  fun findByName(@Param("name") name: String): Dog?

  /**
   * @param names
   * @return
   */
  fun findByNamesIn(@Param("names") names: List<String>): List<Dog>

}
