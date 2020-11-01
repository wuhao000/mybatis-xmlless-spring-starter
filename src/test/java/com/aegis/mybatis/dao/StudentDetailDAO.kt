package com.aegis.mybatis.dao

import com.aegis.mybatis.bean.EducationInfo
import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.bean.StudentDetail
import com.aegis.mybatis.xmlless.annotations.JsonResult
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
interface StudentDetailDAO : XmlLessMapper<Student> {

  /**
   *
   * @return
   */
  fun findAll(): List<Student>

  /**
   *
   * @param f
   * @return
   */
  fun findByFavorites(@Param("favorites") f: String): List<Student>

  /**
   *
   * @param f
   * @return
   */
  fun findByFavoritesIn(@Param("favorites") f: List<String>): List<Student>

  /**
   *
   * @return
   */
  fun findDetail(): List<StudentDetail>

  /**
   *
   * @param id
   * @return
   */
  fun findDetailById(@Param("id") id: String): StudentDetail?

  /**
   *
   * @return
   */
  @JsonResult
  fun findEducation(): List<List<EducationInfo>?>

  /**
   *
   * @return
   */
  @JsonResult
  fun findFavorites(): List<List<String>>

  /**
   *
   * @param id
   * @return
   */
  @JsonResult(true)
  fun findNickNamesById(@Param("id") id: String): List<String>

}
