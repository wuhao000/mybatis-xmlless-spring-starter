package com.aegis.mybatis.dao

import com.aegis.mybatis.bean.*
import com.aegis.mybatis.xmlless.XmlLessMapper
import com.aegis.mybatis.xmlless.annotations.*
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

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
   * @param form
   * @param currentUserId
   * @return
   */
  @ResolvedName(
      name = "findBy",
      conditions = [
        "name like", "age", "createTime between start and end",
        "userName like keywords"
      ],
      sort = ["createTime desc"]
  )
  @NotDeleted
  fun find(
      @Param("form") form: StudentQueryForm,
      @Param("currentUserId") currentUserId: Int? = null
  ): List<Student>

  /**
   * @param keywords
   * @return
   */
  @NotDeleted
  fun findByUserNameLike(keywords: String): List<Student>

  /**
   * @param keywords
   * @return
   */
  @ResolvedName(
      name = "find",
      conditions = [
        "schoolLocation like keywords"
      ]
  )
  @NotDeleted
  fun findVO(keywords: String? = null): List<StudentVO>

  /**
   * @return
   */
  @NotDeleted
  @ResolvedName(
      name = "find",
      groupBy = ["grade"]
  )
  @PropertiesMapping(
      [
        PropertyMapping("sumAge", "sum(age)"),
        PropertyMapping("avgAge", "avg(age)"),
        PropertyMapping("count", "count(*)")
      ]
  )
  fun statistics(): List<StudentStats>

  /**
   * @param pageable
   * @return
   */
  @ResolvedName("find")
  fun findVOPage(
      pageable: Pageable
  ): Page<StudentVO>

  /**
   * @param form
   * @return
   */
  @ResolvedName(
      name = "findBy",
      conditions = [
        "name", "age", "userName like keywords or createUserName like keywords"
      ]
  )
  fun findByNameAndAgeAndUserNameLikeKeywordsOrCreateUserNameLikeKeywords(
      form: StudentQueryForm
  ): List<Student>

  /**
   *
   * @return
   */
  fun count(): Int

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
      name: String?,
      subjectId: Int?,
      @Param("pageable") page: Pageable
  ): Page<Student>

  /**
   *
   * @param pageable
   * @return
   */
  @ResolvedName("findAll")
  fun findAllPageable(@Param("pageable") pageable: Pageable): Page<Student>

  /**
   *
   * @param min
   * @param max
   * @return
   */
  fun findByAgeBetweenMinAndMaxOrderByBirthday(
      @Param("min") min: Int,
      @Param("max") max: Int
  ): List<Student>

  /**
   * @param startTime
   * @param endTime
   * @return
   */
  fun findByCreateTimeBetweenStartTimeAndEndTime(
      @Param("startTime") startTime: LocalDateTime?, @Param("endTime") endTime: LocalDateTime?
  ): List<Student>

  /**
   * @param age
   * @param name
   * @return
   */
  @ResolvedName("findByAge", conditions = ["name"])
  fun findByAge(@Param("age") age: Int, @Param("name") name: String): List<Student>

  /**
   *
   * @param age
   * @return
   */
  fun findByAgeGte(@Param("age") age: Int): List<Student>

  /**
   *
   * @param date
   * @return
   */
  @SelectedProperties(properties = ["id", "name", "createTime", "birthday"])
  fun findByBirthday(date: LocalDate): List<Student>

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
   * @param createTime
   * @return
   */
  fun findByCreateTimeEqDate(createTime: LocalDate): List<Student>

  /**
   * @param createTime
   * @return
   */
  fun findByCreateTimeEqMonth(createTime: LocalDate): List<Student>

  /**
   *
   * @param phoneNumber
   * @return
   */
  fun findByPhoneNumberLikeRight(@Param("phoneNumber") phoneNumber: String): List<Student>

  /**
   *
   * @param state
   * @return
   */
  fun findByStateIn(@Param("state") state: List<StudentState>): List<Student>

  /**
   *
   * @param subJectId
   * @return
   * @param subjectId
   */
  fun findBySubjectId(@Param("subjectId") subjectId: Int): List<Student>

  /**
   *
   * @return
   */
  fun findDetail(): List<StudentDetail>

  /**
   *
   * @return
   */
  fun findId(): List<String>

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
   */
  fun saveOrUpdate(student: Student)

  /**
   *
   * @param list
   */
  @ExcludeProperties(update = ["name"])
  fun saveOrUpdateAll(list: List<Student>)

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

  /**
   * @param form
   * @return
   */
  fun findByNameOrAge(form: QueryForm): List<Student>

  /**
   * @param form
   * @return
   */
  fun findByNameLikeAndAgeAndCreateTimeBetweenStartAndEnd(form: QueryForm): List<Student>

  /**
   * @param form
   * @param pageable
   * @return
   */
  @ResolvedName("findByNameLikeAndAgeAndCreateTimeBetweenStartAndEnd")
  fun findByNameLikeAndAgeAndCreateTimeBetweenStartAndEndPageable3(
      form: QueryForm, pageable: Pageable
  ): Page<Student>

  /**
   * @param form
   * @param pageable
   * @return
   */
  @ResolvedName("findByNameLikeAndAgeAndCreateTimeBetweenStartAndEnd")
  fun findByNameLikeAndAgeAndCreateTimeBetweenStartAndEndPageable(
      @Param("form") form: QueryForm, pageable: Pageable
  ): Page<Student>

  /**
   * @param form
   * @param pageable
   * @return
   */
  @ResolvedName("findByNameLikeAndAgeAndCreateTimeBetweenStartAndEnd")
  fun findByNameLikeAndAgeAndCreateTimeBetweenStartAndEndPageable2(
      @Param("form") form: QueryForm, @Param("p") pageable: Pageable
  ): Page<Student>

  /**
   * @param min
   * @param max
   * @return
   */
  fun findByAgeBetween(min: Int?, max: Int?): List<Student>

  /**
   * @param min
   * @param max
   * @return
   */
  fun findByAgeBetweenMinAndMax(min: Int?, max: Int?): List<Student>

}

class StudentQueryForm(
    var age: Int? = null,
    var name: String? = null,
    var start: Date? = null,
    var end: Date? = null,
    var keywords: String? = null
) {

  @Criteria(
      expression = "name eq currentUserId",
      testExpression = "type = 1 and currentUserId != null",
  )
  @Criteria(
      expression = "updateUserId eq currentUserId",
      testExpression = "type = 2 and currentUserId != null"
  )
  @Criteria(
      expression = "createUserId eq currentUserId",
      testExpression = ">= 5 and currentUserId != null",
  )
  @Criteria(
      expression = "userId eq currentUserId",
      testExpression = "<= 12 and currentUserId != null",
  )
  var type: Int? = null

}

class QueryForm(
    val name: String? = null,
    val age: Int? = null,
    val start: Date? = null,
    val end: Date? = null
)
