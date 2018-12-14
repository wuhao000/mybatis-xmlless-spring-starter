package com.aegis.mybatis.dao

import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.xmlless.annotations.ResolvedName
import com.aegis.mybatis.xmlless.config.XmlLessMapper
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 *
 * Created by 吴昊 on 2018-12-12.
 *
 * @author 吴昊
 * @since 0.0.4
 */
@Mapper
interface StudentDAO : XmlLessMapper<Student> {

  /**
   *
   * @return
   */
  fun count(): Int

  /**
   *
   * @param id
   */
  fun deleteById(id: String)

  /**
   *
   * @param ids
   */
  @ResolvedName("deleteByIdInIds")
  fun deleteByIds(@Param("ids") ids: List<String>)

  /**
   *
   * @param name
   */
  fun deleteByName(name: String)

  /**
   *
   * @param id
   * @return
   */
  fun existsById(id: String): Boolean

  /**
   *
   * @param name
   * @return
   */
  fun existsByName(name: String): Boolean

  /**
   *
   * @return
   */
  fun findAll(): List<Student>

  /**
   *
   * @param page
   * @return
   */
  @ResolvedName("findAll")
  fun findAllPage(@Param("pageable") page: Pageable): Page<Student>

  /**
   *
   * @param pageable
   * @return
   */
  @ResolvedName("findAll")
  fun findAllPageable(@Param("pageable") pageable: Pageable): Page<Student>

  /**
   *
   * @return
   * @param id
   */
  fun findById(id: String): Student?

  /**
   *
   * @param student
   */
  fun save(student: Student)

  /**
   *
   * @param list
   */
  fun saveAll(list: List<Student>)

  /**
   *
   * @param student
   * @return
   */
  fun update(student: Student): Int

  /**
   *
   * @param name
   * @param id
   * @return
   */
  fun updateNameById(name: String, id: String): Int

}
