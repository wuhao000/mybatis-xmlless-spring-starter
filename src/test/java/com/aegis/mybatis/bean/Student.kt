package com.aegis.mybatis.bean

import com.aegis.mybatis.xmlless.annotations.Handler
import com.aegis.mybatis.xmlless.annotations.JoinObject
import com.aegis.mybatis.xmlless.annotations.ModifyIgnore
import com.baomidou.mybatisplus.annotation.TableField
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.persistence.Id

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

  @TableField("sex")
  var gender: Int = 1
  var graduated: Boolean = false
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
      |)""".trimMargin()
  }

}
