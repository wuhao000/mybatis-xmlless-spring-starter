package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.*
import com.baomidou.mybatisplus.annotation.TableField
import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.springframework.data.annotation.CreatedDate
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.LocalDate
import java.time.LocalDateTime

@JsonMappingProperty
data class EducationInfo(
    val school: String = ""
)

@JsonMappingProperty
data class StudentDetail(
    var height: Int? = null
)

/**
 * for test
 * @author 吴昊
 * @since 0.0.6
 */
class StringTypeHandler : BaseTypeHandler<String>() {

  override fun getNullableResult(rs: ResultSet, columnName: String?): String? {
    return rs.getString(columnName)
  }

  override fun getNullableResult(rs: ResultSet, columnIndex: Int): String? {
    return rs.getString(columnIndex)
  }

  override fun getNullableResult(cs: CallableStatement, columnIndex: Int): String? {
    return cs.getString(columnIndex)
  }

  override fun setNonNullParameter(ps: PreparedStatement, i: Int, parameter: String?, jdbcType: JdbcType?) {
    ps.setString(i, parameter)
  }

}

/**
 *
 * @author 吴昊
 * @since 0.0.4
 */
class Student() {

  var age: Int = 0
  var birthday: LocalDate? = null

  @Count(
      targetTable = "t_score",
      targetColumn = "student_id",
      countColumn = "id"
  )
  @Transient
  var count: Int = 0
  @CreatedDate
  var createTime: LocalDateTime? = null

  @JsonMappingProperty
  var detail: StudentDetail? = null

  @JsonMappingProperty
  var education: List<EducationInfo>? = null
  var email: String? = null

  @JsonMappingProperty
  var favorites: List<String> = listOf()

  @Column(name = "sex")
  @TableField("sex")
  var gender: Int = 1
  var graduated: Boolean = false

  @Id
  var id: String = ""
  var name: String = ""

  @JsonMappingProperty
  var nickNames: List<String>? = null

  @Handler(StringTypeHandler::class)
  var phoneNumber: String = ""

  @JoinObject(
      toEntity = JoinEntity(Score::class, joinOnProperty = "studentId"),
      joinOnProperty = "id"
  )
  @SelectedProperties(["score", "subjectId", "subject"])
  @JoinColumn(insertable = false, updatable = false)
  var scores: MutableList<Score>? = null
  var state: StudentState = StudentState.normal

  constructor(
      id: String, name: String, phoneNumber: String,
      gender: Int, state: StudentState = StudentState.normal
  ) : this() {
    this.id = id
    this.name = name
    this.phoneNumber = phoneNumber
    this.gender = gender
    this.state = state
  }

  override fun toString(): String {
    return """Student(
      | name=$name,
      | phoneNumber=$phoneNumber,
      | gender=$gender,
      | graduated=$graduated,
      | scores=$scores
      | favorites=$favorites
      | birthday=$birthday
      |)""".trimMargin()
  }

}

enum class StudentState {

  abnormal,
  normal;

}
