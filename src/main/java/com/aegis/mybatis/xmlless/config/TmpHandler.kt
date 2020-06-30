package com.aegis.mybatis.xmlless.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet


open class TmpHandler(val type: JavaType? = null) : BaseTypeHandler<Any>() {

  val mapper = ObjectMapper().apply {
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
    this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    this.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
    this.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
    this.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
    this.configure(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY, false)
    this.configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false)
    this.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, false)
    this.configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, false)
    this.configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, false)
  }

  override fun getNullableResult(rs: ResultSet, column: String): Any? {
    val columnValue = rs.getString(column)
    return if (columnValue == null) {
      null
    } else {
      toTypeValue(columnValue)
    }
  }

  override fun getNullableResult(rs: ResultSet, columnIndex: Int): Any? {
    val str = rs.getString(columnIndex)
    return toTypeValue(str)
  }

  override fun getNullableResult(cs: CallableStatement, columnIndex: Int): Any? {
    val str = cs.getString(columnIndex)
    return toTypeValue(str)
  }

  override fun setNonNullParameter(ps: PreparedStatement, i: Int, parameter: Any?, jdbcType: JdbcType?) {
    ps.setString(i, mapper.writeValueAsString(parameter))
  }

  private fun toTypeValue(str: String): Any? = mapper.readValue(str, type)

}
