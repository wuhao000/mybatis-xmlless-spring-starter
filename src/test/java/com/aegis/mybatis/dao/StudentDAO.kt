package com.aegis.mybatis.dao

import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.bean.StudentDetail
import com.aegis.mybatis.xmlless.annotations.ExcludeProperties
import com.aegis.mybatis.xmlless.annotations.ResolvedName
import com.aegis.mybatis.xmlless.annotations.SelectedProperties
import com.aegis.mybatis.xmlless.annotations.TestExpression
import com.aegis.mybatis.xmlless.config.XmlLessMapper
import com.aegis.mybatis.xmlless.enums.TestType
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate

/**
 *
 * Created by 吴昊 on 2018-12-12.
 *
 * @author 吴昊
 * @since 0.0.4
 */
@Mapper
interface StudentDAO : XmlLessMapper<Student> {

  fun findDetail(): List<StudentDetail>

  fun findId(): List<String>

  /**
   *
   * @return
   */
  fun count(): Int

  @SelectedProperties(properties = ["id", "name", "birthday"])
  fun findByBirthday(date: LocalDate): List<Student>
  /**
   *
   * @param id
   */
  fun deleteById(@Param("id") id: String)

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
  fun deleteByName(@Param("name") name: String)

  /**
   *
   * @param id
   * @return
   */
  fun existsById(@Param("id") id: String): Boolean

  /**
   *
   * @param name
   * @return
   */
  fun existsByName(@Param("name") name: String): Boolean

  /**
   *
   * @return
   */
  fun findAll(): List<Student>

  /**
   *
   * @param page
   * @return
   * @param form
   * @param name
   * @param subjectId
   */
  @ResolvedName("findAllByNameEqAndSubjectIdEq")
  fun findAllPage(
      @TestExpression([TestType.NotNull, TestType.NotEmpty]) name: String?,
      subjectId: Int?,
      @Param("pageable") page: Pageable): Page<Student>

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
   */
  fun findByGraduatedEqTrue(): List<Student>

  /**
   *
   * @return
   * @param id
   */
  fun findById(@Param("id") id: String): Student?

  /**
   *
   * @param phoneNumber
   * @return
   */
  fun findByPhoneNumberLikeLeft(@Param("phoneNumber") phoneNumber: String): List<Student>

  /**
   *
   * @param phoneNumber
   * @return
   */
  fun findByPhoneNumberLikeRight(@Param("phoneNumber") phoneNumber: String): List<Student>

  /**
   *
   * @param subJectId
   * @return
   * @param subjectId
   */
  fun findBySubjectId(@Param("subjectId") subjectId: Int): List<Student>

  /**
   *
   * @param student
   */
  fun save(student: Student)


  fun saveOrUpdate(student: Student)

  @ExcludeProperties(update = ["name"])
  fun saveOrUpdateAll(list: List<Student>)

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

  /**
   *
   * @param student
   * @return
   */
  @SelectedProperties(["name", "phoneNumber"])
  @ResolvedName("update")
  fun updatePartly(student: Student): Int

}
