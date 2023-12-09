package com.aegis.mybatis.dao

import com.aegis.mybatis.bean.Student
import com.aegis.mybatis.bean.StudentDetail
import com.aegis.mybatis.bean.StudentState
import com.aegis.mybatis.xmlless.XmlLessMapper
import com.aegis.mybatis.xmlless.annotations.*
import com.aegis.mybatis.xmlless.enums.TestType
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class StudentQueryForm(
    var age: Int? = null,
    var name: String? = null,
    var start: Date? = null,
    var end: Date? = null,
    var keywords: String? = null
) {
  @Criteria(
      expression = "name eq currentUserId",
      test = TestExpression(
          expression = "type = 1 and currentUserId != null",
      )
  )
  @Criteria(
      expression = "updateUserId eq currentUserId",
      test = TestExpression(
          expression = "type = 2 and currentUserId != null",
      )
  )
  @Criteria(
      expression = "updateUserId eq currentUserId",
      test = TestExpression(
          expression = ">= 5 and currentUserId != null",
      )
  )
  @Criteria(
      expression = "updateUserId eq currentUserId",
      test = TestExpression(
          expression = "<= 12 and currentUserId != null",
      )
  )
  var type: Int? = null

}

/**
 *
 * Created by 吴昊 on 2018-12-12.
 *
 * @author 吴昊
 * @since 0.0.4
 */
@Mapper
interface StudentDAO : XmlLessMapper<Student> {

  @ResolvedName(
      name = "findBy",
      partNames = [
        "name like", "age", "createTime between start and end",
        "userName like keywords",
        "order by createTime desc",
      ]
  )
  fun find(
      @Param("form") form: StudentQueryForm,
      @Param("currentUserId") currentUserId: Int? = null
  ): List<Student>

  @ResolvedName(
      name = "findBy",
      partNames = [
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
      @TestExpression([TestType.NotNull, TestType.NotEmpty]) name: String?,
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
      @Param("min")
      @TestExpression([TestType.NotNull])
      min: Int,
      @Param("max")
      @TestExpression([TestType.NotNull])
      max: Int
  ): List<Student>

  fun findByCreateTimeBetweenStartTimeAndEndTime(
      @Param("startTime") startTime: LocalDateTime?, @Param("endTime") endTime: LocalDateTime?
  ): List<Student>

  @ResolvedName("findByAge", partNames = ["name"])
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

  fun findByCreateTimeEqDate(createTime: LocalDate): List<Student>

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

  fun findByNameOrAge(form: QueryForm): List<Student>

  fun findByNameLikeAndAgeAndCreateTimeBetweenStartAndEnd(form: QueryForm): List<Student>

  @ResolvedName("findByNameLikeAndAgeAndCreateTimeBetweenStartAndEnd")
  fun findByNameLikeAndAgeAndCreateTimeBetweenStartAndEndPageable3(
      form: QueryForm, pageable: Pageable
  ): Page<Student>

  @ResolvedName("findByNameLikeAndAgeAndCreateTimeBetweenStartAndEnd")
  fun findByNameLikeAndAgeAndCreateTimeBetweenStartAndEndPageable(
      @Param("form") form: QueryForm, pageable: Pageable
  ): Page<Student>


  @ResolvedName("findByNameLikeAndAgeAndCreateTimeBetweenStartAndEnd")
  fun findByNameLikeAndAgeAndCreateTimeBetweenStartAndEndPageable2(
      @Param("form") form: QueryForm, @Param("p") pageable: Pageable
  ): Page<Student>

  fun findByAgeBetween(min: Int?, max: Int?): List<Student>

  fun findByAgeBetweenMinAndMax(min: Int?, max: Int?): List<Student>
}


class QueryForm(
    val name: String? = null,
    val age: Int? = null,
    val start: Date? = null,
    val end: Date? = null
)
