package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.*
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.persistence.Column
import javax.persistence.Id

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
@Suppress("MemberVisibilityCanBePrivate")
class Student() {

  @Count(
      targetTable = "t_score",
      targetColumn = "student_id",
      countColumn = "id"
  )
  var count: Int = 0

  @JsonMappingProperty
  var detail: StudentDetail? = null

  @Column(name = "sex")
  var gender: Int = 1
  var graduated: Boolean = false
  @JsonMappingProperty
  var favorites: List<String> = listOf()
  @Id
  var id: String = ""
  var name: String = ""

  @Handler(StringTypeHandler::class)
  var phoneNumber: String = ""

  @JoinObject(
      targetTable = "t_score",
      targetColumn = "student_id",
      joinProperty = "id",
      associationPrefix = "score_",
      selectProperties = ["score", "subjectId", "subject"]
  )
  @ModifyIgnore
  var scores: MutableList<Score>? = null

  constructor(id: String, name: String, phoneNumber: String, gender: Int)
      : this() {
    this.id = id
    this.name = name
    this.phoneNumber = phoneNumber
    this.gender = gender
  }

  override fun toString(): String {
    return """Student(
      | name=$name,
      | phoneNumber=$phoneNumber,
      | gender=$gender,
      | graduated=$graduated,
      | scores=$scores
      | favorites=$favorites
      |)""".trimMargin()
  }

}
